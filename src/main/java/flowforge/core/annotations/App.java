package flowforge.core.annotations;

import java.lang.annotation.*;

/**
 * The App annotation marks a class as the main entry point of a FlowForge application.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface App {}