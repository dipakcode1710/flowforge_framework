package flowforge;

import flowforge.core.annotations.*;
import flowforge.core.context.Context;
import flowforge.core.context.Injector;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.scanner.ClassScanner;
import flowforge.core.server.Server;
import flowforge.core.server.ExceptionManager;
import flowforge.core.config.Config;

import java.lang.reflect.Method;
import java.util.List;

public class Flow {

    public static void run(Class<?> appClass) {

        try {

            String basePackage = appClass.getPackageName();
            System.out.println("Scanning package: " + basePackage);

            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // =========================
            // 🔥 1. Register Beans
            // =========================
            for (Class<?> clazz : classes) {

                if (
                        clazz.isAnnotationPresent(Controller.class) ||
                        clazz.isAnnotationPresent(Service.class) ||
                        clazz.isAnnotationPresent(ConfigurationProperties.class)
                ) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    Context.addBean(clazz, instance);
                    System.out.println("📦 Bean Registered: " + clazz.getName());
                }
            }

            // =========================
            // 🔥 2. Inject Dependencies
            // =========================
            for (Class<?> clazz : classes) {

                if (Context.contains(clazz)) {
                    Object bean = Context.getBean(clazz);
                    Injector.inject(bean);
                }
            }

            // =========================
            // 🔥 3. Register Routes + Exception Handlers
            // =========================
            for (Class<?> clazz : classes) {

                Object bean = Context.getBean(clazz);
                if (bean == null) continue;

                // 🔥 Controllers
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Dispatcher.register(bean);
                    System.out.println("🚀 Controller Ready: " + clazz.getName());
                }

                // 🔥 Exception Handlers
                for (Method method : clazz.getDeclaredMethods()) {

                    if (method.isAnnotationPresent(ExceptionHandler.class)) {

                        ExceptionHandler eh = method.getAnnotation(ExceptionHandler.class);

                        ExceptionManager.register(
                                eh.value(),
                                method,
                                bean
                        );
                    }
                }
            }

            // =========================
            // 🔥 4. Start Server
            // =========================
            int port = Config.getInt("server.port", 8080);
            Server.start(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}