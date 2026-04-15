package flowforge.core.middleware;

import com.sun.net.httpserver.HttpExchange;
import java.util.List;
import java.util.Map;

public class RequestContext {

    public HttpExchange exchange;
    public Map<String, String> pathVars;
    public String body;
    public boolean handled = false;

    public RequestContext(HttpExchange exchange, Map<String, String> pathVars, String body) {
        this.exchange = exchange;
        this.pathVars = pathVars;
        this.body = body;
    }

    // 🔹 Get single header
    public String getHeader(String key) {
        List<String> values = exchange.getRequestHeaders().get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    // 🔹 Get all headers
    public Map<String, List<String>> getHeaders() {
        return exchange.getRequestHeaders();
    }

    // 🔹 Get method
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    // 🔹 Get path
    public String getPath() {
        return exchange.getRequestURI().getPath();
    }
}