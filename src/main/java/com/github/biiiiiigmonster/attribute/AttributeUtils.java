package com.github.biiiiiigmonster.attribute;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.RelationUtils;
import com.github.biiiiiigmonster.SerializableFunction;
import com.github.biiiiiigmonster.SerializedLambda;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AttributeUtils implements BeanPostProcessor {
    private static final Map<String, Map<Object, Method>> ATTRIBUTE_MAP = new HashMap<>();

    public static Map<Object, Method> getComputedMethod(Field attribute) {
        return ATTRIBUTE_MAP.get(attributeCacheKey(attribute.getDeclaringClass(), attribute.getName()));
    }

    public static <T extends Model<?>> void append(T obj, String... attributes) {
        computed(ListUtil.toList(obj), Arrays.asList(attributes));
    }

    @SafeVarargs
    public static <T extends Model<?>, R> void append(T obj, SerializableFunction<T, R>... attributes) {
        computed(ListUtil.toList(obj), SerializedLambda.resolveFieldNames(attributes));
    }

    public static <T extends Model<?>> void append(List<T> models, String... attributes) {
        computed(models, Arrays.asList(attributes));
    }

    @SafeVarargs
    public static <T extends Model<?>, R> void append(List<T> models, SerializableFunction<T, R>... attributes) {
        computed(models, SerializedLambda.resolveFieldNames(attributes));
    }

    private static <T extends Model<?>> void computed(List<T> models, List<String> attributes) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }

        processAttributes(attributes)
                .forEach(attributeName -> handle(models, attributeName));
    }

    private static List<String> processAttributes(List<String> attributes) {
        return attributes.stream()
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
    }

    /**
     * 添加属性
     * 支持两种方式
     */
    private static <T extends Model<?>> void handle(List<T> models, String attributeName) {
        models.forEach(model -> {
            String attributeMethodName = attributeName;
            Method attributeMethod = ReflectUtil.getMethod(model.getClass(), attributeMethodName);
            if (attributeMethod != null) {
                Object value = ReflectUtil.invoke(model, attributeMethod);
                ReflectUtil.setFieldValue(model, attributeName, value);
            } else {
                Field field = ReflectUtil.getField(model.getClass(), attributeName);
                Map<Object, Method> methodMap = AttributeUtils.getComputedMethod(field);
                if (methodMap == null) {
                    return;
                }
                Object bean = methodMap.keySet().iterator().next();
                Method method = methodMap.values().iterator().next();
                ReflectUtil.invoke(bean, method, model);
            }
        });
    }

    /**
     * 启动时扫描一下一些model的计算属性是否有指定方法填充，默认都是当前model下计算属性的同名方法
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            attribute(bean, method, method.getAnnotation(Attribute.class));
        }

        return bean;
    }

    private void attribute(Object bean, Method method, Attribute annotation) {
        if (annotation == null) {
            return;
        }
        Map<Object, Method> map = new HashMap<>();
        map.put(bean, method);
        Class<?> modelClazz = RelationUtils.getGenericParameterType(method);
        String field = annotation.field();
        if (field.isEmpty()) {
            field = StrUtil.removePreAndLowerFirst(method.getName(), "attribute");
        }
        ATTRIBUTE_MAP.put(attributeCacheKey(modelClazz, field), map);
    }

    private static String attributeCacheKey(Class<?> clazz, String field) {
        return String.format("%s.%s", clazz.getName(), field);
    }

    public static boolean hasAttributeAnnotation(Field field) {
        return field.getAnnotation(Attribute.class) != null;
    }
}
