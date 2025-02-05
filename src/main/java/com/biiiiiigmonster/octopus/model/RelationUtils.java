package com.biiiiiigmonster.octopus.model;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/9 20:03
 */
@Slf4j
@Component
public class RelationUtils implements BeanPostProcessor {
    private static final Map<Class<?>, IService<?>> RELATED_REPOSITORY_MAP = new HashMap<>();

    private static final Map<Class<?>, List<Map<String, Object>>> RELATED_MAP = new HashMap<>();

    private static final Map<String, Map<Object, Method>> MAP_CACHE = new HashMap<>();

    private static final Map<Class<?>, Map<String, ColumnCache>> COLUMN_MAP = new HashMap<>();

    private static final String EXECUTOR_NAME = "relationUtilExecutor";

    private static Executor executor;

    public static <T extends Model<?>> void load(T obj, String... relations) {
        load(ListUtil.toList(obj), Arrays.asList(relations), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, SerializableFunction<T, ?>... relations) {
        load(ListUtil.toList(obj), SerializedLambda.resolveFieldNames(relations), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, RelationOption<T, ?>... relations) {
        load(ListUtil.toList(obj), Arrays.asList(relations), false);
    }

    public static <T extends Model<?>> void load(List<T> list, String... relations) {
        load(list, Arrays.asList(relations), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, SerializableFunction<T, ?>... relations) {
        load(list, SerializedLambda.resolveFieldNames(relations), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, RelationOption<T, ?>... relations) {
        load(list, Arrays.asList(relations), false);
    }

    public static <T extends Model<?>> void load(T obj, boolean loadForce, String... relations) {
        load(ListUtil.toList(obj), Arrays.asList(relations), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, SerializableFunction<T, ?>... relations) {
        load(ListUtil.toList(obj), SerializedLambda.resolveFieldNames(relations), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, RelationOption<T, ?>... relations) {
        load(ListUtil.toList(obj), Arrays.asList(relations), loadForce);
    }

    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, String... relations) {
        load(list, Arrays.asList(relations), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, SerializableFunction<T, ?>... relations) {
        load(list, SerializedLambda.resolveFieldNames(relations), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, RelationOption<T, ?>... relations) {
        load(list, Arrays.asList(relations), loadForce);
    }

    public static <T extends Model<?>> void loadForce(List<T> list, String... relations) {
        load(list, Arrays.asList(relations), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, SerializableFunction<T, ?>... relations) {
        load(list, SerializedLambda.resolveFieldNames(relations), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, RelationOption<T, ?>... relations) {
        load(list, Arrays.asList(relations), true);
    }

    public static <T extends Model<?>> void loadForce(T obj, String... relations) {
        load(ListUtil.toList(obj), Arrays.asList(relations), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, SerializableFunction<T, ?>... relations) {
        load(ListUtil.toList(obj), SerializedLambda.resolveFieldNames(relations), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, RelationOption<T, ?>... relations) {
        load(ListUtil.toList(obj), Arrays.asList(relations), true);
    }

    /**
     *
     * @param models
     * @param list
     * @param loadForce
     * @param <T>
     * @param <P> String | RelationOption<?, ?>
     */
    private static <T extends Model<?>, P> void load(List<T> models, List<P> list, boolean loadForce) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }
        
        List<RelationOption<?, ?>> relations;
        if (list.get(0) instanceof String) {
            relations = processRelations(models.get(0).getClass(), (List<String>) list);
        } else  {
            relations = (List<RelationOption<?, ?>>) list;
        }

        relations.forEach((relation) -> {
            if (executor != null) {
                executor.execute(() -> handle(models, loadForce, relation));
            } else {
                handle(models, loadForce, relation);
            }
        });
    }

    private static <T extends Model<?>, R extends Model<?>> void handle(List<T> models, boolean loadForce, RelationOption<?, ?> relation) {
        RelationReflect<T, R> relationReflect = new RelationReflect<>(relation.getRelatedField());
        // 分离
        List<T> eager = new ArrayList<>();
        List<T> exists = new ArrayList<>();
        List<R> results = new ArrayList<>();
        if (loadForce) {
            eager = models;
        } else {
            for (T model : models) {
                Object value = ReflectUtil.getFieldValue(model, relationReflect.getRelatedField());
                if (value == null) {
                    eager.add(model);
                } else {
                    if (!relation.isNestedEmpty()) {
                        exists.add(model);
                        if (relationReflect.getRelatedFieldIsList()) {
                            results.addAll((List<R>) value);
                        } else {
                            results.add((R) value);
                        }
                    }
                }
            }
        }
        // 合并关联结果
        results.addAll(relationReflect.eagerlyLoad(eager));
        // 将结果中重复的对象直接去重，这个【重复】的判定规则后续还需注意
        results = results.stream().distinct().collect(Collectors.toList());
        // 嵌套处理
        if (!relation.isNestedEmpty()) {
            load(results, relation.getNestedRelations(), loadForce);
        }

        // 合并
        eager.addAll(exists);
        // 组装匹配
        relationReflect.match(eager, results);
    }

    private static List<RelationOption<?, ?>> processRelations(Class<?> clazz, List<String> relations) {
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

        List<RelationOption<?, ?>> list = new ArrayList<>();
        map.forEach((fieldName, nestedRelations) -> {
            RelationOption<?, ?> relationOption = RelationOption.of(clazz, fieldName);
            if (!CollectionUtils.isEmpty(nestedRelations)) {
                relationOption.nested(processRelations(getGenericType(relationOption.getRelatedField()), nestedRelations));
            }
            list.add(relationOption);
        });

        return list;
    }

    public static Map<Object, Method> getRelatedMethod(String key, Field foreignField) {
        return MAP_CACHE.computeIfAbsent(key, k ->
                RELATED_MAP.get(foreignField.getDeclaringClass()).stream()
                        .filter(map -> {
                            Related related = (Related) map.get("annotation");
                            return related.field().equals(foreignField.getName());
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

    public static <T extends Model<?>> IService<T> getRelatedRepository(Class<T> clazz) {
        return (IService<T>) RELATED_REPOSITORY_MAP.get(clazz);
    }

    public static <T extends Model<?>> boolean hasRelatedRepository(Class<T> clazz) {
        return RELATED_REPOSITORY_MAP.containsKey(clazz);
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
     * 启动时扫描一下一些model的加载是否有指定方法，默认都是@RelatedRepository
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (RelationUtils.executor == null && (bean instanceof Executor) && RelationUtils.EXECUTOR_NAME.equals(beanName)) {
            RelationUtils.executor = (Executor) bean;
            return bean;
        }

        relatedRepository(bean);
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            related(bean, method, method.getAnnotation(Related.class));
        }

        return bean;
    }

    private void relatedRepository(Object bean) {
        ClassUtils.getAllInterfaces(bean.getClass()).stream()
                .filter(IService.class::isAssignableFrom)
                .findFirst()
                .ifPresent(iClazz -> {
                    Class<?> typeClass = getTypeClass((ParameterizedType) iClazz.getGenericInterfaces()[0]);
                    RELATED_REPOSITORY_MAP.put(typeClass, (IService<?>) bean);
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
        log.info("实体对象：{}，实例：{}，方法：{}", foreignClazz.getName(), bean.getClass().getName(), method.getName());
        RELATED_MAP.computeIfAbsent(foreignClazz, k -> new ArrayList<>()).add(map);
    }
}
