package com.github.biiiiiigmonster.model;

import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 9:52
 */
@Slf4j
public abstract class Model<T extends Model<?>> {

    public <R> R get(SerializableFunction<T, R> column) {
        R value = column.apply((T) this);
        if (value == null) {
            Field field = SerializedLambda.getField(column);
            if (field.getAnnotation(Relation.class) != null) {
                load(field.getName());
            } else if (field.getAnnotation(Attribute.class) != null) {
                append(field.getName());
            }
        }

        return column.apply((T) this);
    }

    // wip: event
    public Boolean save() {
        IService<T> relatedRepository = RelationUtils.getRelatedRepository((Class<T>) this.getClass());
        boolean result = relatedRepository.saveOrUpdate((T) this);

        return result;
    }

    // wip: event
    public Boolean delete() {
        IService<T> relatedRepository = RelationUtils.getRelatedRepository((Class<T>) this.getClass());
        boolean result = relatedRepository.removeById((T) this);

        return result;
    }

    @SafeVarargs
    public final void append(SerializableFunction<T, ?>... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    public final void append(String... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    @SafeVarargs
    public final void load(SerializableFunction<T, ?>... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SafeVarargs
    public final void load(RelationOption<T, ?>... relations) {
        RelationUtils.load((T) this, relations);
    }

    public final void load(String... relations) {
        RelationUtils.load((T) this, relations);
    }

    @SafeVarargs
    public final void loadForce(SerializableFunction<T, ?>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

    @SafeVarargs
    public final void loadForce(RelationOption<T, ?>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

    public final void loadForce(String... relations) {
        RelationUtils.loadForce((T) this, relations);
    }
}
