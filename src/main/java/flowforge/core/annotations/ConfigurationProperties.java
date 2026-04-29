package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The ConfigurationProperties annotation binds external configuration properties
 * to the annotated class using the specified prefix.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigurationProperties {

    /**
     * The prefix used to resolve configuration properties for the annotated class.
     *
     * @return the configuration prefix
     */
    String value();
}