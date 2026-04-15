package flowforge.core.config;

import flowforge.core.annotations.ConfigurationProperties;

import java.lang.reflect.Field;

public class ConfigBinder {

    public static void bind(Object obj) {

        Class<?> clazz = obj.getClass();

        if (!clazz.isAnnotationPresent(ConfigurationProperties.class)) {
            return;
        }

        ConfigurationProperties cp = clazz.getAnnotation(ConfigurationProperties.class);
        String prefix = cp.value();

        System.out.println("⚙️ [ConfigBinder] Binding: " + clazz.getName());

        try {

            for (Field field : clazz.getDeclaredFields()) {

                field.setAccessible(true);

                String key = prefix + "." + field.getName();

                String value = Config.get(key);

                if (value != null) {

                    Object converted = convert(field.getType(), value);

                    field.set(obj, converted);

                    System.out.println("✅ [ConfigBinder] " + key + " = " + converted);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object convert(Class<?> type, String value) {

        if (value == null) return null;

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }

        return value;
    }
}