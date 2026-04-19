package flowforge.core.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties props = new Properties();

    static {
        try {
            // 1. Load base config
            loadFile("app.properties");

            System.out.println("Loaded app.properties");

            // 2. Check environment
            String env = props.getProperty("app.env");

            if (env != null && !env.isEmpty()) {

                String envFile = "app-" + env + ".properties";

                loadFile(envFile);

                System.out.println("Loaded " + envFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFile(String filename) {

        try {
            InputStream is = Config.class
                    .getClassLoader()
                    .getResourceAsStream(filename);

            if (is != null) {
                Properties temp = new Properties();
                temp.load(is);

                // Override existing values
                props.putAll(temp);
            } else {
                System.out.println("Warning " + filename + " not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Getters
    // =========================

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(props.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}