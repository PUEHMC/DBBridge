package cn.puthmc.model;

/**
 * 索引信息模型
 */
public class IndexInfo {
    
    private String name;
    private String tableName;
    private String columnName;
    private boolean unique;
    private int ordinalPosition;
    private String indexType;
    private String comment;
    
    public IndexInfo() {}
    
    public IndexInfo(String name, String columnName, boolean unique) {
        this.name = name;
        this.columnName = columnName;
        this.unique = unique;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
    
    public int getOrdinalPosition() {
        return ordinalPosition;
    }
    
    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }
    
    public String getIndexType() {
        return indexType;
    }
    
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public String toString() {
        return String.format("IndexInfo{name='%s', column='%s', unique=%s, position=%d}", 
                           name, columnName, unique, ordinalPosition);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        IndexInfo indexInfo = (IndexInfo) obj;
        return name != null ? name.equals(indexInfo.name) : indexInfo.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}