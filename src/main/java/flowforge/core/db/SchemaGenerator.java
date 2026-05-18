package flowforge.core.db;

import java.sql.*;

public class SchemaGenerator {

    public static void run(Class<?> entityClass, String ddlAuto) {
        if ("none".equalsIgnoreCase(ddlAuto)) return;

        EntityMeta meta = EntityMeta.of(entityClass);
        if (meta.idField == null) {
            System.err.println("[DB] Skipping schema for " + entityClass.getSimpleName() + ": no @Id field found");
            return;
        }

        String sql = buildCreateSQL(meta);
        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            conn.createStatement().execute(sql);
            System.out.println("[DB] Table ready: " + meta.tableName);
        } catch (SQLException e) {
            System.err.println("[DB] Schema error for " + meta.tableName + ": " + e.getMessage());
        } finally {
            ConnectionPool.release(conn);
        }
    }

    private static String buildCreateSQL(EntityMeta meta) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(meta.tableName).append("` (\n");

        // Primary key column
        sb.append("  `").append(meta.idColumn).append("` ")
          .append(meta.toSqlType(meta.idField));
        if (meta.idAutoIncrement) sb.append(" AUTO_INCREMENT");
        sb.append(" PRIMARY KEY");

        // Other columns
        for (EntityMeta.ColumnInfo col : meta.columns) {
            sb.append(",\n  `").append(col.columnName()).append("` ")
              .append(meta.toSqlType(col.field()));
            if (!col.nullable()) sb.append(" NOT NULL");
        }

        sb.append("\n)");
        return sb.toString();
    }
}
