package flowforge.core.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Context {

    // Central bean registry
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    /**
     * Add bean to context (prevents duplicates)
     */
    public static void addBean(Class<?> clazz, Object instance) {

        if (beans.containsKey(clazz)) {
            System.out.println(" [Context] ⚠ Bean already exists, skipping: " + clazz.getName());
            return;
        }

        System.out.println(" [Context] ➕ Adding bean: " + clazz.getName());

        beans.put(clazz, instance);
    }

    /**
     * Get bean by type (exact or assignable)
     */
    public static <T> T getBean(Class<T> clazz) {

        System.out.println(" [Context] 🔍 Looking for bean: " + clazz.getName());

        // 1. Exact match
        Object bean = beans.get(clazz);

        if (bean != null) {
            System.out.println(" [Context] ✅ Found exact bean: " + clazz.getName());
            return clazz.cast(bean);
        }

        // 2. Assignable match (interface / parent class)
        for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {

            if (clazz.isAssignableFrom(entry.getKey())) {
                System.out.println(" [Context] ✅ Found assignable bean: " + entry.getKey().getName());
                return clazz.cast(entry.getValue());
            }
        }

        System.out.println(" [Context] ❌ Bean NOT found: " + clazz.getName());

        return null;
    }

    /**
     * Check if bean exists
     */
    public static boolean contains(Class<?> clazz) {
        return beans.containsKey(clazz);
    }

    /**
     * Get all beans (used for injector / lifecycle)
     */
    public static Map<Class<?>, Object> getAllBeans() {
        return Collections.unmodifiableMap(beans);
    }

    /**
     * Clear context (useful for testing / restart)
     */
    public static void clear() {
        System.out.println(" [Context] 🧹 Clearing all beans");
        beans.clear();
    }
}