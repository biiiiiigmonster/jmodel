package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.config.Morph;
import com.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import com.github.biiiiiigmonster.relation.annotation.config.MorphName;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Slf4j
@SuppressWarnings("unchecked")
public abstract class Relation {
    protected Field relatedField;
    protected Model<?> model;

    private static final Map<String, String> MORPH_ALIAS_MAP = new HashMap<>();

    private static final Map<String, String[]> MORPH_MAP = new HashMap<>();

    public Relation(Field relatedField) {
        this.relatedField = relatedField;
    }

    public abstract <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models);

    public abstract <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results);

    public <T extends Model<?>> List<T> getResult(List<?> keys, Field relatedField, Function<List<?>, List<T>> func) {
        if (ObjectUtil.isEmpty(keys)) {
            return new ArrayList<>();
        }

        return RelationUtils.hasRelatedRepository(relatedField)
                ? func.apply(keys)
                : byRelatedMethod(keys, relatedField, additionalRelatedMethodArgs());
    }

    public static <T extends Model<?>> List<T> byRelatedMethod(List<?> localKeyValueList, Field relatedField, Object... args) {
        Map<Object, Method> relatedMethod = RelationUtils.getRelatedMethod(relatedField);
        Object bean = relatedMethod.keySet().iterator().next();
        Method method = relatedMethod.values().iterator().next();
        if (List.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return ReflectUtil.invoke(bean, method, localKeyValueList, args);
        } else {
            log.warn("{}存在N + 1查询隐患，建议{}实现List参数的仓库方法", bean.getClass().getName(), method.getName());
            return localKeyValueList.stream()
                    .map(param -> ReflectUtil.invoke(bean, method, param, args))
                    .filter(Objects::nonNull)
                    .flatMap(r -> {
                        if (List.class.isAssignableFrom(method.getReturnType())) {
                            return ((List<T>) r).stream();
                        } else {
                            return Stream.of((T) r);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    protected Object[] additionalRelatedMethodArgs() {
        return new Object[]{};
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
        return MORPH_ALIAS_MAP.computeIfAbsent(key, k -> Arrays.stream(clazz.getAnnotationsByType(MorphAlias.class))
                .filter(m -> m.in().length == 0 || Arrays.stream(m.in()).collect(Collectors.toSet()).contains(within))
                .max(Comparator.comparingInt(m -> m.in().length))
                .map(m -> StringUtils.isBlank(m.value()) ? clazz.getSimpleName() : m.value())
                .orElse(clazz.getName()));
    }

    public static String[] getMorph(Class<?> clazz) {
        String key = clazz.getName();
        return MORPH_MAP.computeIfAbsent(key, k -> {
            Morph morph = clazz.getAnnotation(Morph.class);
            if (morph != null) {
                return new String[]{morph.type(), morph.id()};
            }

            String name = StrUtil.lowerFirst(clazz.getSimpleName());
            MorphName morphName = clazz.getAnnotation(MorphName.class);
            if (morphName != null && StringUtils.isNotBlank(morphName.value())) {
                name = morphName.value();
            }

            String type = String.format("%sType", name);
            String id = String.format("%sId", name);
            return new String[]{type, id};
        });
    }

    public Relation setModel(Model<?> model) {
        this.model = model;
        return this;
    }
}
