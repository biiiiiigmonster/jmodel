package com.github.biiiiiigmonster.driver;

/**
 * 实体元数据接口
 * 提供实体类的元数据信息，如主键、字段映射等
 * 
 * @author jmodel-core
 */
public interface EntityMetadata {
    
    /**
     * 获取实体类的主键字段名
     * 
     * @param entityClass 实体类
     * @return 主键字段名
     */
    String getPrimaryKey(Class<?> entityClass);
    
    /**
     * 获取字段对应的数据库列名
     * 
     * @param entityClass 实体类
     * @param fieldName 字段名
     * @return 数据库列名
     */
    String getColumnName(Class<?> entityClass, String fieldName);
    
    /**
     * 获取外键字段名（基于约定）
     * 
     * @param entityClass 实体类
     * @return 外键字段名
     */
    String getForeignKey(Class<?> entityClass);
    
    /**
     * 获取元数据提供者标识
     * 
     * @return 提供者名称
     */
    String getProviderName();
}