package flowforge.core.dispatcher;

import flowforge.core.annotations.*;
import flowforge.core.server.Server;
import flowforge.core.server.ExceptionManager;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class Dispatcher {

    // 🔥 Track registered routes (avoid duplicates)
    private static final Set<String> registeredRoutes = new HashSet<>();

    public static void register(Object controller) {

        Class<?> clazz = controller.getClass();

        // 🔥 Only process @Controller classes
        if (!clazz.isAnnotationPresent(Controller.class)) {
            return;
        }

        for (Method method : clazz.getDeclaredMethods()) {

            // =========================
            // 🔀 ROUTING (GET)
            // =========================
            if (method.isAnnotationPresent(Get.class)) {

                Get mapping = method.getAnnotation(Get.class);
                String path = mapping.value();
                String key = "GET:" + path;

                if (registeredRoutes.contains(key)) {
                    System.out.println("⚠️ Duplicate route skipped: " + key);
                    continue;
                }

                Server.addRoute("GET", path, method, controller);
                registeredRoutes.add(key);

                System.out.println("Mapped GET: " + path);
            }

            // =========================
            // 🔀 ROUTING (POST)
            // =========================
            if (method.isAnnotationPresent(Post.class)) {

                Post mapping = method.getAnnotation(Post.class);
                String path = mapping.value();
                String key = "POST:" + path;

                if (registeredRoutes.contains(key)) {
                    System.out.println("⚠️ Duplicate route skipped: " + key);
                    continue;
                }

                Server.addRoute("POST", path, method, controller);
                registeredRoutes.add(key);

                System.out.println("Mapped POST: " + path);
            }

            // =========================
            // 🔥 EXCEPTION HANDLER
            // =========================
            if (method.isAnnotationPresent(ExceptionHandler.class)) {

                ExceptionHandler eh = method.getAnnotation(ExceptionHandler.class);

                ExceptionManager.register(
                        eh.value(),
                        method,
                        controller
                );

                System.out.println("⚠️ ExceptionHandler mapped: " + eh.value().getSimpleName());
            }
        }
    }

    // 🔥 Optional reset (useful for dev reload)
    public static void clearRoutes() {
        registeredRoutes.clear();
    }
}