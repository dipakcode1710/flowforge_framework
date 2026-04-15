package flowforge.core.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties props = new Properties();

    static {
        try {
            InputStream is = Config.class
                    .getClassLoader()
                    .getResourceAsStream("app.properties");

            if (is != null) {
                props.load(is);
                System.out.println("✅ Loaded app.properties");
            } else {
                System.out.println("⚠️ app.properties not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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