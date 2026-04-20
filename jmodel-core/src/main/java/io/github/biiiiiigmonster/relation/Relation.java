package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.DataDriver;
import io.github.biiiiiigmonster.driver.DriverRegistry;
import io.github.biiiiiigmonster.driver.QueryCondition;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import io.github.biiiiiigmonster.relation.annotation.config.MorphId;
import io.github.biiiiiigmonster.relation.annotation.config.MorphName;
import io.github.biiiiiigmonster.relation.annotation.config.MorphType;
import io.github.biiiiiigmonster.relation.constraint.Constraint;
import io.github.biiiiiigmonster.relation.constraint.ConstraintApplier;
import io.github.biiiiiigmonster.relation.constraint.RelationConstraint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@Slf4j
@SuppressWarnings("unchecked")
public abstract class Relation<T extends Model<?>> {
    protected Field relatedField;
    protected T model;

    /**
     * 来自关系注解 {@code constraints()} 的静态约束
     */
    protected Constraint[] annotationConstraints;

    /**
     * 来自关系注解 {@code constraint()} 的静态约束类
     */
    protected Class<? extends RelationConstraint> constraintClass;

    /**
     * 运行时动态约束
     */
    protected RelationConstraint<?> runtimeConstraint;

    private static final Map<String, String> MORPH_ALIAS_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Morph> MORPH_MAP = new ConcurrentHashMap<>();

    public Relation(Field relatedField) {
        this.relatedField = relatedField;
    }

    public abstract <R extends Model<?>> List<R> getEager(List<T> models);

    public abstract <R extends Model<?>> List<R> match(List<T> models, List<R> results);

    /**
     * 统一的结果获取方法，支持条件扩展
     * <p>
     * 如果本次查询的 {@code entityClass} 为终表（即关联模型本身），则会在驱动执行前
     * 应用静态注解约束、静态 {@link RelationConstraint} 类以及运行时约束。中间表
     * （如 BelongsToMany 的 pivot、Through 的中介模型）不会被约束。
     *
     * @param entityClass       实体类型
     * @param conditionEnhancer 条件增强器，可为 null
     */
    protected <R extends Model<?>> List<R> getResult(Class<R> entityClass, Consumer<QueryCondition<R>> conditionEnhancer) {
        DataDriver driver = DriverRegistry.getDriver(entityClass);
        QueryCondition<R> condition = QueryCondition.create(entityClass);
        conditionEnhancer.accept(condition);

        if (isTargetClass(entityClass)) {
            applyConstraints(condition, entityClass);
        }

        return driver.findByCondition(condition);
    }

    /**
     * 判断给定实体类是否为当前关系的终表（即关联模型）
     */
    protected boolean isTargetClass(Class<?> entityClass) {
        Class<?> target = RelationUtils.getGenericType(relatedField);
        return target != null && target.equals(entityClass);
    }

    /**
     * 把静态注解、静态 RelationConstraint 类、运行时约束统一应用到 QueryCondition
     */
    protected <R extends Model<?>> void applyConstraints(QueryCondition<R> condition, Class<R> entityClass) {
        ConstraintApplier.applyAnnotations(condition, entityClass, annotationConstraints);
        ConstraintApplier.applyConstraintClass(condition, constraintClass);
        ConstraintApplier.applyRuntime(condition, runtimeConstraint);
    }

    /**
     * 注入注解声明的静态约束
     */
    public Relation<T> withAnnotationConstraints(Constraint[] constraints,
                                                 Class<? extends RelationConstraint> constraintClass) {
        this.annotationConstraints = constraints;
        this.constraintClass = constraintClass;
        return this;
    }

    /**
     * 注入运行时 {@link RelationConstraint} 约束（可直接传入 lambda）
     */
    public Relation<T> withRuntimeConstraint(RelationConstraint<?> runtimeConstraint) {
        this.runtimeConstraint = runtimeConstraint;
        return this;
    }

    /**
     * 获取结果
     */
    protected <R extends Model<?>> List<R> getResult(List<?> keys, Field relatedField) {
        if (CollectionUtils.isEmpty(keys)) {
            return new ArrayList<>();
        }

        Class<R> entityClass = (Class<R>) relatedField.getDeclaringClass();
        String columnName = RelationUtils.getColumn(relatedField);
        return getResult(entityClass, cond -> cond.in(columnName, keys));
    }

    public static <T extends Model<?>> List<?> relatedKeyValueList(List<T> models, Field field) {
        return models.stream()
                .map(o -> ReflectUtil.getFieldValue(o, field))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
    }

    public static String getMorphAlias(Class<?> clazz, Class<?> within) {
        String key = clazz.getName() + within.getName();
        return MORPH_ALIAS_MAP.computeIfAbsent(key,
                k -> Arrays.stream(clazz.getAnnotationsByType(MorphAlias.class))
                        .filter(m -> m.in().length == 0 || Arrays.stream(m.in()).collect(Collectors.toSet()).contains(within))
                        // MorphAlias allowed annotation more times, get the max in() length when multi matched.
                        .max(Comparator.comparingInt(m -> m.in().length))
                        .map(m -> StringUtils.isBlank(m.value()) ? clazz.getSimpleName() : m.value())
                        .orElse(clazz.getName())
        );
    }

    public static Morph getMorph(Class<?> clazz) {
        return MORPH_MAP.computeIfAbsent(clazz,
                k -> {
                    String name = StrUtil.lowerFirst(k.getSimpleName());
                    MorphName morphName = k.getAnnotation(MorphName.class);
                    if (morphName != null && StringUtils.isNotBlank(morphName.value())) {
                        name = morphName.value();
                    }

                    String type = String.format("%sType", name);
                    String id = String.format("%sId", name);

                    for (Field field : k.getDeclaredFields()) {
                        MorphType morphType = field.getAnnotation(MorphType.class);
                        if (morphType != null) {
                            type = field.getName();
                        }
                        MorphId morphId = field.getAnnotation(MorphId.class);
                        if (morphId != null) {
                            id = field.getName();
                        }
                    }

                    return new Morph(type, id);
                }
        );
    }

    public Relation<T> setModel(T model) {
        this.model = model;
        return this;
    }
}
