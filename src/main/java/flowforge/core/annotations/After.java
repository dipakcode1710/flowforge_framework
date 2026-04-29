package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The After annotation declares that the annotated method or type should be executed
 * after the specified class has been processed.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface After {

    /**
     * The class that must be processed before the annotated method or type is executed.
     *
     * @return the class to execute after
     */
    Class<?> value();
}