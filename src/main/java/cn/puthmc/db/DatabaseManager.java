package cn.puthmc.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

/**
 * 数据库连接管理器
 * 支持SQLite和MySQL数据库连接
 */
public class DatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    public enum DatabaseType {
        SQLITE, MYSQL
    }
    
    /**
 * 创建SQLite数据库连接
     * @param filePath SQLite数据库文件路径
     * @return 数据库连接
     */
    public static Connection createSQLiteConnection(String filePath) throws SQLException {
        try {
            String url = "jdbc:sqlite:" + filePath;
            Properties props = new Properties();
            props.setProperty("foreign_keys", "true");
            
            Connection conn = DriverManager.getConnection(url, props);
            logger.info("成功连接到SQLite数据库: {}", filePath);
            return conn;
        } catch (SQLException e) {
            logger.error("连接SQLite数据库失败: {}", filePath, e);
            throw e;
        }
    }
    
    /**
     * 创建MySQL数据库连接
     * @param host 主机地址
     * @param port 端口号
     * @param database 数据库名
     * @param username 用户名
     * @param password 密码
     * @return 数据库连接
     */
    public static Connection createMySQLConnection(String host, int port, String database, 
                                                   String username, String password) throws SQLException {
        try {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                                     host, port, database);
            
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("autoReconnect", "true");
            
            Connection conn = DriverManager.getConnection(url, props);
            logger.info("成功连接到MySQL数据库: {}:{}/{}", host, port, database);
            return conn;
        } catch (SQLException e) {
            logger.error("连接MySQL数据库失败: {}:{}/{}", host, port, database, e);
            throw e;
        }
    }
    
    /**
     * 测试数据库连接
     * @param connection 数据库连接
     * @return 连接是否有效
     */
    public static boolean testConnection(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            logger.warn("测试数据库连接时发生错误", e);
            return false;
        }
    }
    
    /**
     * 安全关闭数据库连接
     * @param connection 数据库连接
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("数据库连接已关闭");
            } catch (SQLException e) {
                logger.warn("关闭数据库连接时发生错误", e);
            }
        }
    }
    
    /**
     * 安全关闭Statement
     * @param statement Statement对象
     */
    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.warn("关闭Statement时发生错误", e);
            }
        }
    }
    
    /**
     * 安全关闭ResultSet
     * @param resultSet ResultSet对象
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.warn("关闭ResultSet时发生错误", e);
            }
        }
    }
    
    /**
     * 获取数据库类型
     * @param connection 数据库连接
     * @return 数据库类型
     */
    public static DatabaseType getDatabaseType(Connection connection) {
        try {
            String url = connection.getMetaData().getURL();
            if (url.startsWith("jdbc:sqlite:")) {
                return DatabaseType.SQLITE;
            } else if (url.startsWith("jdbc:mysql:")) {
                return DatabaseType.MYSQL;
            }
        } catch (SQLException e) {
            logger.warn("获取数据库类型时发生错误", e);
        }
        return null;
    }
}