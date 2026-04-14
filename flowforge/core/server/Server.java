package flowforge.core.server;
import flowforge.core.annotations.Auth;
import flowforge.core.middleware.AuthMiddleware;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import flowforge.core.annotations.*;
import flowforge.core.middleware.*;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {

    private static final Map<String, Method> routes = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void addRoute(String httpMethod, String path, Method method, Object controller) {
        String key = httpMethod + ":" + path;
        routes.put(key, method);
        controllers.put(key, controller);
    }

    public static void start(int port) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

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
                for (String routeKey : routes.keySet()) {

                    String[] parts = routeKey.split(":", 2);
                    String routeMethod = parts[0];
                    String routePath = parts[1];

                    if (!routeMethod.equals(httpMethod)) continue;

                    Map<String, String> vars = matchPath(routePath, path);

                    if (vars != null) {
                        method = routes.get(routeKey);
                        controller = controllers.get(routeKey);
                        pathVars = vars;
                        break;
                    }
                }

                if (method != null) {

                    String requestBody = readRequestBody(exchange);
                    RequestContext ctx = new RequestContext(exchange, pathVars, requestBody);

                    // 🔥 Middleware
                    List<Middleware> middlewareList = new ArrayList<>();
                    
                 // 🔥 Auto attach AuthMiddleware if @Auth present
                    if (method.isAnnotationPresent(Auth.class) ||
                    	    controller.getClass().isAnnotationPresent(Auth.class)) {

                    	    Auth auth = method.isAnnotationPresent(Auth.class)
                    	            ? method.getAnnotation(Auth.class)
                    	            : controller.getClass().getAnnotation(Auth.class);

                    	    middlewareList.add(new AuthMiddleware(auth.role()));
                    	}                    

                    if (method.isAnnotationPresent(Before.class)) {
                        middlewareList.add(createMiddleware(method.getAnnotation(Before.class).value()));
                    }
                    if (method.isAnnotationPresent(After.class)) {
                        middlewareList.add(createMiddleware(method.getAnnotation(After.class).value()));
                    }
                    if (method.isAnnotationPresent(Around.class)) {
                        middlewareList.add(createMiddleware(method.getAnnotation(Around.class).value()));
                    }

                    final Method finalMethod = method;
                    final Object finalController = controller;

                    MiddlewareChain chain = new MiddlewareChain(middlewareList, (context) -> {
                        return invokeController(finalMethod, finalController, context, queryParams);
                    });

                    Object result = chain.proceed(ctx);

                    if (ctx.handled) {
                        return; // 🔥 STOP everything
                    }
                    
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
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                }

            } catch (Exception e) {
                e.printStackTrace();

                try {
                    response = mapper.writeValueAsString(
                            Map.of("error", e.getMessage())
                    );
                } catch (Exception ignored) {}

                statusCode = 500;
                exchange.getResponseHeaders().add("Content-Type", "application/json");
            }

            sendResponse(exchange, response, statusCode);
        });

        System.out.println("Server started on port " + port);
        server.start();
    }

    // 🔥 Controller invocation
    private static Object invokeController(
            Method method,
            Object controller,
            RequestContext ctx,
            Map<String, String> queryParams) throws Exception {

        int paramCount = method.getParameterCount();

        if (paramCount > 0) {

            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[paramCount];

            for (int i = 0; i < paramCount; i++) {

                Parameter param = parameters[i];
                Class<?> type = param.getType();

                // 🔥 PathVariable
                if (param.isAnnotationPresent(PathVariable.class)) {

                    PathVariable pv = param.getAnnotation(PathVariable.class);
                    String value = ctx.pathVars.get(pv.value());

                    args[i] = convertType(value, type);

                }
                // 🔥 QueryParam (with required support)
                else if (param.isAnnotationPresent(QueryParam.class)) {

                    QueryParam qp = param.getAnnotation(QueryParam.class);
                    String value = queryParams.get(qp.value());

                    if (value == null) {
                        if (qp.required()) {
                            throw new RuntimeException("Missing query param: " + qp.value());
                        } else {
                            args[i] = null;
                        }
                    } else {
                        args[i] = convertType(value, type);
                    }
                }
                // 🔥 JSON body
                else {
                    args[i] = mapper.readValue(ctx.body, type);
                }
            }

            return method.invoke(controller, args);

        } else {
            return method.invoke(controller);
        }
    }

    // 🔥 Query parser
    private static Map<String, String> parseQuery(String query) {

        Map<String, String> map = new HashMap<>();

        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] kv = pair.split("=");

            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }

        return map;
    }

    private static Middleware createMiddleware(Class<?> clazz) throws Exception {
        return (Middleware) clazz.getDeclaredConstructor().newInstance();
    }

    private static Map<String, String> matchPath(String routePath, String requestPath) {

        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) return null;

        Map<String, String> pathVars = new HashMap<>();

        for (int i = 0; i < routeParts.length; i++) {

            if (routeParts[i].startsWith("{") && routeParts[i].endsWith("}")) {
                String varName = routeParts[i].substring(1, routeParts[i].length() - 1);
                pathVars.put(varName, requestParts[i]);
            } else if (!routeParts[i].equals(requestParts[i])) {
                return null;
            }
        }

        return pathVars;
    }

    private static Object convertType(String value, Class<?> type) {

        if (value == null || value.isEmpty()) {

            if (type == int.class || type == Integer.class) return 0;
            if (type == long.class || type == Long.class) return 0L;
            if (type == double.class || type == Double.class) return 0.0;

            return null;
        }

        if (type == String.class) return value;
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