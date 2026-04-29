package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The Before annotation declares that the annotated method or type should be executed
 * before the specified class is processed.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {

    /**
     * The class that must be processed after the annotated method or type is executed.
     *
     * @return the class to execute before
     */
    Class<?> value();
}