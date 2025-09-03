package cn.puthmc.converter;

import cn.puthmc.db.DatabaseManager;
import cn.puthmc.db.SchemaAnalyzer;
import cn.puthmc.model.ColumnInfo;
import cn.puthmc.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 数据迁移器
 * 负责在SQLite和MySQL之间迁移数据
 */
public class DataMigrator {
    
    private static final Logger logger = LoggerFactory.getLogger(DataMigrator.class);
    
    private static final int BATCH_SIZE = 1000; // 批处理大小
    private static final int COMMIT_INTERVAL = 5000; // 提交间隔
    
    private volatile boolean cancelled = false;
    
    /**
     * 迁移进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(String message, double progress);
        void onTableStart(String tableName, long totalRows);
        void onTableComplete(String tableName, long migratedRows);
        void onError(String message, Exception error);
    }
    
    /**
     * 执行数据库迁移
     * @param sourceConn 源数据库连接
     * @param targetConn 目标数据库连接
     * @param callback 进度回调
     * @return 迁移结果
     */
    public MigrationResult migrate(Connection sourceConn, Connection targetConn, ProgressCallback callback) {
        MigrationResult result = new MigrationResult();
        
        try {
            callback.onProgress("开始分析源数据库结构...", 0.0);
            
            // 获取源数据库类型
            DatabaseManager.DatabaseType sourceType = DatabaseManager.getDatabaseType(sourceConn);
            DatabaseManager.DatabaseType targetType = DatabaseManager.getDatabaseType(targetConn);
            
            logger.info("开始数据迁移: {} -> {}", sourceType, targetType);
            
            // 分析源数据库结构
            List<TableInfo> tables = SchemaAnalyzer.getAllTables(sourceConn);
            result.setTotalTables(tables.size());
            
            if (tables.isEmpty()) {
                callback.onProgress("源数据库中没有找到表", 1.0);
                return result;
            }
            
            callback.onProgress(String.format("找到 %d 个表，开始创建表结构...", tables.size()), 0.1);
            
            // 关闭自动提交
            targetConn.setAutoCommit(false);
            
            // 创建表结构
            createTables(tables, targetConn, targetType, callback);
            
            callback.onProgress("表结构创建完成，开始迁移数据...", 0.2);
            
            // 迁移数据
            migrateData(sourceConn, targetConn, tables, sourceType, targetType, callback, result);
            
            // 提交事务
            targetConn.commit();
            
            result.setSuccess(true);
            callback.onProgress("数据迁移完成！", 1.0);
            
            logger.info("数据迁移完成: {} 个表, {} 行数据", result.getMigratedTables(), result.getTotalRows());
            
        } catch (Exception e) {
            logger.error("数据迁移失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            callback.onError("迁移失败: " + e.getMessage(), e);
            
            // 回滚事务
            try {
                targetConn.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("回滚事务失败", rollbackEx);
            }
        } finally {
            try {
                targetConn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.warn("恢复自动提交模式失败", e);
            }
        }
        
        return result;
    }
    
    /**
     * 创建表结构
     * @param tables 表信息列表
     * @param targetConn 目标数据库连接
     * @param targetType 目标数据库类型
     * @param callback 进度回调
     */
    private void createTables(List<TableInfo> tables, Connection targetConn, 
                             DatabaseManager.DatabaseType targetType, ProgressCallback callback) throws SQLException {
        
        for (int i = 0; i < tables.size(); i++) {
            if (cancelled) {
                throw new RuntimeException("迁移已被取消");
            }
            
            TableInfo table = tables.get(i);
            String createSql = SqlConverter.generateCreateTableSql(table, targetType);
            
            try (Statement stmt = targetConn.createStatement()) {
                // 先删除已存在的表
                String dropSql = "DROP TABLE IF EXISTS " + 
                    (targetType == DatabaseManager.DatabaseType.MYSQL ? 
                     "`" + table.getName() + "`" : 
                     "\"" + table.getName() + "\"");
                stmt.execute(dropSql);
                
                // 创建新表
                stmt.execute(createSql);
                
                logger.debug("创建表: {}", table.getName());
                
                double progress = 0.1 + (0.1 * (i + 1) / tables.size());
                callback.onProgress(String.format("创建表: %s (%d/%d)", table.getName(), i + 1, tables.size()), progress);
            }
        }
    }
    
    /**
     * 迁移数据
     * @param sourceConn 源数据库连接
     * @param targetConn 目标数据库连接
     * @param tables 表信息列表
     * @param sourceType 源数据库类型
     * @param targetType 目标数据库类型
     * @param callback 进度回调
     * @param result 迁移结果
     */
    private void migrateData(Connection sourceConn, Connection targetConn, List<TableInfo> tables,
                           DatabaseManager.DatabaseType sourceType, DatabaseManager.DatabaseType targetType,
                           ProgressCallback callback, MigrationResult result) throws SQLException {
        
        AtomicLong totalMigratedRows = new AtomicLong(0);
        
        for (int i = 0; i < tables.size(); i++) {
            if (cancelled) {
                throw new RuntimeException("迁移已被取消");
            }
            
            final int tableIndex = i;
            TableInfo table = tables.get(tableIndex);
            
            if (table.getRowCount() == 0) {
                logger.debug("跳过空表: {}", table.getName());
                result.incrementMigratedTables();
                continue;
            }
            
            callback.onTableStart(table.getName(), table.getRowCount());
            
            long migratedRows = migrateTableData(sourceConn, targetConn, table, sourceType, targetType, 
                                               (rowCount) -> {
                                                   double tableProgress = 0.2 + (0.8 * (tableIndex + (double)rowCount / table.getRowCount()) / tables.size());
                                                   callback.onProgress(String.format("迁移表 %s: %d/%d 行", 
                                                                     table.getName(), rowCount, table.getRowCount()), tableProgress);
                                               });
            
            totalMigratedRows.addAndGet(migratedRows);
            result.incrementMigratedTables();
            result.addRows(migratedRows);
            
            callback.onTableComplete(table.getName(), migratedRows);
            
            logger.info("完成表 {} 的数据迁移: {} 行", table.getName(), migratedRows);
        }
    }
    
    /**
     * 迁移单个表的数据
     * @param sourceConn 源数据库连接
     * @param targetConn 目标数据库连接
     * @param table 表信息
     * @param sourceType 源数据库类型
     * @param targetType 目标数据库类型
     * @param progressCallback 进度回调
     * @return 迁移的行数
     */
    private long migrateTableData(Connection sourceConn, Connection targetConn, TableInfo table,
                                DatabaseManager.DatabaseType sourceType, DatabaseManager.DatabaseType targetType,
                                Consumer<Long> progressCallback) throws SQLException {
        
        String selectSql = generateSelectSql(table, sourceType);
        String insertSql = SqlConverter.generateInsertSql(table.getName(), table.getColumns(), targetType);
        
        long migratedRows = 0;
        
        try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectSql);
             PreparedStatement insertStmt = targetConn.prepareStatement(insertSql);
             ResultSet rs = selectStmt.executeQuery()) {
            
            int batchCount = 0;
            
            while (rs.next() && !cancelled) {
                // 设置插入参数
                for (int i = 0; i < table.getColumns().size(); i++) {
                    ColumnInfo column = table.getColumns().get(i);
                    Object value = rs.getObject(i + 1);
                    
                    // 处理数据类型转换
                    value = convertValue(value, column, targetType);
                    
                    insertStmt.setObject(i + 1, value);
                }
                
                insertStmt.addBatch();
                batchCount++;
                migratedRows++;
                
                // 批量执行
                if (batchCount >= BATCH_SIZE) {
                    insertStmt.executeBatch();
                    insertStmt.clearBatch();
                    batchCount = 0;
                    
                    // 定期提交
                    if (migratedRows % COMMIT_INTERVAL == 0) {
                        targetConn.commit();
                    }
                    
                    progressCallback.accept(migratedRows);
                }
            }
            
            // 执行剩余的批处理
            if (batchCount > 0) {
                insertStmt.executeBatch();
            }
        }
        
