package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.SerializableFunction;
import com.github.biiiiiigmonster.SerializedLambda;
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
    private RelationType relationType;

    public RelationOption(SerializableFunction<T, R> relation) {
        this.relatedField = SerializedLambda.getField(relation);
        parse();
    }

    public RelationOption(Class<?> clazz, String fieldName) {
        this.relatedField = ReflectUtil.getField(clazz, fieldName);
        parse();
    }

    private void parse() {
        relationType = RelationType.of(relatedField);
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

    public boolean isRelatedFieldList() {
        return relationType.isResultList();
    }

    public Relation getRelation() {
        return relationType.getRelation(relatedField);
    }

    public boolean isNestedEmpty() {
        return CollectionUtils.isEmpty(nestedRelations);
    }
}
