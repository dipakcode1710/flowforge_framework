package flowforge.core.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExceptionManager {

    private static final Map<Class<? extends Throwable>, Handler> handlers = new HashMap<>();

    public static class Handler {
        public Method method;
        public Object instance;

        public Handler(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }
    }

    // 🔥 Register handler
    public static void register(Class<? extends Throwable> exType, Method method, Object instance) {
        handlers.put(exType, new Handler(method, instance));
        System.out.println("⚠️ Registered ExceptionHandler for: " + exType.getSimpleName());
    }

    // 🔥 Resolve handler
    public static Handler getHandler(Throwable ex) {

        Class<?> clazz = ex.getClass();

        // Exact match
        if (handlers.containsKey(clazz)) {
            return handlers.get(clazz);
        }

        // Parent match
        for (Class<? extends Throwable> key : handlers.keySet()) {
            if (key.isAssignableFrom(clazz)) {
                return handlers.get(key);
            }
        }

        return null;
    }
}