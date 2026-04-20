package io.github.biiiiiigmonster.relation.constraint;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.driver.Criterion;
import io.github.biiiiiigmonster.driver.QueryCondition;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * 查询约束转化工具。
 * <p>
 * 负责把 {@link Constraint} 注解转化为 {@link Consumer} 形式，便于与运行时
 * {@code Consumer<QueryCondition>} 统一存储、统一应用。
 * <p>
 * 对 {@link Constraint#value()} 的 {@code String[]} 值，按照关联实体对应字段的
 * * Java 类型做自动转换（Long / Integer / Boolean / String 等）。
 *
 * @author luyunfeng
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ConstraintApplier {

    private ConstraintApplier() {
    }

    /**
     * 字段类型缓存：class:fieldName -> Class
     */
    private static final ConcurrentMap<String, Class<?>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 把注解数组转化为 {@code Consumer<QueryCondition>} 列表
     *
     * @param entityClass 关联实体 Class
     * @param constraints 注解数组
     * @return 对应的 Consumer 列表（可为空列表）
     */
    public static List<Consumer<QueryCondition>> toConsumers(Class<?> entityClass, Constraint[] constraints) {
        List<Consumer<QueryCondition>> list = new ArrayList<>();
        if (constraints == null) {
            return list;
        }
        for (Constraint c : constraints) {
            list.add(toConsumer(entityClass, c));
        }

        return list;
    }

    /**
     * 把单个 {@link Constraint} 注解转化为 {@code Consumer<QueryCondition>}
     */
    public static Consumer<QueryCondition> toConsumer(Class<?> entityClass, Constraint c) {
        final String field = c.field();
        final String[] raw = c.value();
        switch (c.type()) {
            case EQ:
            case GT:
            case LT:
            case LIKE:
                return cond -> cond.apply(new Criterion(field, c.type(), convertSingle(entityClass, field, raw)));
            case IN:
                return cond -> cond.apply(new Criterion(field, c.type(), convertList(entityClass, field, raw)));
            case IS_NULL:
            case IS_NOT_NULL:
                return cond -> cond.apply(new Criterion(field, c.type(), null));
            default:
                return cond -> {
                };
        }
    }

    private static Object convertSingle(Class<?> entityClass, String field, String[] raw) {
        if (raw == null || raw.length == 0) {
            return null;
        }
        Class<?> targetType = resolveFieldType(entityClass, field);
        return Convert.convert(targetType, raw[0]);
    }

    private static List<Object> convertList(Class<?> entityClass, String field, String[] raw) {
        List<Object> result = new ArrayList<>();
        if (raw == null || raw.length == 0) {
            return result;
        }
        Class<?> targetType = resolveFieldType(entityClass, field);
        for (String s : raw) {
            result.add(Convert.convert(targetType, s));
        }
        return result;
    }

    private static Class<?> resolveFieldType(Class<?> entityClass, String fieldName) {
        String key = entityClass.getName() + ":" + fieldName;
        return FIELD_TYPE_CACHE.computeIfAbsent(key, k -> {
            Field f = ReflectUtil.getField(entityClass, fieldName);
            return f != null ? f.getType() : String.class;
        });
    }
}
