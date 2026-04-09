package flowforge.core.dispatcher;

import flowforge.core.annotations.*;
import flowforge.core.server.Server;

import java.lang.reflect.Method;

public class Dispatcher {

    public static void register(Object controller) {

        Class<?> clazz = controller.getClass();

        if (clazz.isAnnotationPresent(Controller.class)) {

            for (Method method : clazz.getDeclaredMethods()) {

                // GET
                if (method.isAnnotationPresent(Get.class)) {
                    Get mapping = method.getAnnotation(Get.class);
                    Server.addRoute("GET", mapping.value(), method, controller);
                    System.out.println("Mapped GET: " + mapping.value());
                }

                // POST
                if (method.isAnnotationPresent(Post.class)) {
                    Post mapping = method.getAnnotation(Post.class);
                    Server.addRoute("POST", mapping.value(), method, controller);
                    System.out.println("Mapped POST: " + mapping.value());
                }
            }
        }
    }
}