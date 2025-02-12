package com.github.biiiiiigmonster;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.attribute.AttributeUtils;
import com.github.biiiiiigmonster.relation.RelationOption;
import com.github.biiiiiigmonster.relation.RelationType;
import com.github.biiiiiigmonster.relation.RelationUtils;

import java.lang.reflect.Field;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 9:52
 */
public abstract class Model<T extends Model<?>> {

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

    // wip: event
    public Boolean save() {
        IService<T> relatedRepository = (IService<T>) RelationUtils.getRelatedRepository(this.getClass());
        boolean result = relatedRepository.saveOrUpdate((T) this);

        return result;
    }

    // wip: event
    public Boolean delete() {
        IService<T> relatedRepository = (IService<T>) RelationUtils.getRelatedRepository(this.getClass());
        boolean result = relatedRepository.removeById((T) this);

        return result;
    }

    @SafeVarargs
    public final <R> void append(SerializableFunction<T, R>... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    public final void append(String... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    @SafeVarargs
    public final <R> void load(SerializableFunction<T, R>... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SafeVarargs
    public final void load(RelationOption<T>... relations) {
        RelationUtils.load((T) this, relations);
    }

    public final void load(String... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SafeVarargs
    public final <R> void loadForce(SerializableFunction<T, R>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

    @SafeVarargs
    public final void loadForce(RelationOption<T>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

    public final void loadForce(String... relations) {
        RelationUtils.loadForce((T) this, relations);
    }
}
