package flowforge;

import flowforge.core.annotations.Controller;
import flowforge.core.annotations.Service;
import flowforge.core.context.Context;
import flowforge.core.context.Injector;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.scanner.ClassScanner;
import flowforge.core.server.Server;
import flowforge.core.config.Config;
import flowforge.core.annotations.ConfigurationProperties;

import java.util.List;

public class Flow {

    public static void run(Class<?> appClass) {

        try {

            String basePackage = appClass.getPackageName();

            System.out.println("Scanning package: " + basePackage);

            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // =========================
            // 🔥 1. Register beans
            // =========================
            for (Class<?> clazz : classes) {

                if (
                        clazz.isAnnotationPresent(Controller.class) ||
                        clazz.isAnnotationPresent(Service.class) ||
                        clazz.isAnnotationPresent(ConfigurationProperties.class)
                ) {

                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    Context.addBean(clazz, instance);

                    System.out.println("📦 Registered Bean: " + clazz.getName());
                }
            }

            // =========================
            // 🔥 2. Inject dependencies
            // =========================
            for (Class<?> clazz : classes) {

                if (Context.contains(clazz)) {

                    Object bean = Context.getBean(clazz);

                    Injector.inject(bean);
                }
            }

            // =========================
            // 🔥 3. Register routes
            // =========================
            for (Class<?> clazz : classes) {

                if (clazz.isAnnotationPresent(Controller.class)) {

                    Object controller = Context.getBean(clazz);

                    Dispatcher.register(controller);

                    System.out.println("🚀 Controller Ready: " + clazz.getName());
                }
            }

            // =========================
            // 🔥 4. Start server
            // =========================
            int port = Config.getInt("server.port", 8080);

            Server.start(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}