package flowforge;

import flowforge.core.annotations.Controller;
import flowforge.core.annotations.Service;
import flowforge.core.context.Context;
import flowforge.core.context.Injector;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.scanner.ClassScanner;
import flowforge.core.server.Server;

import java.util.List;

public class Flow {

    public static void run(Class<?> appClass) {

        try {

            String basePackage = appClass.getPackageName();
            System.out.println("Scanning package: " + basePackage);

            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // 🔥 Step 1: Create all @Service beans first
            for (Class<?> clazz : classes) {

                if (clazz.isAnnotationPresent(Service.class)) {

                    Object service = clazz.getDeclaredConstructor().newInstance();

                    Context.addBean(clazz, service);

                    System.out.println("Registered Service: " + clazz.getName());
                }
            }

            // 🔥 Step 2: Create controllers + inject dependencies
            for (Class<?> clazz : classes) {

                if (clazz.isAnnotationPresent(Controller.class)) {

                    Object controller = clazz.getDeclaredConstructor().newInstance();

                    Context.addBean(clazz, controller);

                    // 🔥 Inject dependencies (services will be available now)
                    Injector.inject(controller);

                    Dispatcher.register(controller);

                    System.out.println("Registered Controller: " + clazz.getName());
                }
            }

            Server.start(8080);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}