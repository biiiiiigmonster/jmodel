package com.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.SerializableFunction;
import com.github.biiiiiigmonster.SerializedLambda;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
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

        load(ListUtil.toList(obj), processRelations(obj.getClass(), Arrays.asList(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>, R> void load(T obj, SerializableFunction<T, R>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), SerializedLambda.resolveFieldNames(relations)), false);
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

        load(list, processRelations(list.get(0).getClass(), Arrays.asList(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), SerializedLambda.resolveFieldNames(relations)), false);
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

        load(ListUtil.toList(obj), processRelations(obj.getClass(), Arrays.asList(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, SerializableFunction<T, ?>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), SerializedLambda.resolveFieldNames(relations)), loadForce);
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

        load(list, processRelations(list.get(0).getClass(), Arrays.asList(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), SerializedLambda.resolveFieldNames(relations)), loadForce);
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

        load(list, processRelations(list.get(0).getClass(), Arrays.asList(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), SerializedLambda.resolveFieldNames(relations)), true);
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

        load(ListUtil.toList(obj), processRelations(obj.getClass(), Arrays.asList(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, SerializableFunction<T, ?>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), SerializedLambda.resolveFieldNames(relations)), true);
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
    private static <T extends Model<?>> void load(List<T> models, List<RelationOption<?>> list, boolean loadForce) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }

        list.forEach((relationOption) -> handle(models, relationOption, loadForce));
    }

    private static <T extends Model<?>, R extends Model<?>> void handle(List<T> models, RelationOption<?> relationOption, boolean loadForce) {
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
        Relation relation = relationOption.getRelation();
        // 合并关联结果
        List<R> mergeResults = merge(existResults, relation.getEager(eager));
        // 嵌套处理
        if (relationOption.isNested()) {
            load(mergeResults, relationOption.getNestedRelations(), loadForce);
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
                r -> ReflectUtil.getFieldValue(r, RelationUtils.getPrimaryKey(r.getClass())),
                r -> r,
                (o1, o2) -> o1
        )).values());
    }

    private static <T extends Model<?>, R extends Model<?>> List<RelationOption<?>> processRelations(Class<T> clazz, List<String> relations) {
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

        List<RelationOption<?>> list = new ArrayList<>();
        map.forEach((fieldName, nestedRelations) -> {
            RelationOption<?> relationOption = RelationOption.of(clazz, fieldName);
            if (!CollectionUtils.isEmpty(nestedRelations)) {
                relationOption.nested(processRelations((Class<R>) getGenericType(relationOption.getRelatedField()), nestedRelations));
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
    public static <T extends Model<?>> String getColumn(Field foreignField) {
        Class<?> entityClass = foreignField.getDeclaringClass();
        DataDriver<T> driver = DriverRegistry.getDriver((Class<T>) entityClass);
        return driver.getColumnName(foreignField);
    }

    public static Class<?> getGenericType(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return field.getType();
        }

        return getTypeClass((ParameterizedType) field.getGenericType());
    }

    public static Class<?> getGenericParameterType(Method method) {
        Class<?> paramClazz = method.getParameterTypes()[0];
        if (!List.class.isAssignableFrom(paramClazz)) {
            return paramClazz;
        }

        return getTypeClass((ParameterizedType) method.getGenericParameterTypes()[0]);
    }

    public static Class<?> getGenericReturnType(Method method) {
        if (!List.class.isAssignableFrom(method.getReturnType())) {
            return method.getReturnType();
        }

        return getTypeClass((ParameterizedType) method.getGenericReturnType());
    }

    private static Class<?> getTypeClass(ParameterizedType type) {
        Type[] typeArguments = type.getActualTypeArguments();
        for (Type typeArgument : typeArguments) {
            if (typeArgument instanceof Class) {
                return (Class<?>) typeArgument;
            }
        }

        return Object.class;
    }

    /**
     * 获取实体主键字段名（通过驱动）
     *
     * @param clazz model class
     * @return 主键字段名
     */
    public static <T extends Model<?>> String getPrimaryKey(Class<?> clazz) {
        Class<T> modelClazz = (Class<T>) clazz;
        DataDriver<T> driver = DriverRegistry.getDriver(modelClazz);
        return driver.getPrimaryKey(modelClazz);
    }

    /**
     * 获取外键字段名（通过驱动）
     *
     * @param clazz model class
     * @return 外键字段名
     */
    public static String getForeignKey(Class<?> clazz) {
        String primaryKey = getPrimaryKey(clazz);
        String simpleName = clazz.getSimpleName();
        return StrUtil.lowerFirst(simpleName) + StrUtil.upperFirst(primaryKey);
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, SerializableFunction<T, R> relation, R relationModel) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        associateRelations(relationClass, Collections.singletonList(relationModel));
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, SerializableFunction<T, List<R>> relation, R... relationModels) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        associateRelations(relationClass, Arrays.asList(relationModels));
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, SerializableFunction<T, List<R>> relation, List<R> relationModels) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        associateRelations(relationClass, relationModels);
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, String relation, R... relationModels) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        associateRelations(relationClass, Arrays.asList(relationModels));
    }

    public static <T extends Model<?>, R extends Model<?>> void associateRelations(T model, String relation, List<R> relationModels) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        associateRelations(relationClass, relationModels);
    }

    private static <R extends Model<?>> void associateRelations(Relation relation, List<R> relationModels) {
        if (relation instanceof HasOneOrMany) {
            ((HasOneOrMany) relation).associate(relationModels);
        } else if (relation instanceof BelongsTo) {
            ((BelongsTo) relation).associate(relationModels.get(0));
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        attachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        attachRelations(relationClass, models);
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, String relation, R... models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        attachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void attachRelations(T model, String relation, List<R> models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        attachRelations(relationClass, models);
    }

    private static <R extends Model<?>> void attachRelations(Relation relation, List<R> relationModels) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany) relation).attach(relationModels);
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        detachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        detachRelations(relationClass, models);
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, String relation, R... models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        detachRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void detachRelations(T model, String relation, List<R> models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        detachRelations(relationClass, models);
    }

    private static <R extends Model<?>> void detachRelations(Relation relation, List<R> relationModels) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany) relation).detach(relationModels);
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, models, true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, String relation, R... models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncRelations(T model, String relation, List<R> models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        syncRelations(relationClass, models, true);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), false);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        syncRelations(relationClass, models, false);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, String relation, R... models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        syncRelations(relationClass, Arrays.asList(models), false);
    }

    public static <T extends Model<?>, R extends Model<?>> void syncWithoutDetachingRelations(T model, String relation, List<R> models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        syncRelations(relationClass, models, false);
    }

    private static <R extends Model<?>> void syncRelations(Relation relation, List<R> relationModels, boolean detaching) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany) relation).sync(relationModels, detaching);
        }
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        toggleRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, SerializableFunction<T, List<R>> relation, List<R> models) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        toggleRelations(relationClass, models);
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, String relation, R... models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        toggleRelations(relationClass, Arrays.asList(models));
    }

    public static <T extends Model<?>, R extends Model<?>> void toggleRelations(T model, String relation, List<R> models) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        toggleRelations(relationClass, models);
    }

    private static <R extends Model<?>> void toggleRelations(Relation relation, List<R> relationModels) {
        if (relation instanceof BelongsToMany) {
            ((BelongsToMany) relation).toggle(relationModels);
        }
    }
}
