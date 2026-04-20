package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.SerializableFunction;
import io.github.biiiiiigmonster.SerializedLambda;
import io.github.biiiiiigmonster.relation.constraint.RelationConstraint;
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
    private final Class<T> clazz;
    private final String fieldName;
    private Field relatedField;
    private RelationType relationType;
    /**
     * 运行时约束，在 {@link #getRelation()} 时注入到 {@link Relation}
     */
    private RelationConstraint<?> runtimeConstraint;

    @SuppressWarnings("unchecked")
    public <F> RelationOption(SerializableFunction<T, F> relation) {
        SerializedLambda lambda = SerializedLambda.resolve(relation);
        clazz = (Class<T>) lambda.getImplClass();
        fieldName = SerializedLambda.methodToProperty(lambda.getImplMethodName());
        parse();
    }

    public RelationOption(Class<T> clazz, String fieldName) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        parse();
    }

    private void parse() {
        relatedField = ReflectUtil.getField(clazz, fieldName);
        relationType = RelationType.of(relatedField);
    }

    public static <T extends Model<?>, F> RelationOption<T> of(SerializableFunction<T, F> relation) {
        return new RelationOption<>(relation);
    }

    public static <T extends Model<?>> RelationOption<T> of(Class<T> clazz, String fieldName) {
        return new RelationOption<>(clazz, fieldName);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Model<?>> RelationOption<T> of(T model, String fieldName) {
        return new RelationOption<>((Class<T>) model.getClass(), fieldName);
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

    public <R extends Model<?>> void nested(List<RelationOption<R>> relations) {
        nestedRelations = new ArrayList<>(relations);
    }

    public boolean isRelatedFieldList() {
        return relationType.isResultList();
    }

    public Relation<T> getRelation() {
        Relation<T> relation = relationType.getRelation(this);
        if (runtimeConstraint != null) {
            relation.withRuntimeConstraint(runtimeConstraint);
        }
        return relation;
    }

    public boolean isNested() {
        return !CollectionUtils.isEmpty(nestedRelations);
    }

    /**
     * 设置运行时 {@link RelationConstraint} 形式的约束；
     * 由于 {@link RelationConstraint} 是函数式接口，可直接传入 lambda：
     * <pre>
     * option.constraint(c -&gt; c.like("title", "Spring"));
     * </pre>
     */
    public RelationOption<T> constraint(RelationConstraint<?> constraint) {
        this.runtimeConstraint = constraint;
        return this;
    }
}
