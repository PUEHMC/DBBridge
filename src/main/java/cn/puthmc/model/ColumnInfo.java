package cn.puthmc.model;

/**
 * 列信息模型
 */
public class ColumnInfo {
    
    private String name;
    private String dataType;
    private int size;
    private int decimalDigits;
    private boolean nullable;
    private String defaultValue;
    private String comment;
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean unique;
    
    public ColumnInfo() {}
    
    public ColumnInfo(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public int getDecimalDigits() {
        return decimalDigits;
    }
    
    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
    
    /**
     * 获取完整的数据类型定义（包含长度）
     * @return 完整的数据类型
     */
    public String getFullDataType() {
        if (size > 0) {
            if (decimalDigits > 0) {
                return String.format("%s(%d,%d)", dataType, size, decimalDigits);
            } else {
                return String.format("%s(%d)", dataType, size);
            }
        }
        return dataType;
    }
    
    /**
     * 检查是否为数值类型
     * @return 是否为数值类型
     */
    public boolean isNumericType() {
        if (dataType == null) return false;
        
        String type = dataType.toUpperCase();
        return type.contains("INT") || type.contains("DECIMAL") || 
               type.contains("NUMERIC") || type.contains("FLOAT") || 
               type.contains("DOUBLE") || type.contains("REAL");
    }
    
    /**
     * 检查是否为字符串类型
     * @return 是否为字符串类型
     */
    public boolean isStringType() {
        if (dataType == null) return false;
        
        String type = dataType.toUpperCase();
        return type.contains("CHAR") || type.contains("TEXT") || 
               type.contains("CLOB") || type.contains("STRING");
    }
    
    /**
     * 检查是否为日期时间类型
     * @return 是否为日期时间类型
     */
    public boolean isDateTimeType() {
        if (dataType == null) return false;
        
        String type = dataType.toUpperCase();
        return type.contains("DATE") || type.contains("TIME") || 
               type.contains("TIMESTAMP");
    }
    
    /**
     * 检查是否为二进制类型
     * @return 是否为二进制类型
     */
    public boolean isBinaryType() {
        if (dataType == null) return false;
        
        String type = dataType.toUpperCase();
        return type.contains("BLOB") || type.contains("BINARY") || 
               type.contains("VARBINARY");
    }
    
    @Override
    public String toString() {
        return String.format("ColumnInfo{name='%s', dataType='%s', size=%d, nullable=%s, primaryKey=%s, autoIncrement=%s}", 
                           name, dataType, size, nullable, primaryKey, autoIncrement);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ColumnInfo that = (ColumnInfo) obj;
        return name != null ? name.equals(that.name) : that.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}