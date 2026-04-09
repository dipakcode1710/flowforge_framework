package flowforge.core.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Map<String, Method> routes = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void addRoute(String httpMethod, String path, Method method, Object controller) {
        String key = httpMethod + ":" + path;
        routes.put(key, method);
        controllers.put(key, controller);
    }

    public static void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", Server::handleRequest);

        System.out.println("Server started on port " + port);
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String httpMethod = exchange.getRequestMethod();
        String key = httpMethod + ":" + path;

        int statusCode = 200;
        String responseBody;

        try {
            if (!routes.containsKey(key)) {
                statusCode = 404;
                responseBody = objectMapper.writeValueAsString(Map.of("error", "Not Found"));
            } else {
                Method method = routes.get(key);
                Object controller = controllers.get(key);
                Object[] args = resolveMethodArguments(exchange, method);
                Object result = method.invoke(controller, args);
                responseBody = serializeResponse(result);
            }
        } catch (InvalidJsonException e) {
            statusCode = 400;
            responseBody = objectMapper.writeValueAsString(Map.of("error", "Invalid JSON", "message", e.getMessage()));
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            statusCode = 500;
            responseBody = objectMapper.writeValueAsString(Map.of("error", "Internal Server Error"));
        } catch (Exception e) {
            e.printStackTrace();
            statusCode = 500;
            responseBody = objectMapper.writeValueAsString(Map.of("error", "Internal Server Error"));
        }

        writeResponse(exchange, statusCode, responseBody);
    }

    private static Object[] resolveMethodArguments(HttpExchange exchange, Method method) throws IOException {
        int parameterCount = method.getParameterCount();
        if (parameterCount == 0) {
            return new Object[0];
        }

        if (parameterCount > 1) {
            throw new IllegalArgumentException("Only 0 or 1 parameter methods are currently supported");
        }

        Class<?> parameterType = method.getParameterTypes()[0];
        String requestBody = readRequestBody(exchange);

        if (parameterType.equals(String.class)) {
            return new Object[]{requestBody};
        }

        if (requestBody == null || requestBody.isBlank()) {
            return new Object[]{null};
        }

        try {
            Object mappedBody = objectMapper.readValue(requestBody, parameterType);
            return new Object[]{mappedBody};
        } catch (JsonProcessingException e) {
            throw new InvalidJsonException("Request body could not be parsed", e);
        }
    }

    private static String serializeResponse(Object result) throws JsonProcessingException {
        if (result == null) {
            return "";
        }

        if (result instanceof String) {
            return (String) result;
        }

        return objectMapper.writeValueAsString(result);
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void writeResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static class InvalidJsonException extends RuntimeException {
        InvalidJsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
