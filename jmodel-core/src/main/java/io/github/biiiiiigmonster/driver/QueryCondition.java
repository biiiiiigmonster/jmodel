package io.github.biiiiiigmonster.driver;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.SerializableFunction;
import io.github.biiiiiigmonster.SerializedLambda;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.lang.reflect.Field;
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
     * @param fieldName 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> eq(String fieldName, Object value) {
        return eq(ReflectUtil.getField(entityClass, fieldName), value);
    }

    /**
     * 添加 IN 条件
     *
     * @param fieldName  字段名
     * @param values 值列表
     * @return 当前查询条件实例
     */
    public QueryCondition<T> in(String fieldName, List<?> values) {
        return in(ReflectUtil.getField(entityClass, fieldName), values);
    }

    /**
     * 添加大于条件
     *
     * @param fieldName 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> gt(String fieldName, Object value) {
        return gt(ReflectUtil.getField(entityClass, fieldName), value);
    }

    /**
     * 添加小于条件
     *
     * @param fieldName 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> lt(String fieldName, Object value) {
        return lt(ReflectUtil.getField(entityClass, fieldName), value);
    }

    /**
     * 添加 LIKE 条件
     *
     * @param fieldName   字段名
     * @param pattern 模式
     * @return 当前查询条件实例
     */
    public QueryCondition<T> like(String fieldName, String pattern) {
        return like(ReflectUtil.getField(entityClass, fieldName), pattern);
    }

    /**
     * 添加 IS NULL 条件
     *
     * @param fieldName 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNull(String fieldName) {
        return isNull(ReflectUtil.getField(entityClass, fieldName));
    }

    /**
     * 添加 IS NOT NULL 条件
     *
     * @param fieldName 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNotNull(String fieldName) {
        return isNotNull(ReflectUtil.getField(entityClass, fieldName));
    }

    /**
     * 添加 apply
     *
     * @param fieldName 字段名
     * @param type      方法
     * @param value     值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> apply(String fieldName, CriterionType type, Object value) {
        return apply(ReflectUtil.getField(entityClass, fieldName), type, value);
    }

    /**
     * 添加等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> eq(Field field, Object value) {
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
    public QueryCondition<T> in(Field field, List<?> values) {
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
    public QueryCondition<T> gt(Field field, Object value) {
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
    public QueryCondition<T> lt(Field field, Object value) {
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
    public QueryCondition<T> like(Field field, String pattern) {
        criteria.add(new Criterion(field, CriterionType.LIKE, pattern));
        return this;
    }

    /**
     * 添加 IS NULL 条件
     *
     * @param field 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNull(Field field) {
        criteria.add(new Criterion(field, CriterionType.IS_NULL, null));
        return this;
    }

    /**
     * 添加 IS NOT NULL 条件
     *
     * @param field 字段名
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNotNull(Field field) {
        criteria.add(new Criterion(field, CriterionType.IS_NOT_NULL, null));
        return this;
    }

    /**
     * 添加 apply
     *
     * @param field     字段
     * @param type      方法
     * @param value     值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> apply(Field field, CriterionType type, Object value) {
        criteria.add(new Criterion(field, type, value));
        return this;
    }

    /**
     * 添加等于条件（Lambda 版本）
     *
     * @param field 字段 Lambda 表达式
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> eq(SerializableFunction<T, ?> field, Object value) {
        return eq(SerializedLambda.getField(field), value);
    }

    /**
     * 添加 IN 条件（Lambda 版本）
     *
     * @param field  字段 Lambda 表达式
     * @param values 值列表
     * @return 当前查询条件实例
     */
    public QueryCondition<T> in(SerializableFunction<T, ?> field, List<?> values) {
        return in(SerializedLambda.getField(field), values);
    }

    /**
     * 添加大于条件（Lambda 版本）
     *
     * @param field 字段 Lambda 表达式
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> gt(SerializableFunction<T, ?> field, Object value) {
        return gt(SerializedLambda.getField(field), value);
    }

    /**
     * 添加小于条件（Lambda 版本）
     *
     * @param field 字段 Lambda 表达式
     * @param value 值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> lt(SerializableFunction<T, ?> field, Object value) {
        return lt(SerializedLambda.getField(field), value);
    }

    /**
     * 添加 LIKE 条件（Lambda 版本）
     *
     * @param field   字段 Lambda 表达式
     * @param pattern 模式
     * @return 当前查询条件实例
     */
    public QueryCondition<T> like(SerializableFunction<T, ?> field, String pattern) {
        return like(SerializedLambda.getField(field), pattern);
    }

    /**
     * 添加 IS NULL 条件（Lambda 版本）
     *
     * @param field 字段 Lambda 表达式
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNull(SerializableFunction<T, ?> field) {
        return isNull(SerializedLambda.getField(field));
    }

    /**
     * 添加 IS NOT NULL 条件（Lambda 版本）
     *
     * @param field 字段 Lambda 表达式
     * @return 当前查询条件实例
     */
    public QueryCondition<T> isNotNull(SerializableFunction<T, ?> field) {
        return isNotNull(SerializedLambda.getField(field));
    }

    /**
     * 添加 apply
     *
     * @param field     字段
     * @param type      方法
     * @param value     值
     * @return 当前查询条件实例
     */
    public QueryCondition<T> apply(SerializableFunction<T, ?> field, CriterionType type, Object value) {
        return apply(SerializedLambda.getField(field), type, value);
    }
}