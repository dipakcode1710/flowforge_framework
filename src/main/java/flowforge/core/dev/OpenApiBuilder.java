package flowforge.core.dev;

import flowforge.core.server.Server.RouteInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class OpenApiBuilder {

    public static String build(List<RouteInfo> routes) {

        Map<String, Object> root = new HashMap<>();
        root.put("openapi", "3.0.0");

        Map<String, Object> paths = new HashMap<>();

        for (RouteInfo r : routes) {

            Map<String, Object> pathItem =
                (Map<String, Object>) paths.computeIfAbsent(r.path, k -> new HashMap<>());

            Map<String, Object> operation = new HashMap<>();
            operation.put("summary", r.handler);

            // params
            List<Map<String, Object>> params = new ArrayList<>();

            for (String p : r.params) {
                Map<String, Object> param = new HashMap<>();
                param.put("name", p);
                param.put("in", "query");
                param.put("required", false);
                param.put("schema", Map.of("type", "string"));
                params.add(param);
            }

            if (!params.isEmpty()) {
                operation.put("parameters", params);
            }

            operation.put("responses", Map.of(
                "200", Map.of("description", "OK")
            ));

            pathItem.put(r.method.toLowerCase(), operation);
        }

        root.put("paths", paths);

        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}