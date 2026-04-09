package flowforge.core.context;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    public static void register(Class<?> clazz) throws Exception {
        beans.put(clazz, clazz.getDeclaredConstructor().newInstance());
    }

    public static <T> T get(Class<T> clazz) {
        return (T) beans.get(clazz);
    }
}