package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
import com.github.biiiiiigmonster.driver.QueryCondition;
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

        if (!RelationUtils.hasRelatedRepository(relatedField)) {
            throw new IllegalStateException("No data driver found for entity: " + relatedField.getDeclaringClass().getName());
        }

        return func.apply(keys);
    }

    public <T extends Model<?>> List<T> getResult(List<?> keys, Field relatedField) {
        Class<T> relatedClass = (Class<T>) relatedField.getDeclaringClass();
        DataDriver<T> driver = DriverRegistry.getDriver(relatedClass);
        String columnName = RelationUtils.getColumn(relatedField);
        QueryCondition condition = QueryCondition.byFieldValues(columnName, keys);
        return driver.findByCondition(relatedClass, condition);
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
