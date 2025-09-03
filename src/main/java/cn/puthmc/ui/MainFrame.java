package cn.puthmc.ui;

import cn.puthmc.converter.DataMigrator;
import cn.puthmc.db.DatabaseManager;
import cn.puthmc.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * DBBridge 主窗口
 * 使用Swing实现的GUI界面
 */
public class MainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    
    // UI组件
    private JTextField sqlitePathField;
    private JTextField mysqlHostField;
    private JTextField mysqlPortField;
    private JTextField mysqlDatabaseField;
    private JTextField mysqlUsernameField;
    private JPasswordField mysqlPasswordField;
    private JComboBox<String> conversionDirectionCombo;
    private JButton testConnectionButton;
    private JButton startMigrationButton;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JLabel statusLabel;
    
    // 业务组件
    private DataMigrator dataMigrator;
    
    public MainFrame() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeServices();
    }
    
    private void initializeComponents() {
        setTitle("DBBridge - 数据库转换工具 v1.0");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        
        // 设置现代化的窗口图标和外观
        setIconImage(createDefaultIcon());
        
        // 数据库连接字段 - 使用更大的字体和更好的间距
        Font fieldFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        sqlitePathField = new JTextField(35);
        sqlitePathField.setFont(fieldFont);
        sqlitePathField.setPreferredSize(new Dimension(300, 30));
        
        mysqlHostField = new JTextField("localhost", 20);
        mysqlHostField.setFont(fieldFont);
        mysqlHostField.setPreferredSize(new Dimension(150, 30));
        
        mysqlPortField = new JTextField("3306", 8);
        mysqlPortField.setFont(fieldFont);
        mysqlPortField.setPreferredSize(new Dimension(80, 30));
        
        mysqlDatabaseField = new JTextField(20);
        mysqlDatabaseField.setFont(fieldFont);
        mysqlDatabaseField.setPreferredSize(new Dimension(150, 30));
        
        mysqlUsernameField = new JTextField("root", 15);
        mysqlUsernameField.setFont(fieldFont);
        mysqlUsernameField.setPreferredSize(new Dimension(120, 30));
        
        mysqlPasswordField = new JPasswordField(15);
        mysqlPasswordField.setFont(fieldFont);
        mysqlPasswordField.setPreferredSize(new Dimension(120, 30));
        
        // 转换方向选择
        conversionDirectionCombo = new JComboBox<>(new String[]{
            "SQLite → MySQL", "MySQL → SQLite"
        });
        conversionDirectionCombo.setFont(fieldFont);
        conversionDirectionCombo.setPreferredSize(new Dimension(180, 30));
        
        // 按钮 - 使用更现代的样式
        Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        testConnectionButton = new JButton("🔗 测试连接");
        testConnectionButton.setFont(buttonFont);
        testConnectionButton.setPreferredSize(new Dimension(120, 35));
        testConnectionButton.setFocusPainted(false);
        
        startMigrationButton = new JButton("🚀 开始迁移");
        startMigrationButton.setFont(buttonFont);
        startMigrationButton.setPreferredSize(new Dimension(120, 35));
        startMigrationButton.setEnabled(false);
        startMigrationButton.setFocusPainted(false);
        
        // 进度条 - 更现代的样式
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("就绪");
        progressBar.setPreferredSize(new Dimension(0, 25));
        progressBar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // 日志区域 - 更好的字体和间距
        logArea = new JTextArea(18, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(10, 10, 10, 10));
        
        // 状态标签 - 更好的字体
        statusLabel = new JLabel("📊 就绪");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 顶部面板 - 标题
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        titlePanel.setBackground(UIManager.getColor("Panel.background"));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("🗃️ DBBridge - 数据库转换工具");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // 中央面板 - 使用更好的间距
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        // 配置面板
        JPanel configPanel = createConfigPanel();
        centerPanel.add(configPanel, BorderLayout.NORTH);
        
        // 日志面板
        JPanel logPanel = createLogPanel();
        centerPanel.add(logPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部状态面板
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        TitledBorder border = new TitledBorder("⚙️ 数据库配置");
        border.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.setBorder(BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // SQLite配置
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel sqliteLabel = new JLabel("📁 SQLite文件:");
        sqliteLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(sqliteLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(sqlitePathField, gbc);
        
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JButton browseButton = new JButton("📂 浏览");
        browseButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        browseButton.setPreferredSize(new Dimension(90, 30));
        browseButton.addActionListener(e -> browseSQLiteFile());
        panel.add(browseButton, gbc);
        
        // MySQL配置
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel mysqlHostLabel = new JLabel("🖥️ MySQL主机:");
        mysqlHostLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(mysqlHostLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(mysqlHostField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel portLabel = new JLabel("🔌 端口:");
        portLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(portLabel, gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(mysqlPortField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel dbLabel = new JLabel("🗄️ 数据库名:");
        dbLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(dbLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(mysqlDatabaseField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel userLabel = new JLabel("👤 用户名:");
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(userLabel, gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(mysqlUsernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("🔐 密码:");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(mysqlPasswordField, gbc);
        
        // 转换方向和按钮
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        JLabel directionLabel = new JLabel("🔄 转换方向:");
        directionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(directionLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(conversionDirectionCombo, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(8, 15, 8, 8);
        panel.add(testConnectionButton, gbc);
        
        gbc.gridx = 3; gbc.insets = new Insets(8, 8, 8, 8);
        panel.add(startMigrationButton, gbc);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = new TitledBorder("📋 操作日志");
        border.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.setBorder(BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建默认图标
     */
    private Image createDefaultIcon() {
        // 创建一个简单的32x32图标
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制数据库图标
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillOval(4, 4, 24, 24);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2d.drawString("DB", 8, 20);
        
        g2d.dispose();
        return icon;
    }
    
    private void setupEventHandlers() {
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        // 测试连接按钮
        testConnectionButton.addActionListener(e -> testConnections());
        
        // 开始迁移按钮
        startMigrationButton.addActionListener(e -> startMigration());
    }
    
    private void initializeServices() {
        dataMigrator = new DataMigrator();
    }
    
    private void browseSQLiteFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite数据库文件 (*.db, *.sqlite)", "db", "sqlite"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            sqlitePathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    private void testConnections() {
        CompletableFuture.runAsync(() -> {
            SwingUtilities.invokeLater(() -> {
                testConnectionButton.setEnabled(false);
                updateStatus("正在测试连接...");
                appendLog("开始测试数据库连接...");
            });
            
            try {
                // 测试SQLite连接
                String sqlitePath = sqlitePathField.getText().trim();
                if (sqlitePath.isEmpty()) {
                    throw new IllegalArgumentException("请选择SQLite数据库文件");
                }
                
                final boolean[] sqliteOk = {false};
                try (Connection conn = DatabaseManager.createSQLiteConnection(sqlitePath)) {
                    sqliteOk[0] = DatabaseManager.testConnection(conn);
                    SwingUtilities.invokeLater(() -> {
                        appendLog(sqliteOk[0] ? "✓ SQLite连接成功" : "✗ SQLite连接失败");
                    });
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("✗ SQLite连接失败: " + e.getMessage());
                    });
                }
                
                // 测试MySQL连接
                String host = mysqlHostField.getText().trim();
                String port = mysqlPortField.getText().trim();
                String database = mysqlDatabaseField.getText().trim();
                String username = mysqlUsernameField.getText().trim();
                String password = new String(mysqlPasswordField.getPassword());
                
                if (host.isEmpty() || database.isEmpty() || username.isEmpty()) {
                    throw new IllegalArgumentException("请填写完整的MySQL连接信息");
                }
                
                final boolean[] mysqlOk = {false};
                try (Connection conn = DatabaseManager.createMySQLConnection(host, Integer.parseInt(port), database, username, password)) {
                    mysqlOk[0] = DatabaseManager.testConnection(conn);
                    SwingUtilities.invokeLater(() -> {
                        appendLog(mysqlOk[0] ? "✓ MySQL连接成功" : "✗ MySQL连接失败");
                    });
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("✗ MySQL连接失败: " + e.getMessage());
                    });
                }
                
                if (sqliteOk[0] && mysqlOk[0]) {
                    SwingUtilities.invokeLater(() -> {
                        updateStatus("连接测试成功");
                        startMigrationButton.setEnabled(true);
                        appendLog("所有数据库连接测试通过，可以开始迁移");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        updateStatus("连接测试失败");
                        startMigrationButton.setEnabled(false);
                    });
                }
                
            } catch (Exception e) {
                logger.error("连接测试失败", e);
                SwingUtilities.invokeLater(() -> {
                    updateStatus("连接测试失败: " + e.getMessage());
                    appendLog("连接测试失败: " + e.getMessage());
                    startMigrationButton.setEnabled(false);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    testConnectionButton.setEnabled(true);
                });
            }
        });
    }
    
    private void startMigration() {
        // 实现数据迁移逻辑
        appendLog("数据迁移功能正在开发中...");
    }
    
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            java.time.LocalTime now = java.time.LocalTime.now();
            String timeFormat = String.format("[%02d %02d %02d INFO] ", 
                now.getHour(), now.getMinute(), now.getSecond());
            logArea.append(timeFormat + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void shutdown() {
        logger.info("应用程序正在关闭...");
        
        // 清理资源
        // DatabaseManager使用静态方法，连接会自动管理
        
        System.exit(0);
    }
}