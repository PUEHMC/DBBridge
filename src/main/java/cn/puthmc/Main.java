package cn.puthmc;

import cn.puthmc.ui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;

/**
 * DBBridge - 主应用程序入口
 * 支持SQLite和MySQL数据库之间的双向转换
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("DBBridge 正在启动...");
        
        // 设置系统外观
        try {
            // 使用FlatLaf现代化深色主题
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            try {
                // 如果深色主题失败，尝试浅色主题
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception ex) {
                // 如果FlatLaf都失败，使用系统默认
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception fallbackEx) {
                    logger.warn("无法设置系统外观: {}", fallbackEx.getMessage());
                }
            }
        }
        
        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            try {
                logger.info("启动 DBBridge 应用程序");
                
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                
                logger.info("应用程序启动成功");
                
            } catch (Exception e) {
                logger.error("启动应用程序时发生错误", e);
                JOptionPane.showMessageDialog(null, 
                    "启动应用程序时发生错误: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}