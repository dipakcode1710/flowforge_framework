package flowforge.core.context;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    public static void addBean(Class<?> clazz, Object instance) {
        beans.put(clazz, instance);
    }

    public static <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }

    public static boolean contains(Class<?> clazz) {
        return beans.containsKey(clazz);
    }
}