package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Slf4j
public abstract class Relation {
    protected Field relatedField;

    private static final BiMap<String, Class<?>> MORPH_MAP = HashBiMap.create();

    public Relation(Field relatedField) {
        this.relatedField = relatedField;
    }

    public abstract <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models);

    public abstract <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results);

    @SuppressWarnings("unchecked")
    protected <R extends Model<?>> List<R> byRelatedMethod(List<?> localKeyValueList, Map<Object, Method> relatedMethod) {
        Object bean = relatedMethod.keySet().iterator().next();
        Method method = relatedMethod.values().iterator().next();
        if (List.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return ReflectUtil.invoke(bean, method, localKeyValueList);
        } else {
            log.warn("{}存在N + 1查询隐患，建议{}实现List参数的仓库方法", bean.getClass().getName(), method.getName());
            return localKeyValueList.stream()
                    .map(param -> ReflectUtil.invoke(bean, method, param))
                    .filter(Objects::nonNull)
                    .flatMap(r -> {
                        if (List.class.isAssignableFrom(method.getReturnType())) {
                            return ((List<R>) r).stream();
                        } else {
                            return Stream.of((R) r);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public static <T extends Model<?>> List<?> relatedKeyValueList(List<T> models, Field field) {
        return models.stream()
                .map(o -> ReflectUtil.getFieldValue(o, field))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
    }

    public static String getMorphAlias(Class<?> clazz) {
        return MORPH_MAP.inverse().computeIfAbsent(clazz, Class::getName);
    }

    @SneakyThrows
    public static Class<?> getMorphClass(String morphAlias) {
        return MORPH_MAP.computeIfAbsent(morphAlias, Class::forName);
    }
}
