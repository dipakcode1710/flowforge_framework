package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * Sets the HTTP response status code for an @ExceptionHandler method.
 * If omitted, the status defaults to 500.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseStatus {

    /**
     * The HTTP status code to return.
     *
     * @return the HTTP status code
     */
    int value() default 500;
}