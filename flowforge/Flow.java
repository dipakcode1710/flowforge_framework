package flowforge;

import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.server.Server;
import example.UserController;

public class Flow {

    public static void run(Class<?> appClass) {
        try {

            // Register controller
            UserController controller = new UserController();
            Dispatcher.register(controller);

            // Start server
            Server.start(8080);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}