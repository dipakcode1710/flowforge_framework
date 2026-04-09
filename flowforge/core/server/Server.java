package flowforge.core.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Map<String, Method> routes = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    // 🔥 Jackson mapper
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

            String key = httpMethod + ":" + path;

            String response = "";

            try {

                if (routes.containsKey(key)) {

                    Method method = routes.get(key);
                    Object controller = controllers.get(key);

                    String requestBody = readRequestBody(exchange);

                    Object result;

                    // 🔥 Handle request body (JSON → Java)
                    if (method.getParameterCount() == 1) {

                        Class<?> paramType = method.getParameterTypes()[0];

                        Object arg;

                        try {
                            arg = mapper.readValue(requestBody, paramType);
                        } catch (Exception e) {
                            String errorJson = "{\"error\":\"Invalid JSON\"}";

                            exchange.getResponseHeaders().add("Content-Type", "application/json");
                            exchange.sendResponseHeaders(400, errorJson.length());
                            exchange.getResponseBody().write(errorJson.getBytes());
                            exchange.getResponseBody().close();
                            return;
                        }

                        result = method.invoke(controller, arg);

                    } else {
                        result = method.invoke(controller);
                    }

                    // 🔥 Handle response
                    if (result instanceof String) {
                        response = (String) result;
                        exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    } else {
                        response = mapper.writeValueAsString(result);
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                    }

                } else {
                    response = "404 Not Found";
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                }

            } catch (Exception e) {
                e.printStackTrace();
                response = "500 Internal Server Error";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        System.out.println("Server started on port " + port);
        server.start();
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