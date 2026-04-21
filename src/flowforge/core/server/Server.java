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
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {

    private static final Map<String, Method> routeMethods = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    // Dev dashboard
    private static final List<RouteInfo> routeList = new ArrayList<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    // =========================
    public static class RouteInfo {
        public String method;
        public String path;
        public String handler;
        public List<String> params;
        public boolean auth;

        public RouteInfo(String method, String path, String handler,
                        List<String> params, boolean auth) {
            this.method = method;
            this.path = path;
            this.handler = handler;
            this.params = params;
            this.auth = auth;
        }
    }

    // =========================
    // Load docs-ui.html from classpath
    // Place the file at:
    //   src/main/resources/flowforge/core/server/docs-ui.html
    // =========================
    private static String loadDocsTemplate() throws Exception {
        try (InputStream is = Server.class.getResourceAsStream("docs-ui.html")) {
            if (is == null) {
                throw new IllegalStateException(
                    "docs-ui.html not found on classpath. " +
                    "Place it at: src/main/resources/flowforge/core/server/docs-ui.html"
                );
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // =========================
    // Register route
    // =========================
    public static void addRoute(String httpMethod, String path, Method method, Object controller) {

        String key = httpMethod + ":" + path;

        routeMethods.put(key, method);
        controllers.put(key, controller);

        List<String> params = new ArrayList<>();

        for (Parameter p : method.getParameters()) {

            if (p.isAnnotationPresent(QueryParam.class)) {
                QueryParam qp = p.getAnnotation(QueryParam.class);
                params.add(qp.value());
            }

            if (p.isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = p.getAnnotation(PathVariable.class);
                params.add(pv.value());
            }
        }

        boolean auth = method.isAnnotationPresent(Auth.class) ||
                    controller.getClass().isAnnotationPresent(Auth.class);

        routeList.add(new RouteInfo(
                httpMethod,
                path,
                controller.getClass().getSimpleName() + "#" + method.getName(),
                params,
                auth
        ));
    }

    // =========================
    // START SERVER
    // =========================
    public static void start(int port) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // =========================
        // DEV DASHBOARD (IMPORTANT: BEFORE "/")
        // =========================
        server.createContext("/dev/routes", exchange -> {

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
        // SWAGGER-LIKE DOCS UI
        // Loads docs-ui.html from classpath and injects live route data.
        // =========================
        server.createContext("/dev/docs-ui", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                // Encode route data as base64 and inject into the HTML template
                String routeJson = mapper.writeValueAsString(routeList);
                String routeJsonBase64 = Base64.getEncoder()
                    .encodeToString(routeJson.getBytes(StandardCharsets.UTF_8));

                String html = loadDocsTemplate()
                    .replace("{{ROUTES_BASE64}}", routeJsonBase64);

                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.flush();
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
                sendSafe(exchange, "Failed to load docs-ui.html: " + e.getMessage(), 500);
            }
        });

        // =========================
        // MAIN ROUTER
        // =========================
        server.createContext("/", exchange -> {

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

                // Route matching
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

                if (method == null) {
                    sendSafe(exchange, "404 Not Found", 404);
                    return;
                }

                String requestBody = readBody(exchange);
                RequestContext ctx = new RequestContext(exchange, pathVars, requestBody);

                List<Middleware> middlewareList = new ArrayList<>();

                // Auth middleware
                boolean authEnabled = Config.getBoolean("auth.enabled", true);

                if (authEnabled &&
                        (method.isAnnotationPresent(Auth.class) ||
                         controller.getClass().isAnnotationPresent(Auth.class))) {

                    Auth auth = method.isAnnotationPresent(Auth.class)
                            ? method.getAnnotation(Auth.class)
                            : controller.getClass().getAnnotation(Auth.class);

                    middlewareList.add(new AuthMiddleware(auth.role()));
                }

                Method finalMethod = method;
                Object finalController = controller;

                MiddlewareChain chain = new MiddlewareChain(
                        middlewareList,
                        context -> invokeController(finalMethod, finalController, context, queryParams)
                );

                Object result = chain.proceed(ctx);

                if (ctx.handled) return;

                // Response handling
                if (result instanceof String) {
                    response = (String) result;
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                } else {
                    response = mapper.writeValueAsString(result);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                }

            } catch (Exception e) {

                e.printStackTrace();

                try {

                    Throwable actual = (e instanceof java.lang.reflect.InvocationTargetException)
                            ? e.getCause()
                            : e;

                    ExceptionManager.Handler handler = ExceptionManager.getHandler(actual);

                    if (handler != null) {

                        Object result = handler.method.invoke(handler.instance, actual);

                        if (result instanceof String) {
                            response = (String) result;
                            exchange.getResponseHeaders().add("Content-Type", "text/plain");
                        } else {
                            response = mapper.writeValueAsString(result);
                            exchange.getResponseHeaders().add("Content-Type", "application/json");
                        }

                    } else {

                        response = mapper.writeValueAsString(
                                Map.of(
                                        "error", actual.getClass().getSimpleName(),
                                        "message", actual.getMessage()
                                )
                        );
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    response = "Critical Error";
                }

                statusCode = 500;
            }

            sendSafe(exchange, response, statusCode);
        });

        System.out.println(" Server started on port " + port);
        server.start();
    }

    // =========================
    // Controller invocation
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
            Object value = null;

            // =========================
            // 🔥 1. Query Param
            // =========================
            if (param.isAnnotationPresent(QueryParam.class)) {

                QueryParam qp = param.getAnnotation(QueryParam.class);
                value = convert(queryParams.get(qp.value()), param.getType());
            }

            // =========================
            // 🔥 2. Path Variable (FIX)
            // =========================
            else if (param.isAnnotationPresent(PathVariable.class)) {

                PathVariable pv = param.getAnnotation(PathVariable.class);
                String pathVal = ctx.pathVars.get(pv.value());

                value = convert(pathVal, param.getType());
            }

            // =========================
            // 🔥 3. Request Body (SAFE)
            // =========================
            else {

                // Only parse if body exists
                if (ctx.body != null && !ctx.body.isEmpty()) {
                    value = mapper.readValue(ctx.body, param.getType());
                }
            }

            // =========================
            // 🔥 Validation
            // =========================
            Validator.validate(param, value);

            args[i] = value;
        }

        return method.invoke(controller, args);
    }

    // =========================
    private static Map<String, String> parseQuery(String query) {

        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }

        return map;
    }

    private static Map<String, String> matchPath(String route, String path) {

        String[] r = route.split("/");
        String[] p = path.split("/");

        if (r.length != p.length) return null;

        Map<String, String> vars = new HashMap<>();

        for (int i = 0; i < r.length; i++) {

            if (r[i].startsWith("{")) {
                vars.put(r[i].replace("{", "").replace("}", ""), p[i]);
            } else if (!r[i].equals(p[i])) {
                return null;
            }
        }

        return vars;
    }

    private static Object convert(String value, Class<?> type) {

        if (value == null) return null;

        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);

        return value;
    }

    private static String readBody(HttpExchange ex) {

        try {
            InputStream is = ex.getRequestBody();
            return new String(is.readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }

    // =========================
    // SAFE RESPONSE (no crash)
    // =========================
    private static void sendSafe(HttpExchange ex, String res, int code) {
        try {
            byte[] bytes = res.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(code, bytes.length);
            OutputStream os = ex.getResponseBody();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}