package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The Around annotation declares that the annotated method or type should be executed
 * around the specified class during processing.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {

    /**
     * The class around which the annotated method or type is executed.
     *
     * @return the class to execute around
     */
    Class<?> value();
}