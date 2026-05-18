package flowforge.core.ui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UiRouter {

    private static final Map<String, Class<? extends FlowView>> routes = new LinkedHashMap<>();

    public static void register(String path, Class<? extends FlowView> viewClass) {
        routes.put(path, viewClass);
    }

    public static Map<String, Class<? extends FlowView>> getAllRoutes() {
        return Collections.unmodifiableMap(routes);
    }

    public static boolean hasRoutes() {
        return !routes.isEmpty();
    }
}
