package flowforge.core.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import flowforge.core.annotations.PathVariable;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

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
                    Object result;

                    int paramCount = method.getParameterCount();

                    if (paramCount > 0) {

                        Parameter[] parameters = method.getParameters();
                        Object[] args = new Object[paramCount];

                        for (int i = 0; i < paramCount; i++) {

                            Parameter param = parameters[i];
                            Class<?> type = param.getType();

                            // 🔥 Handle @PathVariable
                            if (param.isAnnotationPresent(PathVariable.class)) {

                                PathVariable pv = param.getAnnotation(PathVariable.class);
                                String name = pv.value();

                                String value = pathVars.get(name);

                                args[i] = convertType(value, type);

                            } else {
                                // 🔥 JSON fallback
                                try {
                                    args[i] = mapper.readValue(requestBody, type);
                                } catch (Exception e) {
                                    response = mapper.writeValueAsString(
                                            Map.of("error", "Invalid JSON")
                                    );
                                    statusCode = 400;
                                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                                    sendResponse(exchange, response, statusCode);
                                    return;
                                }
                            }
                        }

                        result = method.invoke(controller, args);

                    } else {
                        result = method.invoke(controller);
                    }

                    // 🔥 Response handling
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
                            Map.of("error", "Internal Server Error")
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

    // 🔥 Match path variables
    private static Map<String, String> matchPath(String routePath, String requestPath) {

        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) {
            return null;
        }

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

    // 🔥 Type conversion
    private static Object convertType(String value, Class<?> type) {

        if (type == String.class) return value;

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }

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