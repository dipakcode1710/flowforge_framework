package flowforge.core.annotations;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {
    String value();
    boolean required() default true;  // 🔥 NEW
}