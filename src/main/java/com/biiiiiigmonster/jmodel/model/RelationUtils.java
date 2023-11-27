package com.biiiiiigmonster.jmodel.model;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
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
    private static final Map<Class<?>, List<Map<String, Object>>> RELATION_REPOSITORY_MAP = new HashMap<>();

    private static final Map<String, Map<Object, Method>> FILL_MAP = new HashMap<>();

    private static final Map<String, Map<Object, Method>> MAP_CACHE = new HashMap<>();

    public static <T> void load(T obj, String... withs) {
        load(ListUtil.toList(obj), withs, false);
    }

    public static <T> void load(List<T> list, String... withs) {
        load(list, withs, false);
    }

    public static <T> void load(T obj, boolean loadForce, String... withs) {
        load(ListUtil.toList(obj), withs, loadForce);
    }

    public static <T> void load(List<T> list, boolean loadForce, String... withs) {
        load(list, withs, loadForce);
    }

    public static <T> void loadForce(List<T> list, String... withs) {
        load(list, withs, true);
    }

    public static <T> void loadForce(T obj, String... withs) {
        load(ListUtil.toList(obj), withs, true);
    }

    private static <T, R> void load(List<T> models, String[] withs, boolean loadForce) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }

        Class<T> clazz = (Class<T>) models.get(0).getClass();
        Arrays.stream(withs)
                .filter(ObjectUtil::isNotEmpty)
                .forEach(with -> {
                    String fieldName = StrUtil.subBefore(with, ".", false);
                    String nestedWith = StrUtil.subAfter(with, ".", false);
                    RelationReflect<T, R> relationReflect = new RelationReflect<>(clazz, fieldName);
                    // 分离
                    List<T> eager = new ArrayList<>();
                    List<T> exists = new ArrayList<>();
                    List<R> foreign = new ArrayList<>();
                    if (loadForce) {
                        eager = models;
                    } else {
                        for (T model : models) {
                            Object value = ReflectUtil.getFieldValue(model, relationReflect.getRelatedField());
                            if (value == null) {
                                eager.add(model);
                            } else {
                                if (!nestedWith.isEmpty()) {
                                    exists.add(model);
                                    if (relationReflect.getRelatedFieldIsList()) {
                                        foreign.addAll((List<R>) value);
                                    } else {
                                        foreign.add((R) value);
                                    }
                                }
                            }
                        }
                    }
                    // 加载
                    foreign.addAll(relationReflect.fetchForeignResult(eager));
                    // 嵌套
                    load(foreign, new String[]{nestedWith}, loadForce);
                    // 组装
                    eager.addAll(exists);
                    relationReflect.setRelation(eager, foreign);
                });
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

    public static Map<Object, Method> getRepositoryMethodList(String key, Field foreignField) {
        return MAP_CACHE.computeIfAbsent(key, k ->
                RELATION_REPOSITORY_MAP.get(foreignField.getDeclaringClass()).stream()
                        .filter(map -> {
                            RelatedRepository relatedRepository = (RelatedRepository) map.get("annotation");
                            if (relatedRepository.field().isEmpty()) {
                                return getGenericParameterType((Method) map.get("method")).equals(foreignField.getType());
                            } else {
                                return relatedRepository.field().equals(foreignField.getName());
                            }
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
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            relatedRepository(bean, method, method.getAnnotation(RelatedRepository.class));
            fill(bean, method, method.getAnnotation(Fill.class));
        }

        return bean;
    }

    private void relatedRepository(Object bean, Method method, RelatedRepository annotation) {
        if (annotation == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("bean", bean);
        map.put("method", method);
        map.put("annotation", annotation);
        Class<?> foreignClazz = getGenericReturnType(method);
        log.info("实体对象：{}，字段：{}，实例：{}，方法：{}", foreignClazz.getName(), map.get("field"), bean.getClass().getName(), method.getName());
        RELATION_REPOSITORY_MAP.computeIfAbsent(foreignClazz, k -> new ArrayList<>()).add(map);
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
