package flowforge.core.scanner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    public static List<Class<?>> scan(String packageName) {

        List<Class<?>> classes = new ArrayList<>();

        try {
            String path = packageName.replace(".", "/");

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);

            if (resource == null) return classes;

            File directory = new File(resource.getFile());

            for (File file : directory.listFiles()) {

                if (file.getName().endsWith(".class")) {

                    String className = packageName + "."
                            + file.getName().replace(".class", "");

                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }
}