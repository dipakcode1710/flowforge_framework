package flowforge;

import flowforge.core.annotations.Controller;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.server.Server;
import flowforge.core.scanner.ClassScanner;

import java.util.List;

public class Flow {

    public static void run(Class<?> appClass) {
        try {

            // 🔥 Get base package (e.g., "example")
            String basePackage = appClass.getPackageName();
            System.out.println("Scanning package: " + basePackage);

            // 🔥 Scan all classes in package
            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // 🔥 Find controllers automatically
            for (Class<?> clazz : classes) {

                if (clazz.isAnnotationPresent(Controller.class)) {

                    Object controller = clazz.getDeclaredConstructor().newInstance();

                    Dispatcher.register(controller);

                    System.out.println("Registered Controller: " + clazz.getName());
                }
            }

            // 🔥 Start server
            Server.start(8080);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}