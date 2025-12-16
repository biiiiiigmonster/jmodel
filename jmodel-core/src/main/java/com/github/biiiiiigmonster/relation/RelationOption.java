package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.SerializableFunction;
import com.github.biiiiiigmonster.SerializedLambda;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RelationOption<T extends Model<?>> {
    private List<RelationOption<? extends Model<?>>> nestedRelations = new ArrayList<>();
    private final Field relatedField;
    private RelationType relationType;

    public <F> RelationOption(SerializableFunction<T, F> relation) {
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

    public static <T extends Model<?>, F> RelationOption<T> of(SerializableFunction<T, F> relation) {
        return new RelationOption<>(relation);
    }

    public static <T extends Model<?>> RelationOption<T> of(Class<?> clazz, String fieldName) {
        return new RelationOption<>(clazz, fieldName);
    }

    @SafeVarargs
    public final <R extends Model<?>, F> RelationOption<T> appendNested(SerializableFunction<R, F>... relations) {
        nestedRelations.addAll(Arrays.stream(relations).map(RelationOption::of).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final <R extends Model<?>> RelationOption<T> appendNested(RelationOption<R>... relations) {
        nestedRelations.addAll(Arrays.stream(relations).collect(Collectors.toList()));
        return this;
    }

    public void nested(List<RelationOption<?>> relations) {
        nestedRelations = relations;
    }

    public boolean isRelatedFieldList() {
        return relationType.isResultList();
    }

    public Relation getRelation() {
        return relationType.getRelation(relatedField);
    }

    public boolean isNested() {
        return !CollectionUtils.isEmpty(nestedRelations);
    }
}
