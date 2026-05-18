package flowforge;

import flowforge.core.annotations.*;
import flowforge.core.banner.Banner;
import flowforge.core.config.Config;
import flowforge.core.context.Context;
import flowforge.core.context.Injector;
import flowforge.core.db.ConnectionPool;
import flowforge.core.db.DataSourceConfig;
import flowforge.core.db.FlowRepository;
import flowforge.core.db.SchemaGenerator;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.scanner.ClassScanner;
import flowforge.core.server.Server;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The Flow class is the main entry point for bootstrapping a FlowForge application.
 *
 * It performs the full application lifecycle: scanning the classpath for components,
 * registering beans, injecting dependencies, registering routes, and starting the embedded
 * HTTP server.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
public class Flow {

    /**
     * Runs the FlowForge application starting from the given application class.
     *
     * This method:
     *   0. Prints the startup banner
     *   1. Scans the base package for component classes
     *   2. Registers all beans in the application context
     *   3. Injects dependencies into registered beans
     *   4. Registers controllers and their routes with the dispatcher
     *   5. Starts the embedded HTTP server on the configured port
     *
     * @param appClass the application class used to determine the base package for scanning
     */
    public static void run(Class<?> appClass) {

        // 🔥 Enforce @App on the entry point class
        if (!appClass.isAnnotationPresent(flowforge.core.annotations.App.class)) {
            throw new IllegalArgumentException(
                "Class '" + appClass.getName() + "' must be annotated with @App to run FlowForge."
            );
        }

        long startTime = System.currentTimeMillis();
        try {
            // 0. Banner
            Banner.print();

            String basePackage = appClass.getPackageName();
            System.out.println("Scanning package: " + basePackage);
            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // 1. Register Beans
            int beanCount = 0;
            for (Class<?> clazz : classes) {
                if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                if (isBean(clazz)) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    Context.addBean(clazz, instance);
                    beanCount++;
                    System.out.println("Bean Registered: " + clazz.getName());
                }
            }

            // 2. Inject Dependencies
            for (Object bean : Context.getAllBeans().values()) {
                Injector.inject(bean);
            }

            // 3. Initialize Database (if datasource is configured)
            DataSourceConfig dsConfig = DataSourceConfig.load();
            if (dsConfig != null) {
                int poolSize = Config.getInt("datasource.pool-size", 10);
                ConnectionPool.init(dsConfig, poolSize);
                System.out.println("[DB] Connected: " + dsConfig.url);
                for (Object bean : Context.getAllBeans().values()) {
                    if (bean instanceof FlowRepository<?, ?> repo) {
                        SchemaGenerator.run(repo.entityClass, dsConfig.ddlAuto);
                    }
                }
            }

            // 4. Register Routes
            int controllerCount = 0;
            for (Object bean : Context.getAllBeans().values()) {
                Class<?> clazz = bean.getClass();
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Dispatcher.register(bean);
                    controllerCount++;
                    System.out.println("Controller Ready: " + clazz.getName());
                }
            }

            // 5. Start Server
            int port = Config.getInt("server.port", 8080);
            Server.start(port);

            // 6. Startup Summary
            long time = System.currentTimeMillis() - startTime;
            System.out.println("\n=================================");
            System.out.println("FlowForge Started Successfully");
            System.out.println("=================================");
            System.out.println("Beans Loaded     : " + beanCount);
            System.out.println("Controllers      : " + controllerCount);
            System.out.println("Server Port      : " + port);
            System.out.println("Startup Time     : " + time + " ms");
            System.out.println("Docs UI          : http://localhost:" + port + "/dev/docs-ui");
            System.out.println("=================================\n");

        } catch (Exception e) {
            System.err.println("Failed to start FlowForge");
            e.printStackTrace();
        }
    }

    /**
     * Determines whether the given class should be registered as a bean in the application context.
     *
     * A class is considered a bean if it is annotated with any of the following:
     * Component, Service, Controller, or ConfigurationProperties.
     *
     * @param clazz the class to check
     * @return true if the class is annotated as a bean, false otherwise
     */
    private static boolean isBean(Class<?> clazz) {
        return clazz.isAnnotationPresent(flowforge.core.annotations.Component.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.Service.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.Controller.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.ConfigurationProperties.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.Repository.class);
    }
}