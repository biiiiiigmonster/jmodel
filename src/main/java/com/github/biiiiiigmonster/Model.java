package com.github.biiiiiigmonster;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.attribute.AttributeUtils;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationOption;
import com.github.biiiiiigmonster.relation.RelationType;
import com.github.biiiiiigmonster.relation.RelationUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 9:52
 */
public abstract class Model<T extends Model<?>> {

    @SuppressWarnings("unchecked")
    public <R> R get(SerializableFunction<T, R> column) {
        R value = column.apply((T) this);
        if (value == null) {
            Field field = SerializedLambda.getField(column);
            if (RelationType.hasRelationAnnotation(field)) {
                load(column);
            } else if (AttributeUtils.hasAttributeAnnotation(field)) {
                append(column);
            }
        }

        return column.apply((T) this);
    }

    public <R> Relation relation(SerializableFunction<T, R> column) {
        return RelationOption.of(column).getRelation();
    }

    @SuppressWarnings("unchecked")
    public T find(Serializable id) {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        return relatedRepository.selectById(id);
    }

    @SuppressWarnings("unchecked")
    public T first(Wrapper<?> queryWrapper) {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        return relatedRepository.selectList((Wrapper<T> )queryWrapper).get(0);
    }

    public <R extends Model<?>> boolean isAssociate(Model<?> model) {
        Random random = new Random();
        return random.nextBoolean();
    }

    // wip: event
    @SuppressWarnings("unchecked")
    public Boolean save() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        int insert = relatedRepository.insert((T) this);

        return insert > 0;
    }

    // wip: event
    @SuppressWarnings("unchecked")
    public Boolean delete() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        int i = relatedRepository.deleteById((T) this);

        return i > 0;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <R> void append(SerializableFunction<T, R>... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    @SuppressWarnings("unchecked")
    public final void append(String... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <R> void load(SerializableFunction<T, R>... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final void load(RelationOption<T>... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SuppressWarnings("unchecked")
    public final void load(String... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <R> void loadForce(SerializableFunction<T, R>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final void loadForce(RelationOption<T>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

    @SuppressWarnings("unchecked")
    public final void loadForce(String... relations) {
        RelationUtils.loadForce((T) this, relations);
    }
}
