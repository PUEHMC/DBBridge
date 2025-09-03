package cn.puthmc.db;

import cn.puthmc.model.ColumnInfo;
import cn.puthmc.model.IndexInfo;
import cn.puthmc.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库结构分析器
 * 用于分析SQLite和MySQL数据库的表结构、索引、约束等信息
 */
public class SchemaAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaAnalyzer.class);
    
    /**
     * 获取数据库中所有表的信息
     * @param connection 数据库连接
     * @return 表信息列表
     */
    public static List<TableInfo> getAllTables(Connection connection) throws SQLException {
        List<TableInfo> tables = new ArrayList<>();
        DatabaseManager.DatabaseType dbType = DatabaseManager.getDatabaseType(connection);
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取表列表
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    
                    // 跳过系统表
                    if (isSystemTable(tableName, dbType)) {
                        continue;
                    }
                    
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setName(tableName);
                    tableInfo.setComment(rs.getString("REMARKS"));
                    
                    // 获取列信息
                    tableInfo.setColumns(getTableColumns(connection, tableName));
                    
                    // 获取索引信息
                    tableInfo.setIndexes(getTableIndexes(connection, tableName));
                    
                    // 获取行数
                    tableInfo.setRowCount(getTableRowCount(connection, tableName));
                    
                    tables.add(tableInfo);
                    logger.debug("分析表: {} ({}列, {}行)", tableName, 
                               tableInfo.getColumns().size(), tableInfo.getRowCount());
                }
            }
            
            logger.info("成功分析 {} 个表", tables.size());
            return tables;
            
        } catch (SQLException e) {
            logger.error("分析数据库结构时发生错误", e);
            throw e;
        }
    }
    
    /**
     * 获取表的列信息
     * @param connection 数据库连接
     * @param tableName 表名
     * @return 列信息列表
     */
    public static List<ColumnInfo> getTableColumns(Connection connection, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        DatabaseManager.DatabaseType dbType = DatabaseManager.getDatabaseType(connection);
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取主键信息
            List<String> primaryKeys = new ArrayList<>();
            try (ResultSet pkRs = metaData.getPrimaryKeys(null, null, tableName)) {
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            }
            
            // 获取列信息
            try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setName(rs.getString("COLUMN_NAME"));
                    column.setDataType(rs.getString("TYPE_NAME"));
                    column.setSize(rs.getInt("COLUMN_SIZE"));
                    column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.setDefaultValue(rs.getString("COLUMN_DEF"));
                    column.setComment(rs.getString("REMARKS"));
                    column.setPrimaryKey(primaryKeys.contains(column.getName()));
                    
                    // 检查是否为自增列
                    if (dbType == DatabaseManager.DatabaseType.SQLITE) {
                        column.setAutoIncrement(checkSQLiteAutoIncrement(connection, tableName, column.getName()));
                    } else {
                        column.setAutoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")));
                    }
                    
                    columns.add(column);
                }
            }
            
            return columns;
            
        } catch (SQLException e) {
            logger.error("获取表 {} 的列信息时发生错误", tableName, e);
            throw e;
        }
    }
    
    /**
     * 获取表的索引信息
     * @param connection 数据库连接
     * @param tableName 表名
     * @return 索引信息列表
     */
    public static List<IndexInfo> getTableIndexes(Connection connection, String tableName) throws SQLException {
        List<IndexInfo> indexes = new ArrayList<>();
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    
                    // 跳过主键索引
                    if (indexName == null || "PRIMARY".equals(indexName)) {
                        continue;
                    }
                    
                    IndexInfo index = new IndexInfo();
                    index.setName(indexName);
                    index.setUnique(!rs.getBoolean("NON_UNIQUE"));
                    index.setColumnName(rs.getString("COLUMN_NAME"));
                    index.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
                    
                    indexes.add(index);
                }
            }
            
            return indexes;
            
        } catch (SQLException e) {
            logger.error("获取表 {} 的索引信息时发生错误", tableName, e);
            throw e;
        }
    }
    
    /**
     * 获取表的行数
     * @param connection 数据库连接
     * @param tableName 表名
     * @return 行数
     */
    public static long getTableRowCount(Connection connection, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + escapeTableName(tableName);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
        } catch (SQLException e) {
            logger.warn("获取表 {} 的行数时发生错误: {}", tableName, e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * 检查是否为系统表
     * @param tableName 表名
     * @param dbType 数据库类型
     * @return 是否为系统表
     */
    private static boolean isSystemTable(String tableName, DatabaseManager.DatabaseType dbType) {
        if (dbType == DatabaseManager.DatabaseType.SQLITE) {
            return tableName.startsWith("sqlite_");
        } else if (dbType == DatabaseManager.DatabaseType.MYSQL) {
            return tableName.startsWith("information_schema") || 
                   tableName.startsWith("performance_schema") ||
                   tableName.startsWith("mysql") ||
                   tableName.startsWith("sys");
        }
        return false;
    }
    
    /**
     * 检查SQLite列是否为自增列
     * @param connection 数据库连接
     * @param tableName 表名
     * @param columnName 列名
     * @return 是否为自增列
     */
    private static boolean checkSQLiteAutoIncrement(Connection connection, String tableName, String columnName) {
        String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String createSql = rs.getString("sql");
                    return createSql != null && 
                           createSql.toUpperCase().contains(columnName.toUpperCase() + " INTEGER PRIMARY KEY AUTOINCREMENT");
                }
            }
            
        } catch (SQLException e) {
            logger.warn("检查SQLite自增列时发生错误", e);
        }
        
        return false;
    }
    
    /**
     * 转义表名
     * @param tableName 表名
     * @return 转义后的表名
     */
    private static String escapeTableName(String tableName) {
        return "`" + tableName + "`";
    }
}