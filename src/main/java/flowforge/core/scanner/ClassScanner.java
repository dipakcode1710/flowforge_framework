package flowforge.core.scanner;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    public static List<Class<?>> scan(String packageName) {

        List<Class<?>> classes = new ArrayList<>();

        try {
            String path = packageName.replace(".", "/");

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {

                URL resource = resources.nextElement();

                String protocol = resource.getProtocol();

                if (protocol.equals("file")) {
                    // ✅ IDE / file system
                    classes.addAll(scanDirectory(resource, packageName));
                }

                else if (protocol.equals("jar")) {
                    // ✅ JAR support
                    classes.addAll(scanJar(resource, path));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    // 🔥 Scan file system
    private static List<Class<?>> scanDirectory(URL resource, String packageName) {

        List<Class<?>> classes = new ArrayList<>();

        try {
            File directory = new File(resource.toURI());

            for (File file : Objects.requireNonNull(directory.listFiles())) {

                if (file.getName().endsWith(".class")) {

                    String className = packageName + "."
                            + file.getName().replace(".class", "");

                    classes.add(Class.forName(className));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    // 🔥 Scan JAR
    private static List<Class<?>> scanJar(URL resource, String path) {

        List<Class<?>> classes = new ArrayList<>();

        try {
            String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));

            JarFile jarFile = new JarFile(jarPath);

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();

                String name = entry.getName();

                if (name.startsWith(path) && name.endsWith(".class")) {

                    String className = name
                            .replace("/", ".")
                            .replace(".class", "");

                    classes.add(Class.forName(className));
                }
            }

            jarFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }
}