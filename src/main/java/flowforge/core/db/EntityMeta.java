package flowforge.core.db;

import flowforge.core.db.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityMeta {

    public final Class<?> entityClass;
    public final String tableName;
    public final Field idField;
    public final String idColumn;
    public final boolean idAutoIncrement;
    public final List<ColumnInfo> columns;

    public record ColumnInfo(Field field, String columnName, boolean nullable, int length) {}

    private static final Map<Class<?>, EntityMeta> cache = new ConcurrentHashMap<>();

    private EntityMeta(Class<?> clazz) {
        this.entityClass = clazz;
        this.tableName = resolveTableName(clazz);

        Field idF = null;
        String idCol = null;
        boolean idAuto = true;
        List<ColumnInfo> cols = new ArrayList<>();

        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.isAnnotationPresent(Ignore.class)) continue;
            if (Modifier.isStatic(f.getModifiers())) continue;

            if (f.isAnnotationPresent(Id.class)) {
                idF = f;
                idCol = resolveColumnName(f);
                idAuto = f.getAnnotation(Id.class).autoIncrement();
            } else {
                Column col = f.getAnnotation(Column.class);
                boolean nullable = col == null || col.nullable();
                int length = col != null ? col.length() : 255;
                cols.add(new ColumnInfo(f, resolveColumnName(f), nullable, length));
            }
        }

        this.idField = idF;
        this.idColumn = idCol;
        this.idAutoIncrement = idAuto;
        this.columns = Collections.unmodifiableList(cols);
    }

    public static EntityMeta of(Class<?> clazz) {
        return cache.computeIfAbsent(clazz, EntityMeta::new);
    }

    public String toSqlType(Field f) {
        Class<?> t = f.getType();
        Column col = f.getAnnotation(Column.class);
        int length = col != null ? col.length() : 255;

        if (t == String.class)                                    return "VARCHAR(" + length + ")";
        if (t == int.class || t == Integer.class)                 return "INT";
        if (t == long.class || t == Long.class)                   return "BIGINT";
        if (t == double.class || t == Double.class)               return "DOUBLE";
        if (t == float.class || t == Float.class)                 return "FLOAT";
        if (t == boolean.class || t == Boolean.class)             return "TINYINT(1)";
        if (t == java.time.LocalDateTime.class
         || t == java.util.Date.class)                            return "DATETIME";
        if (t == java.time.LocalDate.class)                       return "DATE";
        if (t == byte[].class)                                    return "BLOB";
        return "TEXT";
    }

    private static String resolveTableName(Class<?> clazz) {
        Table t = clazz.getAnnotation(Table.class);
        if (t != null && !t.name().isEmpty()) return t.name();
        return toSnakeCase(clazz.getSimpleName());
    }

    private static String resolveColumnName(Field f) {
        Column col = f.getAnnotation(Column.class);
        if (col != null && !col.name().isEmpty()) return col.name();
        return toSnakeCase(f.getName());
    }

    static String toSnakeCase(String name) {
        return name.replaceAll("([A-Z])", "_$1").toLowerCase().replaceAll("^_", "");
    }
}
