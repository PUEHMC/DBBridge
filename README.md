# DBBridge - SQLite ⇄ MySQL Database Converter

一个功能强大的图形化数据库转换工具，支持 SQLite 和 MySQL 数据库之间的双向转换。

A powerful graphical database conversion tool that supports bidirectional conversion between SQLite and MySQL databases.

## 功能特性

- 🔄 **双向转换**: 支持 SQLite 到 MySQL 和 MySQL 到 SQLite 的转换
- 🖱️ **拖拽支持**: 支持直接拖拽 SQLite 文件到应用程序
- 🎨 **现代化界面**: 基于 JavaFX 的美观用户界面
- 📊 **实时进度**: 显示详细的迁移进度和状态
- 📝 **日志记录**: 完整的操作日志和错误信息
- ⚡ **高性能**: 批量处理和优化的数据传输
- 🔧 **灵活配置**: 可选择迁移数据、结构或两者

## 系统要求

- Java 21 或更高版本
- Windows/Linux/macOS 操作系统
- 至少 512MB 可用内存

## 快速开始

### 编译和运行

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/DBBridge.git
   cd DBBridge
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **运行应用程序**
   ```bash
   mvn javafx:run
   ```

### 打包为可执行文件

```bash
mvn clean package
```

生成的 JAR 文件位于 `target/` 目录下，可以直接运行：

```bash
java -jar target/db-converter-1.0-SNAPSHOT-shaded.jar
```

## 使用指南

### SQLite 到 MySQL 转换

1. **选择源数据库**
   - 选择 "SQLite" 作为源数据库类型
   - 点击 "浏览..." 选择 SQLite 文件，或直接拖拽文件到拖拽区域

2. **配置目标数据库**
   - 选择 "MySQL" 作为目标数据库类型
   - 填写 MySQL 连接信息（主机、端口、数据库名、用户名、密码）
   - 点击 "测试连接" 验证连接

3. **开始转换**
   - 选择迁移选项（数据、结构、删除已存在的表）
   - 点击 "开始迁移" 按钮
   - 观察进度条和日志输出

### MySQL 到 SQLite 转换

1. **选择源数据库**
   - 选择 "MySQL" 作为源数据库类型
   - 填写 MySQL 连接信息并测试连接

2. **配置目标数据库**
   - 选择 "SQLite" 作为目标数据库类型
   - 点击 "浏览..." 选择输出 SQLite 文件路径

3. **开始转换**
   - 配置迁移选项
   - 点击 "开始迁移" 按钮

## 技术架构

### 核心组件

- **DatabaseManager**: 数据库连接管理
- **SchemaAnalyzer**: 数据库结构分析
- **SqlConverter**: SQL 语法转换引擎
- **DataMigrator**: 数据迁移核心逻辑
- **MainController**: GUI 界面控制器

### 技术栈

- **Java 21**: 核心开发语言
- **JavaFX**: 图形用户界面框架
- **JDBC**: 数据库连接
- **Maven**: 项目构建和依赖管理
- **SLF4J + Logback**: 日志记录
- **SQLite JDBC**: SQLite 数据库驱动
- **MySQL Connector/J**: MySQL 数据库驱动

## 数据类型映射

### SQLite 到 MySQL

| SQLite 类型 | MySQL 类型 |
|-------------|------------|
| INTEGER | INT |
| REAL | DOUBLE |
| TEXT | TEXT |
| BLOB | BLOB |
| NUMERIC | DECIMAL |

### MySQL 到 SQLite

| MySQL 类型 | SQLite 类型 |
|------------|-------------|
| INT, BIGINT | INTEGER |
| FLOAT, DOUBLE | REAL |
| VARCHAR, TEXT | TEXT |
| BLOB | BLOB |
| DECIMAL | NUMERIC |

## 配置选项

### 迁移选项

- **迁移数据**: 复制表中的所有数据
- **迁移结构**: 复制表结构、索引和约束
- **删除已存在的表**: 在创建新表前删除目标数据库中的同名表

### 性能调优

- 批处理大小: 1000 行/批次
- 提交间隔: 5000 行
- 连接池: 自动管理

## 故障排除

### 常见问题

1. **连接失败**
   - 检查数据库服务是否运行
   - 验证连接参数（主机、端口、用户名、密码）
   - 确保防火墙允许连接

2. **内存不足**
   - 增加 JVM 堆内存: `-Xmx2g`
   - 处理大型数据库时分批迁移

3. **字符编码问题**
   - 确保数据库使用 UTF-8 编码
   - 检查特殊字符的处理

### 日志文件

应用程序日志保存在 `logs/db-converter.log` 文件中，包含详细的操作记录和错误信息。

## 开发指南

### 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/dbconverter/
│   │       ├── Main.java                 # 应用程序入口
│   │       ├── converter/                # 转换器模块
│   │       │   ├── DataMigrator.java     # 数据迁移器
│   │       │   └── SqlConverter.java     # SQL转换器
│   │       ├── db/                       # 数据库模块
│   │       │   ├── DatabaseManager.java # 数据库管理器
│   │       │   └── SchemaAnalyzer.java  # 结构分析器
│   │       ├── model/                    # 数据模型
│   │       │   ├── ColumnInfo.java      # 列信息
│   │       │   ├── IndexInfo.java       # 索引信息
│   │       │   └── TableInfo.java       # 表信息
│   │       └── ui/                       # 用户界面
│   │           └── MainController.java  # 主控制器
│   └── resources/
│       ├── css/
│       │   └── styles.css               # 样式文件
│       ├── fxml/
│       │   └── main.fxml                # 界面布局
│       ├── icons/
│       │   └── app-icon.svg             # 应用图标
│       └── logback.xml                  # 日志配置
└── pom.xml                              # Maven配置
```

### 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 更新日志

### v1.0.0 (2024-01-XX)

- 🎉 初始版本发布
- ✨ 支持 SQLite 和 MySQL 双向转换
- 🎨 现代化 JavaFX 用户界面
- 🖱️ 文件拖拽支持
- 📊 实时进度显示
- 📝 完整的日志记录

## 支持

如果您遇到问题或有功能建议，请：

1. 查看 [FAQ](docs/FAQ.md)
2. 搜索现有的 [Issues](issues)
3. 创建新的 [Issue](issues/new)

---

**开发者**: [Your Name]
**邮箱**: [your.email@example.com]
**项目主页**: [https://github.com/yourusername/DBBridge](https://github.com/yourusername/DBBridge)