        return migratedRows;
    }
    
    /**
     * 生成查询SQL
     * @param table 表信息
     * @param sourceType 源数据库类型
     * @return 查询SQL
     */
    private String generateSelectSql(TableInfo table, DatabaseManager.DatabaseType sourceType) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        for (int i = 0; i < table.getColumns().size(); i++) {
            if (i > 0) sql.append(", ");
            
            String columnName = table.getColumns().get(i).getName();
            if (sourceType == DatabaseManager.DatabaseType.MYSQL) {
                sql.append("`").append(columnName).append("`");
            } else {
                sql.append("\"").append(columnName).append("\"");
            }
        }
        
        sql.append(" FROM ");
        if (sourceType == DatabaseManager.DatabaseType.MYSQL) {
            sql.append("`").append(table.getName()).append("`");
        } else {
            sql.append("\"").append(table.getName()).append("\"");
        }
        
        return sql.toString();
    }
    
    /**
     * 转换数据值
     * @param value 原始值
     * @param column 列信息
     * @param targetType 目标数据库类型
     * @return 转换后的值
     */
    private Object convertValue(Object value, ColumnInfo column, DatabaseManager.DatabaseType targetType) {
        if (value == null) {
            return null;
        }
        
        // 处理布尔值
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        
        // 处理日期时间
        if (value instanceof Timestamp || value instanceof java.util.Date) {
            if (targetType == DatabaseManager.DatabaseType.SQLITE) {
                return value.toString();
            }
        }
        
        return value;
    }
    
    /**
     * 取消迁移
     */
    public void cancel() {
        this.cancelled = true;
        logger.info("数据迁移已被取消");
    }
    
    /**
     * 迁移结果类
     */
    public static class MigrationResult {
        private boolean success = false;
        private String errorMessage;
        private int totalTables = 0;
        private int migratedTables = 0;
        private long totalRows = 0;
        private long startTime = System.currentTimeMillis();
        private long endTime;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { 
            this.success = success; 
            if (success) this.endTime = System.currentTimeMillis();
        }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public int getTotalTables() { return totalTables; }
        public void setTotalTables(int totalTables) { this.totalTables = totalTables; }
        
        public int getMigratedTables() { return migratedTables; }
        public void incrementMigratedTables() { this.migratedTables++; }
        
        public long getTotalRows() { return totalRows; }
        public void addRows(long rows) { this.totalRows += rows; }
        
        public long getDuration() { 
            return endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime; 
        }
        
        @Override
        public String toString() {
            return String.format("MigrationResult{success=%s, tables=%d/%d, rows=%d, duration=%dms}", 
                               success, migratedTables, totalTables, totalRows, getDuration());
        }
    }
}