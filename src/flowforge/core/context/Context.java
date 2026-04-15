package flowforge.core.context;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    public static void addBean(Class<?> clazz, Object instance) {

        System.out.println("📦 [Context] Adding bean: " + clazz.getName());

        beans.put(clazz, instance);
    }

    public static <T> T getBean(Class<T> clazz) {

        System.out.println("🔍 [Context] Looking for bean: " + clazz.getName());

        Object bean = beans.get(clazz);

        if (bean != null) {
            System.out.println("✅ [Context] Found exact bean: " + clazz.getName());
            return clazz.cast(bean);
        }

        for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {

            if (clazz.isAssignableFrom(entry.getKey())) {
                System.out.println("✅ [Context] Found assignable bean: " + entry.getKey().getName());
                return clazz.cast(entry.getValue());
            }
        }

        System.out.println("❌ [Context] Bean NOT found: " + clazz.getName());

        return null;
    }

    public static boolean contains(Class<?> clazz) {
        return getBean(clazz) != null;
    }
}