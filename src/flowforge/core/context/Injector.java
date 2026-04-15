package flowforge.core.context;

import flowforge.core.annotations.Inject;
import flowforge.core.annotations.Value;
import flowforge.core.config.Config;

import java.lang.reflect.Field;

public class Injector {

    public static void inject(Object obj) {

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {

            field.setAccessible(true);

            try {

                // =========================
                // 🔥 1. @Inject (existing)
                // =========================
                if (field.isAnnotationPresent(Inject.class)) {

                    Class<?> type = field.getType();
                    Object dependency;

                    if (Context.contains(type)) {
                        dependency = Context.getBean(type);
                    } else {
                        dependency = type.getDeclaredConstructor().newInstance();
                        Context.addBean(type, dependency);

                        // 🔥 IMPORTANT: inject nested dependencies
                        inject(dependency);
                    }

                    field.set(obj, dependency);
                }

                // =========================
                // 🔥 2. @Value (NEW FEATURE)
                // =========================
                if (field.isAnnotationPresent(Value.class)) {

                    Value val = field.getAnnotation(Value.class);
                    String key = val.value();

                    String configValue = Config.get(key);

                    Object converted = convert(field.getType(), configValue);

                    field.set(obj, converted);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================
    // 🔥 Type conversion
    // =========================
    private static Object convert(Class<?> type, String value) {

        if (value == null) return null;

        try {

            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(value);
            }

            if (type == long.class || type == Long.class) {
                return Long.parseLong(value);
            }

            if (type == double.class || type == Double.class) {
                return Double.parseDouble(value);
            }

            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(value);
            }

            // default → String
            return value;

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to convert config value '" + value + "' to " + type.getSimpleName()
            );
        }
    }
}