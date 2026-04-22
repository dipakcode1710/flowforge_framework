package flowforge;

import flowforge.core.annotations.*;
import flowforge.core.banner.Banner;
import flowforge.core.config.Config;
import flowforge.core.context.Context;
import flowforge.core.context.Injector;
import flowforge.core.dispatcher.Dispatcher;
import flowforge.core.scanner.ClassScanner;
import flowforge.core.server.Server;

import java.lang.reflect.Method;
import java.util.List;

public class Flow {

    public static void run(Class<?> appClass) {

        long startTime = System.currentTimeMillis();

        try {

            // =========================
            // 🔥 0. Banner
            // =========================
            Banner.print();

            String basePackage = appClass.getPackageName();
            System.out.println("🔍 Scanning package: " + basePackage);

            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // =========================
            // 1. Register Beans
            // =========================
            int beanCount = 0;

            for (Class<?> clazz : classes) {

                if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                if (isBean(clazz)) {

                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    Context.addBean(clazz, instance);
                    beanCount++;

                    System.out.println("📦 Bean Registered: " + clazz.getName());
                }
            }

            // =========================
            // 2. Inject Dependencies
            // =========================
            for (Object bean : Context.getAllBeans().values()) {
                Injector.inject(bean);
            }

            // =========================
            // 3. Register Routes
            // =========================
            int controllerCount = 0;

            for (Object bean : Context.getAllBeans().values()) {

                Class<?> clazz = bean.getClass();

                if (clazz.isAnnotationPresent(Controller.class)) {
                    Dispatcher.register(bean);
                    controllerCount++;

                    System.out.println("🚀 Controller Ready: " + clazz.getName());
                }
            }

            // =========================
            // 4. Start Server
            // =========================
            int port = Config.getInt("server.port", 8080);
            Server.start(port);

            // =========================
            // 🔥 5. Startup Summary
            // =========================
            long time = System.currentTimeMillis() - startTime;

            System.out.println("\n=================================");
            System.out.println("🚀 FlowForge Started Successfully");
            System.out.println("=================================");
            System.out.println("📦 Beans Loaded     : " + beanCount);
            System.out.println("🎯 Controllers      : " + controllerCount);
            System.out.println("🌐 Server Port      : " + port);
            System.out.println("⏱ Startup Time     : " + time + " ms");
            System.out.println("🔗 Docs UI          : http://localhost:" + port + "/dev/docs-ui");
            System.out.println("=================================\n");

        } catch (Exception e) {
            System.err.println("❌ Failed to start FlowForge");
            e.printStackTrace();
        }
    }

    private static boolean isBean(Class<?> clazz) {

        return clazz.isAnnotationPresent(flowforge.core.annotations.Component.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.Service.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.Controller.class) ||
               clazz.isAnnotationPresent(flowforge.core.annotations.ConfigurationProperties.class);
    }
}