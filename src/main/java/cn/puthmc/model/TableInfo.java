package cn.puthmc.model;

import java.util.List;
import java.util.ArrayList;

/**
 * 表信息模型
 */
public class TableInfo {
    
    private String name;
    private String comment;
    private List<ColumnInfo> columns = new ArrayList<>();
    private List<IndexInfo> indexes = new ArrayList<>();
    private long rowCount;
    
    public TableInfo() {}
    
    public TableInfo(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public List<ColumnInfo> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns != null ? columns : new ArrayList<>();
    }
    
    public List<IndexInfo> getIndexes() {
        return indexes;
    }
    
    public void setIndexes(List<IndexInfo> indexes) {
        this.indexes = indexes != null ? indexes : new ArrayList<>();
    }
    
    public long getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }
    
    // 便利方法
    public void addColumn(ColumnInfo column) {
        if (column != null) {
            this.columns.add(column);
        }
    }
    
    public void addIndex(IndexInfo index) {
        if (index != null) {
            this.indexes.add(index);
        }
    }
    
    public ColumnInfo getColumn(String columnName) {
        return columns.stream()
                .filter(col -> col.getName().equals(columnName))
                .findFirst()
                .orElse(null);
    }
    
    public List<ColumnInfo> getPrimaryKeyColumns() {
        return columns.stream()
                .filter(ColumnInfo::isPrimaryKey)
                .toList();
    }
    
    public boolean hasPrimaryKey() {
        return columns.stream().anyMatch(ColumnInfo::isPrimaryKey);
    }
    
    @Override
    public String toString() {
        return String.format("TableInfo{name='%s', columns=%d, indexes=%d, rows=%d}", 
                           name, columns.size(), indexes.size(), rowCount);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TableInfo tableInfo = (TableInfo) obj;
        return name != null ? name.equals(tableInfo.name) : tableInfo.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}