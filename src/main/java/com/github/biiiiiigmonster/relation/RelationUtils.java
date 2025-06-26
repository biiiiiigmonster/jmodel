package com.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.SerializableFunction;
import com.github.biiiiiigmonster.SerializedLambda;
import com.github.biiiiiigmonster.relation.annotation.config.Related;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author luyunfeng
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class RelationUtils implements BeanPostProcessor {
    private static final Map<Class<?>, BaseMapper<?>> RELATED_REPOSITORY_MAP = new HashMap<>();

    private static final Map<Class<?>, List<Map<String, Object>>> RELATED_MAP = new HashMap<>();

    private static final Map<String, Map<Object, Method>> MAP_CACHE = new HashMap<>();

    private static final Map<Class<?>, Map<String, ColumnCache>> COLUMN_MAP = new HashMap<>();

    @Autowired
    private ApplicationContext context;

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
        List<R> results = new ArrayList<>();
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
                            results.addAll((List<R>) value);
                        } else {
                            results.add((R) value);
                        }
                    }
                }
            }
        }
        Relation relation = relationOption.getRelation();
        // 合并关联结果
        results = merge(results, relation.getEager(eager));
        // 嵌套处理
        if (relationOption.isNested()) {
            load(results, relationOption.getNestedRelations(), loadForce);
        }

        // 合并父模型数据
        eager.addAll(exists);
        // 组装匹配
        relation.match(eager, results);
    }

    // 新旧结果集合并，如果重复以新结果集为准
    private static <T extends Model<?>> List<T> merge(List<T> oldList, List<T> newList) {
        if (CollectionUtils.isEmpty(oldList) || CollectionUtils.isEmpty(newList)) {
            return newList;
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
                        }
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

    public static String relatedMethodCacheKey(Field field) {
        return String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
    }

    public static Map<Object, Method> getRelatedMethod(Field field) {
        String key = relatedMethodCacheKey(field);
        return MAP_CACHE.computeIfAbsent(key, k ->
                RELATED_MAP.get(field.getDeclaringClass()).stream()
                        .filter(map -> {
                            Related related = (Related) map.get("annotation");
                            return related.field().equals(field.getName());
                        })
                        .map(map -> {
                            Map<Object, Method> cache = new HashMap<>();
                            cache.put(map.get("bean"), (Method) map.get("method"));
                            return cache;
                        })
                        .findFirst()
                        .orElse(null)
        );
    }

    public static BaseMapper<?> getRelatedRepository(Class<?> clazz) {
        return RELATED_REPOSITORY_MAP.get(clazz);
    }

    public static boolean hasRelatedRepository(Field field) {
        return RELATED_REPOSITORY_MAP.containsKey(field.getDeclaringClass());
    }

    public static String getColumn(Field foreignField) {
        Class<?> entityClass = foreignField.getDeclaringClass();

        Map<String, ColumnCache> columnMap = COLUMN_MAP.computeIfAbsent(entityClass, LambdaUtils::getColumnMap);
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(foreignField.getName()));
        return columnCache.getColumn();
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
     * 模型默认本地键名
     *
     * @param clazz model class
     * @return String
     */
    public static String getPrimaryKey(Class<?> clazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        return tableInfo == null ? "id" : tableInfo.getKeyColumn();
    }

    /**
     * 模型默认外地键名
     *
     * @param clazz model class
     * @return String
     */
    public static String getForeignKey(Class<?> clazz) {
        return StrUtil.lowerFirst(clazz.getSimpleName()) + StrUtil.upperFirst(getPrimaryKey(clazz));
    }

    /**
     * 启动时扫描一下一些model的加载是否有指定方法，默认都是@RelatedRepository
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            related(bean, method, method.getAnnotation(Related.class));
        }

        return bean;
    }

    @PostConstruct
    public void init() {
        context.getBeansOfType(BaseMapper.class).forEach((beanName, mapper) -> {
            Class<?> mapperClass = AopUtils.isAopProxy(mapper) ? AopUtils.getTargetClass(beanName) : mapper.getClass();
            Class<?> typeClass = ReflectionKit.getSuperClassGenericType(mapperClass, BaseMapper.class, 0);
            RELATED_REPOSITORY_MAP.put(typeClass, mapper);
        });
    }

    private void related(Object bean, Method method, Related annotation) {
        if (annotation == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("bean", bean);
        map.put("method", method);
        map.put("annotation", annotation);
        Class<?> foreignClazz = getGenericReturnType(method);
        RELATED_MAP.computeIfAbsent(foreignClazz, k -> new ArrayList<>()).add(map);
    }

    public static <T extends Model<?>, R extends Model<?>> void saveRelations(T model, SerializableFunction<T, R> relation, R relationModel) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        saveRelations(relationClass, Collections.singletonList(relationModel));
    }

    public static <T extends Model<?>, R extends Model<?>> void saveRelations(T model, SerializableFunction<T, List<R>> relation, R... relationModels) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        saveRelations(relationClass, Arrays.asList(relationModels));
    }

    public static <T extends Model<?>, R extends Model<?>> void saveRelations(T model, SerializableFunction<T, List<R>> relation, List<R> relationModels) {
        Relation relationClass = RelationOption.of(relation).getRelation().setModel(model);

        saveRelations(relationClass, relationModels);
    }

    public static <T extends Model<?>, R extends Model<?>> void saveRelations(T model, String relation, R... relationModels) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        saveRelations(relationClass, Arrays.asList(relationModels));
    }

    public static <T extends Model<?>, R extends Model<?>> void saveRelations(T model, String relation, List<R> relationModels) {
        Relation relationClass = RelationOption.of(model.getClass(), relation).getRelation().setModel(model);

        saveRelations(relationClass, relationModels);
    }

    private static <R extends Model<?>> void saveRelations(Relation relation, List<R> relationModels) {
        if (relation instanceof HasOneOrMany) {
            ((HasOneOrMany) relation).save(relationModels);
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
