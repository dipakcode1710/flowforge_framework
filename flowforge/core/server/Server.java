package flowforge.core.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Map<String, Method> routes = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

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

                    // 🔥 Read request body
                    String requestBody = readRequestBody(exchange);

                    Object result;

                    // 🔥 Check if method expects parameter
                    if (method.getParameterCount() == 1) {
                        result = method.invoke(controller, requestBody);
                    } else {
                        result = method.invoke(controller);
                    }

                    // 🔥 Handle response types
                    if (result instanceof String) {
                        response = (String) result;
                    } else {
                        response = toJson(result); // convert object to JSON
                    }

                } else {
                    response = "404 Not Found";
                }

            } catch (Exception e) {
                e.printStackTrace();
                response = "500 Internal Server Error";
            }

            // 🔥 Set response headers
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        System.out.println("Server started on port " + port);
        server.start();
    }

    // ✅ Read request body
    private static String readRequestBody(HttpExchange exchange) {
        try {
            InputStream is = exchange.getRequestBody();
            return new String(is.readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }

    // ✅ Simple JSON converter (temporary)
    private static String toJson(Object obj) {
        if (obj == null) return "{}";

        try {
            StringBuilder json = new StringBuilder("{");
            var fields = obj.getClass().getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                Object value = fields[i].get(obj);

                json.append("\"")
                    .append(fields[i].getName())
                    .append("\":\"")
                    .append(value)
                    .append("\"");

                if (i < fields.length - 1) {
                    json.append(",");
                }
            }

            json.append("}");
            return json.toString();

        } catch (Exception e) {
            return "{}";
        }
    }
}