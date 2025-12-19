package com.github.biiiiiigmonster.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件抽象类
 * 提供统一的查询条件表示，由各驱动实现转换为具体的查询语句
 * 专注于条件过滤，保持接口精简
 * 
 * @author jmodel-core
 */
public class QueryCondition {
    private final List<Criterion> criteria = new ArrayList<>();
    
    /**
     * 创建新的查询条件实例
     * 
     * @return 查询条件实例
     */
    public static QueryCondition create() {
        return new QueryCondition();
    }
    
    /**
     * 添加等于条件
     * 
     * @param field 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition eq(String field, Object value) {
        criteria.add(new Criterion(field, CriterionType.EQ, value));
        return this;
    }
    
    /**
     * 添加 IN 条件
     * 
     * @param field 字段名
     * @param values 值列表
     * @return 当前查询条件实例
     */
    public QueryCondition in(String field, List<?> values) {
        criteria.add(new Criterion(field, CriterionType.IN, values));
        return this;
    }
    
    /**
     * 添加大于条件
     * 
     * @param field 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition gt(String field, Object value) {
        criteria.add(new Criterion(field, CriterionType.GT, value));
        return this;
    }
    
    /**
     * 添加小于条件
     * 
     * @param field 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition lt(String field, Object value) {
        criteria.add(new Criterion(field, CriterionType.LT, value));
        return this;
    }
    
    /**
     * 添加 LIKE 条件
     * 
     * @param field 字段名
     * @param pattern 模式
     * @return 当前查询条件实例
     */
    public QueryCondition like(String field, String pattern) {
        criteria.add(new Criterion(field, CriterionType.LIKE, pattern));
        return this;
    }
    
    /**
     * 添加 IS NULL 条件
     * 
     * @param field 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition isNull(String field) {
        criteria.add(new Criterion(field, CriterionType.IS_NULL, null));
        return this;
    }
    
    /**
     * 添加 IS NOT NULL 条件
     * 
     * @param field 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition isNotNull(String field) {
        criteria.add(new Criterion(field, CriterionType.IS_NOT_NULL, null));
        return this;
    }
    
    /**
     * 便捷方法：批量主键查询
     * 
     * @param primaryKeyField 主键字段名
     * @param ids 主键值列表
     * @return 查询条件实例
     */
    public static QueryCondition byIds(String primaryKeyField, List<? extends Serializable> ids) {
        return create().in(primaryKeyField, new ArrayList<>(ids));
    }
    
    /**
     * 便捷方法：字段值批量查询（用于关联查询优化）
     * 
     * @param fieldName 字段名
     * @param values 值列表
     * @return 查询条件实例
     */
    public static QueryCondition byFieldValues(String fieldName, List<?> values) {
        return create().in(fieldName, new ArrayList<>(values));
    }
    
    /**
     * 获取所有查询条件
     * 
     * @return 条件列表
     */
    public List<Criterion> getCriteria() { 
        return criteria; 
    }
    
    /**
     * 查询条件项
     */
    public static class Criterion {
        private final String field;
        private final CriterionType type;
        private final Object value;
        
        public Criterion(String field, CriterionType type, Object value) {
            this.field = field;
            this.type = type;
            this.value = value;
        }
        
        public String getField() { 
            return field; 
        }
        
        public CriterionType getType() { 
            return type; 
        }
        
        public Object getValue() { 
            return value; 
        }
    }
    
    /**
     * 条件类型枚举
     */
    public enum CriterionType {
        EQ, IN, GT, LT, LIKE, IS_NULL, IS_NOT_NULL
    }
}