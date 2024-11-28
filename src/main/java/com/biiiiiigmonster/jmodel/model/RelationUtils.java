package com.biiiiiigmonster.jmodel.model;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final Map<String, Map<Object, Method>> FILL_MAP = new HashMap<>();

    private static final Map<Class<?>, Map<String, ColumnCache>> COLUMN_MAP = new HashMap<>();

    public static <T extends Model<?>> void load(T obj, String... withs) {
        load(ListUtil.toList(obj), withs, false);
    }

    public static <T extends Model<?>> void load(List<T> list, String... withs) {
        load(list, withs, false);
    }

    public static <T extends Model<?>> void load(T obj, boolean loadForce, String... withs) {
        load(ListUtil.toList(obj), withs, loadForce);
    }

    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, String... withs) {
        load(list, withs, loadForce);
    }

    public static <T extends Model<?>> void loadForce(List<T> list, String... withs) {
        load(list, withs, true);
    }

    public static <T extends Model<?>> void loadForce(T obj, String... withs) {
        load(ListUtil.toList(obj), withs, true);
    }

    private static <T extends Model<?>> void load(List<T> models, String[] withs, boolean loadForce) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }

        processWiths(withs).forEach((fieldName, nestedWiths) -> handle(models, loadForce, fieldName, nestedWiths));
    }

    private static <T extends Model<?>, R extends Model<?>> void handle(List<T> models, boolean loadForce, String fieldName, List<String> nestedWiths) {
        RelationReflect<T, R> relationReflect = new RelationReflect<>((Class<T>) models.get(0).getClass(), fieldName);
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
                    if (!nestedWiths.isEmpty()) {
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
        results.addAll(relationReflect.fetchForeignResult(eager));
        // 将结果中重复的对象直接去重，这个【重复】的判定规则后续还需注意
        results = results.stream().distinct().collect(Collectors.toList());
        // 嵌套处理
        if (!nestedWiths.isEmpty()) {
            load(results, nestedWiths.toArray(new String[0]), loadForce);
        }

        // 合并
        eager.addAll(exists);
        // 组装匹配
        relationReflect.match(eager, results);
    }

    private static Map<String, List<String>> processWiths(String[] withs) {
        return Arrays.stream(withs)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toMap(
                        with -> StrUtil.subBefore(with, ".", false),
                        with -> {
                            String nestedWith = StrUtil.subAfter(with, ".", false);
                            return nestedWith.isEmpty() ? new ArrayList<>() : ListUtil.toList(nestedWith);
                        },
                        (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        }
                ));
    }

    @SafeVarargs
    public static <T extends Model<T>, R> void append(List<T> models, SerializableFunction<T, R>... column) {
        models.forEach(model -> model.append(column));
    }

    @SafeVarargs
    public static <T extends Model<T>, R> void append(T obj, SerializableFunction<T, R>... column) {
        if (obj != null) {
            obj.append(column);
        }
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

    public static IService<?> getRelatedRepository(Class<?> clazz) {
        return RELATED_REPOSITORY_MAP.get(clazz);
    }

    public static boolean hasRelatedRepository(Class<?> clazz) {
        return RELATED_REPOSITORY_MAP.containsKey(clazz);
    }

    public static String getColumn(Field foreignField) {
        Class<?> entityClass = foreignField.getDeclaringClass();

        Map<String, ColumnCache> columnMap = COLUMN_MAP.computeIfAbsent(entityClass, LambdaUtils::getColumnMap);
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(foreignField.getName()));
        return columnCache.getColumn();
    }

    public static Map<Object, Method> getFillMethod(Field fillable) {
        return FILL_MAP.get(fillCacheKey(fillable.getDeclaringClass(), fillable.getName()));
    }

    public static Class<?> getGenericType(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return field.getType();
        }

        return getTypeClass((ParameterizedType) field.getGenericType());
    }

    private static Class<?> getGenericParameterType(Method method) {
        Class<?> paramClazz = method.getParameterTypes()[0];
        if (!List.class.isAssignableFrom(paramClazz)) {
            return paramClazz;
        }

        return getTypeClass((ParameterizedType) method.getGenericParameterTypes()[0]);
    }

    private static Class<?> getGenericReturnType(Method method) {
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

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        relatedRepository(bean);
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            related(bean, method, method.getAnnotation(Related.class));
            fill(bean, method, method.getAnnotation(Fill.class));
        }

        return bean;
    }

    private void relatedRepository(Object bean) {
        Class<?> clazz = bean.getClass();
        if (bean instanceof IService) {
            ClassUtils.getAllInterfaces(clazz).stream()
                    .filter(iClazz -> iClazz.getAnnotation(RelatedRepository.class) != null && IService.class.isAssignableFrom(iClazz))
                    .findFirst()
                    .ifPresent(iClazz -> {
                        Class<?> typeClass = getTypeClass((ParameterizedType) iClazz.getGenericInterfaces()[0]);
                        RELATED_REPOSITORY_MAP.put(typeClass, (IService<?>) bean);
                    });
        }
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

    private void fill(Object bean, Method method, Fill annotation) {
        if (annotation == null) {
            return;
        }
        Map<Object, Method> map = new HashMap<>();
        map.put(bean, method);
        Class<?> modelClazz = getGenericParameterType(method);
        String field = annotation.field();
        if (field.isEmpty()) {
            field = StrUtil.removePreAndLowerFirst(method.getName(), "fill");
        }
        FILL_MAP.put(fillCacheKey(modelClazz, field), map);
    }

    private static String fillCacheKey(Class<?> clazz, String field) {
        return String.format("%s.%s", clazz.getName(), field);
    }
}
