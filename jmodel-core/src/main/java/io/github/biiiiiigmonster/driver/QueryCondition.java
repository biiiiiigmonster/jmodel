package io.github.biiiiiigmonster.driver;

import io.github.biiiiiigmonster.Model;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
@Getter
@AllArgsConstructor
public class QueryCondition<T extends Model<?>> implements Serializable {
    private final Class<T> entityClass;
    private final List<Criterion> criteria = new ArrayList<>();

    /**
     * 创建新的查询条件实例
     *
     * @return 查询条件实例
     */
    public static <T extends Model<?>> QueryCondition<T> create(Class<T> modelClass) {
        return new QueryCondition<>(modelClass);
    }

    /**
     * 添加等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> eq(String field, Object value) {
        criteria.add(new Criterion(field, CriterionType.EQ, value));
        return this;
    }

    /**
     * 添加 IN 条件
     *
     * @param field  字段名
     * @param values 值列表
     * @return 当前查询条件实例
     */
    public QueryCondition<T> in(String field, List<?> values) {
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
    public QueryCondition<T> gt(String field, Object value) {
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
    public QueryCondition<T> lt(String field, Object value) {
        criteria.add(new Criterion(field, CriterionType.LT, value));
        return this;
    }

    /**
     * 添加 LIKE 条件
     *
     * @param field   字段名
     * @param pattern 模式
     * @return 当前查询条件实例
     */
    public QueryCondition<T> like(String field, String pattern) {
        criteria.add(new Criterion(field, CriterionType.LIKE, pattern));
        return this;
    }

    /**
     * 添加 IS NULL 条件
     *
     * @param field 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNull(String field) {
        criteria.add(new Criterion(field, CriterionType.IS_NULL, null));
        return this;
    }

    /**
     * 添加 IS NOT NULL 条件
     *
     * @param field 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNotNull(String field) {
        criteria.add(new Criterion(field, CriterionType.IS_NOT_NULL, null));
        return this;
    }

    /**
     * 查询条件项
     */
    @Getter
    @AllArgsConstructor
    public static class Criterion {
        private final String field;
        private final CriterionType type;
        private final Object value;
    }

    /**
     * 条件类型枚举
     */
    public enum CriterionType {
        EQ, IN, GT, LT, LIKE, IS_NULL, IS_NOT_NULL
    }
}