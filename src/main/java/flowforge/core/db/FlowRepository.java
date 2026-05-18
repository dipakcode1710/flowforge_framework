package flowforge.core.db;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class FlowRepository<T, ID> {

    public final Class<T> entityClass;
    protected final EntityMeta meta;

    @SuppressWarnings("unchecked")
    protected FlowRepository() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) pt.getActualTypeArguments()[0];
        this.meta = EntityMeta.of(entityClass);
    }

    // -----------------------------------------------------------------------
    // CRUD
    // -----------------------------------------------------------------------

    public T findById(ID id) {
        String sql = "SELECT * FROM `" + meta.tableName + "` WHERE `" + meta.idColumn + "` = ?";
        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        } finally {
            ConnectionPool.release(conn);
        }
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM `" + meta.tableName + "`";
        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                List<T> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll failed: " + e.getMessage(), e);
        } finally {
            ConnectionPool.release(conn);
        }
    }

    public T save(T entity) {
        try {
            Object idValue = meta.idField.get(entity);
            boolean isNew = idValue == null || (idValue instanceof Number n && n.longValue() == 0);
            return isNew ? insert(entity) : update(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("save failed", e);
        }
    }

    public void deleteById(ID id) {
        String sql = "DELETE FROM `" + meta.tableName + "` WHERE `" + meta.idColumn + "` = ?";
        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("deleteById failed: " + e.getMessage(), e);
        } finally {
            ConnectionPool.release(conn);
        }
    }

    public void delete(T entity) {
        try {
            @SuppressWarnings("unchecked")
            ID id = (ID) meta.idField.get(entity);
            deleteById(id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("delete failed", e);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM `" + meta.tableName + "`";
        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("count failed: " + e.getMessage(), e);
        } finally {
            ConnectionPool.release(conn);
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private T insert(T entity) throws IllegalAccessException {
        List<EntityMeta.ColumnInfo> cols = meta.columns;
        String colNames = cols.stream().map(c -> "`" + c.columnName() + "`").collect(Collectors.joining(", "));
        String placeholders = cols.stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO `" + meta.tableName + "` (" + colNames + ") VALUES (" + placeholders + ")";

        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < cols.size(); i++) {
                    ps.setObject(i + 1, cols.get(i).field().get(entity));
                }
                ps.executeUpdate();
                if (meta.idAutoIncrement) {
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) meta.idField.set(entity, castId(keys.getObject(1)));
                }
                return entity;
            }
        } catch (SQLException e) {
            throw new RuntimeException("insert failed: " + e.getMessage(), e);
        } finally {
            ConnectionPool.release(conn);
        }
    }

    private T update(T entity) throws IllegalAccessException {
        List<EntityMeta.ColumnInfo> cols = meta.columns;
        String sets = cols.stream().map(c -> "`" + c.columnName() + "` = ?").collect(Collectors.joining(", "));
        String sql = "UPDATE `" + meta.tableName + "` SET " + sets + " WHERE `" + meta.idColumn + "` = ?";

        Connection conn = null;
        try {
            conn = ConnectionPool.borrow();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < cols.size(); i++) {
                    ps.setObject(i + 1, cols.get(i).field().get(entity));
                }
                ps.setObject(cols.size() + 1, meta.idField.get(entity));
                ps.executeUpdate();
                return entity;
            }
        } catch (SQLException e) {
            throw new RuntimeException("update failed: " + e.getMessage(), e);
        } finally {
            ConnectionPool.release(conn);
        }
    }

    private T mapRow(ResultSet rs) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            if (meta.idField != null) {
                meta.idField.set(entity, castTo(meta.idField.getType(), rs.getObject(meta.idColumn)));
            }
            for (EntityMeta.ColumnInfo col : meta.columns) {
                col.field().set(entity, castTo(col.field().getType(), rs.getObject(col.columnName())));
            }
            return entity;
        } catch (Exception e) {
            throw new SQLException("Row mapping failed: " + e.getMessage(), e);
        }
    }

    private Object castTo(Class<?> type, Object value) {
        if (value == null) return null;
        if (type == int.class || type == Integer.class)     return ((Number) value).intValue();
        if (type == long.class || type == Long.class)       return ((Number) value).longValue();
        if (type == double.class || type == Double.class)   return ((Number) value).doubleValue();
        if (type == float.class || type == Float.class)     return ((Number) value).floatValue();
        if (type == boolean.class || type == Boolean.class) return ((Number) value).intValue() != 0;
        return value;
    }

    private Object castId(Object value) {
        Class<?> type = meta.idField.getType();
        if (type == int.class || type == Integer.class) return ((Number) value).intValue();
        if (type == long.class || type == Long.class)   return ((Number) value).longValue();
        return value;
    }
}
