package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The Component annotation marks a class as a managed component within the FlowForge
 * application context, making it eligible for auto-detection and dependency injection.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {}