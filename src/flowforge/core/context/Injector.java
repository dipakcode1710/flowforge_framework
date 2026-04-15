package flowforge.core.context;

import flowforge.core.annotations.Inject;
import flowforge.core.annotations.Value;
import flowforge.core.config.Config;
import flowforge.core.config.ConfigBinder;

import java.lang.reflect.Field;

public class Injector {

    public static void inject(Object obj) {

        System.out.println("🚀 [Injector] Processing: " + obj.getClass().getName());

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {

            field.setAccessible(true);

            try {

                // =========================
                // 🔥 1. @Inject
                // =========================
                if (field.isAnnotationPresent(Inject.class)) {

                    Class<?> type = field.getType();

                    System.out.println("👉 [Injector] Trying to inject: " + type.getName());

                    Object dependency = Context.getBean(type);

                    if (dependency == null) {

                        System.out.println("❌ [Injector] Bean NOT found, creating: " + type.getName());

                        dependency = type.getDeclaredConstructor().newInstance();

                        Context.addBean(type, dependency);

                        System.out.println("✅ [Injector] Bean registered: " + type.getName());

                        // 🔥 Inject recursively
                        inject(dependency);

                        // 🔥 Bind config
                        ConfigBinder.bind(dependency);

                    } else {
                        System.out.println("✅ [Injector] Bean found: " + type.getName());
                    }

                    field.set(obj, dependency);

                    System.out.println("🔥 [Injector] Injected " + type.getName() +
                            " into " + obj.getClass().getName());
                }

                // =========================
                // 🔥 2. @Value
                // =========================
                if (field.isAnnotationPresent(Value.class)) {

                    Value val = field.getAnnotation(Value.class);

                    String key = val.value();
                    String configValue = Config.get(key);

                    if (configValue == null || configValue.isEmpty()) {
                        configValue = val.defaultValue();
                    }

                    Object converted = convert(field.getType(), configValue);

                    field.set(obj, converted);

                    System.out.println("⚙️ [Injector] @Value injected: " + key + " = " + converted);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // =========================
        // 🔥 Final config binding
        // =========================
        ConfigBinder.bind(obj);

        System.out.println("✅ [Injector] Completed: " + obj.getClass().getName());
    }

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

            return value;

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to convert config value '" + value + "' to " + type.getSimpleName()
            );
        }
    }
}