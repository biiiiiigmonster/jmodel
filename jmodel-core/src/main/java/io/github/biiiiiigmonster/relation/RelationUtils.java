package io.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.SerializableFunction;
import io.github.biiiiiigmonster.SerializedLambda;
import io.github.biiiiiigmonster.driver.DataDriver;
import io.github.biiiiiigmonster.driver.DriverRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关联关系工具类
 * 提供模型关联加载、关联操作等功能
 *
 * @author luyunfeng
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class RelationUtils {

    public static <T extends Model<?>> void load(T obj, String... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj, Arrays.asList(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>, R> void load(T obj, SerializableFunction<T, R>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj, SerializedLambda.resolveFieldNames(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, RelationOption<T>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), Arrays.asList(relations), false);
    }

    public static <T extends Model<?>> void load(List<T> list, String... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0), Arrays.asList(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0), SerializedLambda.resolveFieldNames(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, RelationOption<T>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, Arrays.asList(relations), false);
    }

    public static <T extends Model<?>> void load(T obj, boolean loadForce, String... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj, Arrays.asList(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, SerializableFunction<T, ?>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj, SerializedLambda.resolveFieldNames(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, RelationOption<T>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), Arrays.asList(relations), loadForce);
    }

    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, String... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0), Arrays.asList(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0), SerializedLambda.resolveFieldNames(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, RelationOption<T>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, Arrays.asList(relations), loadForce);
    }

    public static <T extends Model<?>> void loadForce(List<T> list, String... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0), Arrays.asList(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0), SerializedLambda.resolveFieldNames(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, RelationOption<T>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, Arrays.asList(relations), true);
    }

    public static <T extends Model<?>> void loadForce(T obj, String... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj, Arrays.asList(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, SerializableFunction<T, ?>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj, SerializedLambda.resolveFieldNames(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, RelationOption<T>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), Arrays.asList(relations), true);
    }

    /**
     * @param models
     * @param list
     * @param loadForce
     * @param <T>
     */
    private static <T extends Model<?>> void load(List<T> models, List<RelationOption<T>> list, boolean loadForce) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }

        list.forEach((relationOption) -> handle(models, relationOption, loadForce));
    }

    private static <T extends Model<?>, R extends Model<?>> void handle(List<T> models, RelationOption<T> relationOption, boolean loadForce) {
        // 分离
        List<T> eager = new ArrayList<>();
        List<T> exists = new ArrayList<>();
        List<R> existResults = new ArrayList<>();
        if (loadForce) {
            eager = models;
        } else {
            for (T model : models) {
                Object value = ReflectUtil.getFieldValue(model, relationOption.getRelatedField());
                if (value == null) {
                    eager.add(model);
                } else {
                    if (relationOption.isNested()) {
                        exists.add(model);
                        if (relationOption.isRelatedFieldList()) {
                            existResults.addAll((List<R>) value);
                        } else {
                            existResults.add((R) value);
                        }
                    }
                }
            }
        }
        Relation<T> relation = relationOption.getRelation();
        // 合并关联结果
        List<R> mergeResults = merge(existResults, relation.getEager(eager));
        // 嵌套处理
        if (relationOption.isNested()) {
            List<RelationOption<? extends Model<?>>> nestedRelations = relationOption.getNestedRelations();
            List<RelationOption<R>> nestedRelationOptions = new ArrayList<>();
            for (RelationOption<? extends Model<?>> nestedRelation : nestedRelations) {
                nestedRelationOptions.add((RelationOption<R>) nestedRelation);
            }
            load(mergeResults, nestedRelationOptions, loadForce);
        }

        // 合并父模型数据
        eager.addAll(exists);
        // 组装匹配
        relation.match(eager, mergeResults);
    }

    // 新旧结果集合并，如果重复以新结果集为准
    private static <T extends Model<?>> List<T> merge(List<T> oldList, List<T> newList) {
        if (CollectionUtils.isEmpty(oldList)) {
            return newList;
        }

        if (CollectionUtils.isEmpty(newList)) {
            return oldList;
        }

        newList.addAll(oldList);
        return new ArrayList<>(newList.stream().collect(Collectors.toMap(
                r -> ReflectUtil.getFieldValue(r, RelationUtils.getPrimaryKey((Class<? extends Model<?>>) r.getClass())),
                r -> r,
                (o1, o2) -> o1
        )).values());
    }

    private static <T extends Model<?>> List<RelationOption<T>> processRelations(T model, List<String> relations) {
        return processRelations((Class<T>) model.getClass(), relations);
    }

    private static <T extends Model<?>, R extends Model<?>> List<RelationOption<T>> processRelations(Class<T> clazz, List<String> relations) {
        Map<String, List<String>> map = relations.stream()
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toMap(
                        relation -> StrUtil.subBefore(relation, ".", false),
                        relation -> {
                            String nestedRelation = StrUtil.subAfter(relation, ".", false);
                            return nestedRelation.isEmpty() ? new ArrayList<>() : ListUtil.toList(nestedRelation);
                        },
                        (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        },
                        LinkedHashMap::new
                ));

        List<RelationOption<T>> list = new ArrayList<>();
        map.forEach((fieldName, nestedRelations) -> {
            RelationOption<T> relationOption = RelationOption.of(clazz, fieldName);
            if (!CollectionUtils.isEmpty(nestedRelations)) {
                List<RelationOption<R>> nestedRelationOptions = processRelations((Class<R>) getGenericType(relationOption.getRelatedField()), nestedRelations);
                relationOption.nested(nestedRelationOptions);
            }
            list.add(relationOption);
        });

        return list;
    }

    /**
     * 获取字段对应的数据库列名（通过驱动）
     *
     * @param foreignField 字段
     * @return 数据库列名
     */
    public static String getColumn(Field foreignField) {
        Class<?> entityClass = foreignField.getDeclaringClass();
        DataDriver driver = DriverRegistry.getDriver((Class<? extends Model<?>>) entityClass);
        return driver.getColumnName(foreignField);
    }

    public static Class<? extends Model<?>> getGenericType(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return (Class<? extends Model<?>>) field.getType();
        }

        return getTypeClass((ParameterizedType) field.getGenericType());
    }

    public static Class<? extends Model<?>> getGenericParameterType(Method method) {
        Class<?> paramClazz = method.getParameterTypes()[0];
        if (!List.class.isAssignableFrom(paramClazz)) {
            return (Class<? extends Model<?>>) paramClazz;
        }

        return getTypeClass((ParameterizedType) method.getGenericParameterTypes()[0]);
    }

    private static Class<? extends Model<?>> getTypeClass(ParameterizedType type) {
        Type[] typeArguments = type.getActualTypeArguments();
        for (Type typeArgument : typeArguments) {
            if (typeArgument instanceof Class) {
                return (Class<? extends Model<?>>) typeArgument;
            }
        }

        return null;
    }

    /**
     * 获取实体主键字段名（通过驱动）
     *
     * @param clazz model class
     * @return 主键字段名
     */
    public static String getPrimaryKey(Class<? extends Model<?>> clazz) {
        DataDriver driver = DriverRegistry.getDriver(clazz);
        return driver.getPrimaryKey(clazz);
    }

    /**
     * 获取外键字段名（通过驱动）
     *
     * @param clazz model class
     * @return 外键字段名
     */
    public static String getForeignKey(Class<? extends Model<?>> clazz) {
        String primaryKey = getPrimaryKey(clazz);
        String simpleName = clazz.getSimpleName();
        return StrUtil.lowerFirst(simpleName) + StrUtil.upperFirst(primaryKey);
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, SerializableFunction<T, R> relation, R relationModel) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        associateRelations(relationClass, Collections.singletonList(relationModel));
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, SerializableFunction<T, List<R>> relation, R... relationModels) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        associateRelations(relationClass, Arrays.asList(relationModels));
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, SerializableFunction<T, List<R>> relation, List<R> relationModels) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        associateRelations(relationClass, relationModels);
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, String relation, R... relationModels) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        associateRelations(relationClass, Arrays.asList(relationModels));
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, String relation, List<R> relationModels) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        associateRelations(relationClass, relationModels);
    }

    private static <T extends Model<?>, R extends Model<?>> void associateRelations(Relation<T> relation, List<R> relationModels) {
        if (relation instanceof HasOneOrMany) {
            ((HasOneOrMany<T>) relation).associate(relationModels);
        } else if (relation instanceof BelongsTo) {
            ((BelongsTo<T>) relation).associate(relationModels.get(0));
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        attachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        attachRelations(relationClass, models);
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, String relation, R... models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        attachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, String relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        attachRelations(relationClass, models);
    }

    private static <T extends Model<?>, R extends Model<?>> void attachRelations(Relation<T> relation, List<R> relationModels) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany<T, ? extends Pivot<?>>) relation).attach(relationModels);
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        detachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        detachRelations(relationClass, models);
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, String relation, R... models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        detachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, String relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        detachRelations(relationClass, models);
    }

    private static <T extends Model<?>, R extends Model<?>> void detachRelations(Relation<T> relation, List<R> relationModels) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany<T, ? extends Pivot<?>>) relation).detach(relationModels);
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, models, true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, String relation, R... models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, String relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        syncRelations(relationClass, models, true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), false);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, models, false);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, String relation, R... models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), false);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, String relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        syncRelations(relationClass, models, false);
    }

    private static <T extends Model<?>, R extends Model<?>> void syncRelations(Relation<T> relation, List<R> relationModels, boolean detaching) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany<T, ? extends Pivot<?>>) relation).sync(relationModels, detaching);
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        toggleRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(relation).getRelation().setModel(model);

        toggleRelations(relationClass, models);
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, String relation, R... models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        toggleRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, String relation, List<R> models) {
        Relation<T> relationClass = RelationOption.of(model, relation).getRelation().setModel(model);

        toggleRelations(relationClass, models);
    }

    private static <T extends Model<?>, R extends Model<?>> void toggleRelations(Relation<T> relation, List<R> relationModels) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany<T, ? extends Pivot<?>>) relation).toggle(relationModels);
        }
    }
}
