package cn.puthmc.converter;

import cn.puthmc.db.DatabaseManager;
import cn.puthmc.model.ColumnInfo;
import cn.puthmc.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQL语法转换器
 * 处理SQLite和MySQL之间的语法差异
 */
public class SqlConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlConverter.class);
    
    // SQLite到MySQL的数据类型映射
    private static final Map<String, String> SQLITE_TO_MYSQL_TYPES = new HashMap<>();
    
    // MySQL到SQLite的数据类型映射
    private static final Map<String, String> MYSQL_TO_SQLITE_TYPES = new HashMap<>();
    
    static {
        // 初始化SQLite到MySQL的类型映射
        SQLITE_TO_MYSQL_TYPES.put("INTEGER", "INT");
        SQLITE_TO_MYSQL_TYPES.put("INT", "INT");
        SQLITE_TO_MYSQL_TYPES.put("TINYINT", "TINYINT");
        SQLITE_TO_MYSQL_TYPES.put("SMALLINT", "SMALLINT");
        SQLITE_TO_MYSQL_TYPES.put("MEDIUMINT", "MEDIUMINT");
        SQLITE_TO_MYSQL_TYPES.put("BIGINT", "BIGINT");
        SQLITE_TO_MYSQL_TYPES.put("UNSIGNED BIG INT", "BIGINT UNSIGNED");
        SQLITE_TO_MYSQL_TYPES.put("INT2", "SMALLINT");
        SQLITE_TO_MYSQL_TYPES.put("INT8", "BIGINT");
        SQLITE_TO_MYSQL_TYPES.put("TEXT", "TEXT");
        SQLITE_TO_MYSQL_TYPES.put("CLOB", "LONGTEXT");
        SQLITE_TO_MYSQL_TYPES.put("REAL", "DOUBLE");
        SQLITE_TO_MYSQL_TYPES.put("DOUBLE", "DOUBLE");
        SQLITE_TO_MYSQL_TYPES.put("DOUBLE PRECISION", "DOUBLE");
        SQLITE_TO_MYSQL_TYPES.put("FLOAT", "FLOAT");
        SQLITE_TO_MYSQL_TYPES.put("BLOB", "LONGBLOB");
        SQLITE_TO_MYSQL_TYPES.put("NUMERIC", "DECIMAL");
        SQLITE_TO_MYSQL_TYPES.put("DECIMAL", "DECIMAL");
        SQLITE_TO_MYSQL_TYPES.put("BOOLEAN", "BOOLEAN");
        SQLITE_TO_MYSQL_TYPES.put("VARCHAR", "VARCHAR");
        SQLITE_TO_MYSQL_TYPES.put("VARYING CHARACTER", "VARCHAR");
        SQLITE_TO_MYSQL_TYPES.put("NCHAR", "CHAR");
        SQLITE_TO_MYSQL_TYPES.put("NATIVE CHARACTER", "CHAR");
        SQLITE_TO_MYSQL_TYPES.put("NVARCHAR", "VARCHAR");
        SQLITE_TO_MYSQL_TYPES.put("CHAR", "CHAR");
        SQLITE_TO_MYSQL_TYPES.put("CHARACTER", "CHAR");
        SQLITE_TO_MYSQL_TYPES.put("DATETIME", "DATETIME");
        SQLITE_TO_MYSQL_TYPES.put("DATE", "DATE");
        SQLITE_TO_MYSQL_TYPES.put("TIME", "TIME");
        SQLITE_TO_MYSQL_TYPES.put("TIMESTAMP", "TIMESTAMP");
        
        // 初始化MySQL到SQLite的类型映射
        MYSQL_TO_SQLITE_TYPES.put("TINYINT", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("SMALLINT", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("MEDIUMINT", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("INT", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("INTEGER", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("BIGINT", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("BIT", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("BOOL", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("BOOLEAN", "INTEGER");
        MYSQL_TO_SQLITE_TYPES.put("FLOAT", "REAL");
        MYSQL_TO_SQLITE_TYPES.put("DOUBLE", "REAL");
        MYSQL_TO_SQLITE_TYPES.put("REAL", "REAL");
        MYSQL_TO_SQLITE_TYPES.put("DECIMAL", "NUMERIC");
        MYSQL_TO_SQLITE_TYPES.put("NUMERIC", "NUMERIC");
        MYSQL_TO_SQLITE_TYPES.put("CHAR", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("VARCHAR", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("TINYTEXT", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("TEXT", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("MEDIUMTEXT", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("LONGTEXT", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("ENUM", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("SET", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("JSON", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("BINARY", "BLOB");
        MYSQL_TO_SQLITE_TYPES.put("VARBINARY", "BLOB");
        MYSQL_TO_SQLITE_TYPES.put("TINYBLOB", "BLOB");
        MYSQL_TO_SQLITE_TYPES.put("BLOB", "BLOB");
        MYSQL_TO_SQLITE_TYPES.put("MEDIUMBLOB", "BLOB");
        MYSQL_TO_SQLITE_TYPES.put("LONGBLOB", "BLOB");
        MYSQL_TO_SQLITE_TYPES.put("DATE", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("TIME", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("DATETIME", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("TIMESTAMP", "TEXT");
        MYSQL_TO_SQLITE_TYPES.put("YEAR", "INTEGER");
    }
    
    /**
     * 生成创建表的SQL语句
     * @param tableInfo 表信息
     * @param targetType 目标数据库类型
     * @return CREATE TABLE SQL语句
     */
    public static String generateCreateTableSql(TableInfo tableInfo, DatabaseManager.DatabaseType targetType) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("CREATE TABLE ");
        if (targetType == DatabaseManager.DatabaseType.MYSQL) {
            sql.append("`").append(tableInfo.getName()).append("`");
        } else {
            sql.append("\"").append(tableInfo.getName()).append("\"");
        }
        sql.append(" (\n");
        
        // 添加列定义
        for (int i = 0; i < tableInfo.getColumns().size(); i++) {
            ColumnInfo column = tableInfo.getColumns().get(i);
            sql.append("  ").append(generateColumnDefinition(column, targetType));
            
            if (i < tableInfo.getColumns().size() - 1) {
                sql.append(",");
            }
            sql.append("\n");
        }
        
        // 添加主键约束
        if (tableInfo.hasPrimaryKey()) {
            sql.append(",\n  PRIMARY KEY (");
            boolean first = true;
            for (ColumnInfo column : tableInfo.getPrimaryKeyColumns()) {
                if (!first) sql.append(", ");
                if (targetType == DatabaseManager.DatabaseType.MYSQL) {
                    sql.append("`").append(column.getName()).append("`");
                } else {
                    sql.append("\"").append(column.getName()).append("\"");
                }
                first = false;
            }
            sql.append(")\n");
        }
        
        sql.append(")");
        
        // 添加MySQL特有的表选项
        if (targetType == DatabaseManager.DatabaseType.MYSQL) {
            sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }
        
        sql.append(";");
        
        logger.debug("生成创建表SQL: {}", tableInfo.getName());
        return sql.toString();
    }
    
    /**
     * 生成列定义
     * @param column 列信息
     * @param targetType 目标数据库类型
     * @return 列定义SQL
     */
    private static String generateColumnDefinition(ColumnInfo column, DatabaseManager.DatabaseType targetType) {
        StringBuilder sql = new StringBuilder();
        
        // 列名
        if (targetType == DatabaseManager.DatabaseType.MYSQL) {
            sql.append("`").append(column.getName()).append("`");
        } else {
            sql.append("\"").append(column.getName()).append("\"");
        }
        
        // 数据类型
        sql.append(" ").append(convertDataType(column.getDataType(), column.getSize(), targetType));
        
        // NULL约束
        if (!column.isNullable()) {
            sql.append(" NOT NULL");
        }
        
        // 自增
        if (column.isAutoIncrement()) {
            if (targetType == DatabaseManager.DatabaseType.MYSQL) {
                sql.append(" AUTO_INCREMENT");
            } else {
                // SQLite的自增在主键定义中处理
                if (column.isPrimaryKey()) {
                    sql.append(" PRIMARY KEY AUTOINCREMENT");
                }
            }
        }
        
        // 默认值
        if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
            String defaultValue = convertDefaultValue(column.getDefaultValue(), targetType);
            sql.append(" DEFAULT ").append(defaultValue);
        }
        
        // 注释
        if (column.getComment() != null && !column.getComment().isEmpty() && 
            targetType == DatabaseManager.DatabaseType.MYSQL) {
            sql.append(" COMMENT '").append(escapeString(column.getComment())).append("'");
        }
        
        return sql.toString();
    }
    
    /**
     * 转换数据类型
     * @param sourceType 源数据类型
     * @param size 大小
     * @param targetType 目标数据库类型
     * @return 转换后的数据类型
     */
    public static String convertDataType(String sourceType, int size, DatabaseManager.DatabaseType targetType) {
        if (sourceType == null) {
            return "TEXT";
        }
        
        String upperType = sourceType.toUpperCase();
        
        // 移除括号中的内容进行匹配
        String baseType = upperType.replaceAll("\\([^)]*\\)", "");
        
        String convertedType;
        if (targetType == DatabaseManager.DatabaseType.MYSQL) {
            convertedType = SQLITE_TO_MYSQL_TYPES.getOrDefault(baseType, sourceType);
        } else {
            convertedType = MYSQL_TO_SQLITE_TYPES.getOrDefault(baseType, sourceType);
        }
        
        // 处理大小参数
        if (size > 0 && needsSize(convertedType, targetType)) {
            if (targetType == DatabaseManager.DatabaseType.MYSQL) {
                // MySQL的TEXT类型不需要大小参数
                if (!"TEXT".equals(convertedType)) {
                    convertedType += "(" + size + ")";
                }
            } else {
                // SQLite通常不需要大小参数
            }
        }
        
        return convertedType;
    }
    
    /**
     * 检查数据类型是否需要大小参数
     * @param dataType 数据类型
     * @param targetType 目标数据库类型
     * @return 是否需要大小参数
     */
    private static boolean needsSize(String dataType, DatabaseManager.DatabaseType targetType) {
        if (targetType == DatabaseManager.DatabaseType.MYSQL) {
            return "VARCHAR".equals(dataType) || "CHAR".equals(dataType) || 
                   "DECIMAL".equals(dataType) || "NUMERIC".equals(dataType);
        }
        return false;
    }
    
    /**
     * 转换默认值
     * @param defaultValue 默认值
     * @param targetType 目标数据库类型
     * @return 转换后的默认值
     */
    private static String convertDefaultValue(String defaultValue, DatabaseManager.DatabaseType targetType) {
        if (defaultValue == null || defaultValue.isEmpty()) {
            return "NULL";
        }
        
        // 处理特殊的默认值
        String upper = defaultValue.toUpperCase();
        
        if ("CURRENT_TIMESTAMP".equals(upper) || "NOW()".equals(upper)) {
            return targetType == DatabaseManager.DatabaseType.MYSQL ? "CURRENT_TIMESTAMP" : "CURRENT_TIMESTAMP";
        }
        
        // 如果是数字，直接返回
        if (defaultValue.matches("-?\\d+(\\.\\d+)?")) {
            return defaultValue;
        }
        
        // 如果是布尔值
        if ("true".equalsIgnoreCase(defaultValue) || "1".equals(defaultValue)) {
            return targetType == DatabaseManager.DatabaseType.MYSQL ? "1" : "1";
        }
        if ("false".equalsIgnoreCase(defaultValue) || "0".equals(defaultValue)) {
            return targetType == DatabaseManager.DatabaseType.MYSQL ? "0" : "0";
        }
        
        // 字符串值需要加引号
        return "'" + escapeString(defaultValue) + "'";
    }
    
    /**
     * 转义字符串中的特殊字符
     * @param str 原字符串
     * @return 转义后的字符串
     */
    private static String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''").replace("\\", "\\\\");
    }
    
    /**
     * 生成插入数据的SQL语句
     * @param tableName 表名
     * @param columns 列信息
     * @param targetType 目标数据库类型
     * @return INSERT SQL语句模板
     */
    public static String generateInsertSql(String tableName, java.util.List<ColumnInfo> columns, 
                                          DatabaseManager.DatabaseType targetType) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("INSERT INTO ");
        if (targetType == DatabaseManager.DatabaseType.MYSQL) {
            sql.append("`").append(tableName).append("`");
        } else {
            sql.append("\"").append(tableName).append("\"");
        }
        
        sql.append(" (");
        
        // 添加列名
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            
            if (targetType == DatabaseManager.DatabaseType.MYSQL) {
                sql.append("`").append(columns.get(i).getName()).append("`");
            } else {
                sql.append("\"").append(columns.get(i).getName()).append("\"");
            }
        }
        
        sql.append(") VALUES (");
        
        // 添加参数占位符
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        
        sql.append(")");
        
        return sql.toString();
    }
}