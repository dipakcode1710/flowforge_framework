package flowforge.core.server;

import flowforge.core.annotations.*;
import flowforge.core.middleware.*;
import flowforge.core.validation.Validator;
import flowforge.core.config.Config;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {

    private static final Map<String, Method> routeMethods = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    // 🔥 Dev dashboard storage
    private static final List<RouteInfo> routeList = new ArrayList<>();

    private static Method exceptionHandlerMethod = null;
    private static Object exceptionHandlerInstance = null;

    private static final ObjectMapper mapper = new ObjectMapper();

    // =========================
    // 🔥 Route Info
    // =========================
    public static class RouteInfo {
        public String method;
        public String path;
        public String handler;

        public RouteInfo(String method, String path, String handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
        }
    }

    // =========================
    // 🔥 Add Route
    // =========================
    public static void addRoute(String httpMethod, String path, Method method, Object controller) {

        String key = httpMethod + ":" + path;

        routeMethods.put(key, method);
        controllers.put(key, controller);

        // 🔥 Store for dev dashboard
        routeList.add(new RouteInfo(
                httpMethod,
                path,
                controller.getClass().getSimpleName() + "#" + method.getName()
        ));

        System.out.println("Mapped " + httpMethod + ": " + path);
    }

    public static void registerExceptionHandler(Method method, Object controller) {
        exceptionHandlerMethod = method;
        exceptionHandlerInstance = controller;
    }

    // =========================
    // 🔥 START SERVER
    // =========================
    public static void start(int port) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // =========================
        // 🔥 DEV DASHBOARD (IMPORTANT: BEFORE "/")
        // =========================
        server.createContext("/dev/routes", exchange -> {

            System.out.println("🔥 DEV ROUTES HIT");

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String json = mapper.writeValueAsString(routeList);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);

            exchange.getResponseBody().write(json.getBytes());
            exchange.close();
        });

        // =========================
        // 🔥 MAIN ROUTER
        // =========================
        server.createContext("/", (HttpExchange exchange) -> {

            String path = exchange.getRequestURI().getPath();
            String httpMethod = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            Map<String, String> queryParams = parseQuery(query);

            String response = "";
            int statusCode = 200;

            try {

                Method method = null;
                Object controller = null;
                Map<String, String> pathVars = null;

                // 🔥 Route matching
                for (String routeKey : routeMethods.keySet()) {

                    String[] parts = routeKey.split(":", 2);
                    String routeMethod = parts[0];
                    String routePath = parts[1];

                    if (!routeMethod.equals(httpMethod)) continue;

                    Map<String, String> vars = matchPath(routePath, path);

                    if (vars != null) {
                        method = routeMethods.get(routeKey);
                        controller = controllers.get(routeKey);
                        pathVars = vars;
                        break;
                    }
                }

                if (method != null) {

                    String requestBody = readRequestBody(exchange);
                    RequestContext ctx = new RequestContext(exchange, pathVars, requestBody);

                    List<Middleware> middlewareList = new ArrayList<>();

                    // 🔐 Auth
                    boolean authEnabled = Config.getBoolean("auth.enabled", true);

                    if (authEnabled &&
                        (method.isAnnotationPresent(Auth.class) ||
                         controller.getClass().isAnnotationPresent(Auth.class))) {

                        Auth auth = method.isAnnotationPresent(Auth.class)
                                ? method.getAnnotation(Auth.class)
                                : controller.getClass().getAnnotation(Auth.class);

                        middlewareList.add(new AuthMiddleware(auth.role()));
                    }

                    final Method finalMethod = method;
                    final Object finalController = controller;

                    MiddlewareChain chain = new MiddlewareChain(middlewareList, (context) -> {
                        return invokeController(finalMethod, finalController, context, queryParams);
                    });

                    Object result = chain.proceed(ctx);

                    if (ctx.handled) return;

                    // 🔥 Response
                    if (result instanceof String) {
                        response = (String) result;
                        exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    } else {
                        response = mapper.writeValueAsString(result);
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                    }

                } else {
                    response = "404 Not Found";
                    statusCode = 404;
                }

            } catch (Exception e) {

                e.printStackTrace();

                try {
                    if (exceptionHandlerMethod != null) {

                        Object result = exceptionHandlerMethod.invoke(
                                exceptionHandlerInstance, e
                        );

                        if (result instanceof String) {
                            response = (String) result;
                        } else {
                            response = mapper.writeValueAsString(result);
                        }

                    } else {
                        response = mapper.writeValueAsString(
                                Map.of("error", e.getMessage())
                        );
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    response = "Critical Error";
                }

                statusCode = 500;
            }

            sendResponse(exchange, response, statusCode);
        });

        System.out.println("🚀 Server started on port " + port);
        server.start();
    }

    // =========================
    // 🔥 Controller invocation
    // =========================
    private static Object invokeController(
            Method method,
            Object controller,
            RequestContext ctx,
            Map<String, String> queryParams) throws Exception {

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter param = parameters[i];
            Class<?> type = param.getType();
            Object value;

            if (param.isAnnotationPresent(QueryParam.class)) {

                QueryParam qp = param.getAnnotation(QueryParam.class);
                value = convertType(queryParams.get(qp.value()), type);

            } else {
                value = mapper.readValue(ctx.body, type);
            }

            Validator.validate(param, value);
            args[i] = value;
        }

        return method.invoke(controller, args);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    private static Map<String, String> matchPath(String routePath, String requestPath) {

        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) return null;

        Map<String, String> pathVars = new HashMap<>();

        for (int i = 0; i < routeParts.length; i++) {

            if (routeParts[i].startsWith("{")) {
                String var = routeParts[i].replace("{", "").replace("}", "");
                pathVars.put(var, requestParts[i]);
            } else if (!routeParts[i].equals(requestParts[i])) {
                return null;
            }
        }

        return pathVars;
    }

    private static Object convertType(String value, Class<?> type) {

        if (value == null) return null;

        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);

        return value;
    }

    private static void sendResponse(HttpExchange exchange, String response, int statusCode) {
        try {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readRequestBody(HttpExchange exchange) {
        try {
            InputStream is = exchange.getRequestBody();
            return new String(is.readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }
}