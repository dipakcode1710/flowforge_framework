package flowforge.core.db;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPool {

    private static DataSourceConfig config;
    private static int maxSize = 10;
    private static final LinkedBlockingQueue<Connection> pool = new LinkedBlockingQueue<>();

    public static void init(DataSourceConfig cfg, int max) throws SQLException {
        config = cfg;
        maxSize = max;
        // Validate connection on startup
        Connection test = DriverManager.getConnection(cfg.url, cfg.username, cfg.password);
        pool.offer(test);
    }

    public static Connection borrow() throws SQLException {
        Connection conn = pool.poll();
        if (conn != null) {
            try {
                if (conn.isValid(1)) return conn;
            } catch (SQLException ignored) {}
        }
        return DriverManager.getConnection(config.url, config.username, config.password);
    }

    public static void release(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed() && pool.size() < maxSize) {
                pool.offer(conn);
            } else {
                conn.close();
            }
        } catch (SQLException ignored) {}
    }

    public static boolean isInitialized() {
        return config != null;
    }
}
