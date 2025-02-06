package com.github.biiiiiigmonster.model;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RelationOption<T extends Model<?>, R extends Model<?>> {
    private List<RelationOption<?, ?>> nestedRelations;
    private Field relatedField;

    public RelationOption(SerializableFunction<T, R> relation) {
        this.relatedField = SerializedLambda.getField(relation);
    }

    public RelationOption(Class<?> clazz, String fieldName) {
        this.relatedField = ReflectUtil.getField(clazz, fieldName);
    }

    public static <T extends Model<?>, R extends Model<?>> RelationOption<T, R> of(SerializableFunction<T, R> relation) {
        return new RelationOption<>(relation);
    }

    public static <T extends Model<?>, R extends Model<?>> RelationOption<T, R> of(Class<?> clazz, String fieldName) {
        return new RelationOption<>(clazz, fieldName);
    }

    public void nested(Object... relations) {
        nestedRelations = Arrays.stream(relations)
                .map(relation -> {
                    if (relation instanceof SerializableFunction) {
                        return of((SerializableFunction<T, R>) relation);
                    }
                    return relation;
                })
                .filter(relation -> relation instanceof RelationOption)
                .map(relation -> (RelationOption<?, ?>) relation)
                .collect(Collectors.toList());
    }

    public boolean isNestedEmpty() {
        return CollectionUtils.isEmpty(nestedRelations);
    }
}
