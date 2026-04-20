package io.github.biiiiiigmonster.relation.constraint;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.QueryCondition;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 查询约束应用工具。
 * <p>
 * 负责将 {@link Constraint} 注解、{@link RelationConstraint} 实例、运行时
 * {@code Consumer} 形式的约束，统一应用到 {@link QueryCondition} 上。
 * <p>
 * 对 {@link Constraint} 的 {@code String[]} 值，按照关联实体对应字段的 Java 类型
 * 进行自动转换（{@code Long}/{@code Integer}/{@code Boolean}/{@code String} 等）。
 *
 * @author luyunfeng
 */
public final class ConstraintApplier {

    private ConstraintApplier() {
    }

    /**
     * 字段类型缓存：class:fieldName -> Class
     */
    private static final ConcurrentMap<String, Class<?>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 应用注解声明的静态约束数组
     *
     * @param condition    查询条件
     * @param entityClass  关联实体 Class
     * @param constraints  注解数组
     */
    public static <R extends Model<?>> void applyAnnotations(QueryCondition<R> condition,
                                                             Class<R> entityClass,
                                                             Constraint[] constraints) {
        if (constraints == null || constraints.length == 0) {
            return;
        }
        for (Constraint c : constraints) {
            applyAnnotation(condition, entityClass, c);
        }
    }

    /**
     * 应用单个 {@link Constraint} 注解
     */
    public static <R extends Model<?>> void applyAnnotation(QueryCondition<R> condition,
                                                            Class<R> entityClass,
                                                            Constraint c) {
        String field = c.field();
        String[] raw = c.value();
        switch (c.type()) {
            case EQ:
                condition.eq(field, convertSingle(entityClass, field, raw));
                break;
            case GT:
                condition.gt(field, convertSingle(entityClass, field, raw));
                break;
            case LT:
                condition.lt(field, convertSingle(entityClass, field, raw));
                break;
            case LIKE:
                condition.like(field, raw != null && raw.length > 0 ? raw[0] : "");
                break;
            case IN:
                condition.in(field, convertList(entityClass, field, raw));
                break;
            case IS_NULL:
                condition.isNull(field);
                break;
            case IS_NOT_NULL:
                condition.isNotNull(field);
                break;
            default:
                break;
        }
    }

    /**
     * 应用 {@link RelationConstraint} 类型约束，支持 {@link RelationConstraint.Noop}
     * 作为占位符跳过
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <R extends Model<?>> void applyConstraintClass(QueryCondition<R> condition,
                                                                 Class<? extends RelationConstraint> constraintClass) {
        if (constraintClass == null || constraintClass == RelationConstraint.Noop.class) {
            return;
        }
        try {
            RelationConstraint instance = constraintClass.getDeclaredConstructor().newInstance();
            instance.apply(condition);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate RelationConstraint: " + constraintClass.getName(), e);
        }
    }

    /**
     * 应用运行时 {@link RelationConstraint} 实例
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <R extends Model<?>> void applyRuntime(QueryCondition<R> condition,
                                                         RelationConstraint runtimeConstraint) {
        if (runtimeConstraint == null) {
            return;
        }
        runtimeConstraint.apply(condition);
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
