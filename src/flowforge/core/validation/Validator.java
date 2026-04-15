package flowforge.core.validation;

import flowforge.core.annotations.*;

import java.lang.reflect.Parameter;

public class Validator {

    public static void validate(Parameter param, Object value) {

        // 🔥 Resolve proper parameter name
        String name = resolveName(param);

        // =========================
        // 🔥 NotNull
        // =========================
        if (param.isAnnotationPresent(NotNull.class)) {
            if (value == null) {
                throw new RuntimeException("Parameter '" + name + "' cannot be null");
            }
        }

        // =========================
        // 🔥 Min
        // =========================
        if (param.isAnnotationPresent(Min.class)) {

            if (value != null) {
                long minValue = param.getAnnotation(Min.class).value();

                try {
                    long actual = Long.parseLong(value.toString());

                    if (actual < minValue) {
                        throw new RuntimeException(
                            "Parameter '" + name + "' must be >= " + minValue
                        );
                    }

                } catch (NumberFormatException e) {
                    throw new RuntimeException(
                        "Parameter '" + name + "' must be a valid number"
                    );
                }
            }
        }
    }

    // 🔥 Resolve parameter name (IMPORTANT)
    private static String resolveName(Parameter param) {

        if (param.isAnnotationPresent(QueryParam.class)) {
            return param.getAnnotation(QueryParam.class).value();
        }

        if (param.isAnnotationPresent(PathVariable.class)) {
            return param.getAnnotation(PathVariable.class).value();
        }

        // fallback (if no annotation)
        return param.getName();
    }
}