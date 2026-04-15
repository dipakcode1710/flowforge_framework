package flowforge;

import flowforge.core.annotations.Controller;
import flowforge.core.context.Context;
import flowforge.core.context.Injector;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.scanner.ClassScanner;
import flowforge.core.server.Server;
import flowforge.core.config.Config;

import java.util.List;

public class Flow {

    public static void run(Class<?> appClass) {

        try {

            String basePackage = appClass.getPackageName();

            System.out.println("Scanning package: " + basePackage);

            List<Class<?>> classes = ClassScanner.scan(basePackage);

            for (Class<?> clazz : classes) {

                if (clazz.isAnnotationPresent(Controller.class)) {

                    Object controller = clazz.getDeclaredConstructor().newInstance();

                    // 🔥 Register bean
                    Context.addBean(clazz, controller);

                    // 🔥 Inject dependencies
                    Injector.inject(controller);

                    // 🔥 Register routes
                    Dispatcher.register(controller);

                    System.out.println("Registered Controller: " + clazz.getName());
                }
            }

            // 🔥 Use config port
            int port = Config.getInt("server.port", 8080);

            Server.start(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}