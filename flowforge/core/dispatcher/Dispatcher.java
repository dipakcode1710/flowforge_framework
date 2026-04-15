package flowforge.core.dispatcher;

import flowforge.core.annotations.*;
import flowforge.core.server.Server;

import java.lang.reflect.Method;

public class Dispatcher {

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

                Server.addRoute("GET", mapping.value(), method, controller);

                System.out.println("Mapped GET: " + mapping.value());
            }

            // =========================
            // 🔀 ROUTING (POST)
            // =========================
            if (method.isAnnotationPresent(Post.class)) {

                Post mapping = method.getAnnotation(Post.class);

                Server.addRoute("POST", mapping.value(), method, controller);

                System.out.println("Mapped POST: " + mapping.value());
            }

            // =========================
            // 🔥 EXCEPTION HANDLER
            // =========================
            if (method.isAnnotationPresent(ExceptionHandler.class)) {

                Server.registerExceptionHandler(method, controller);

                System.out.println("Registered ExceptionHandler: " + method.getName());
            }
        }
    }
}