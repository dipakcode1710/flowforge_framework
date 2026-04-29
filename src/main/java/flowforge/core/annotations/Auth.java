package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The Auth annotation restricts access to the annotated method or type based on the
 * required role.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

    /**
     * The role required to access the annotated method or type.
     * Defaults to an empty string, meaning no specific role is required.
     *
     * @return the required role
     */
    String role() default "";
}