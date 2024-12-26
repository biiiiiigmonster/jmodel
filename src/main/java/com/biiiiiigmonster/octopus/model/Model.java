package com.biiiiiigmonster.octopus.model;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;

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

    @SafeVarargs
    public final <R> void append(SerializableFunction<T, R>... attributes) {
        AttributeUtils.append((T) this, SerializedLambda.resolveFieldNames(attributes));
    }

    public final void append(String... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    @SafeVarargs
    public final <R> void load(SerializableFunction<T, R>... withs) {
        RelationUtils.load((T) this, SerializedLambda.resolveFieldNames(withs));
    }

    public final void load(String... withs) {
        RelationUtils.load((T) this, withs);
    }

    @SafeVarargs
    public final <R> void loadForce(SerializableFunction<T, R>... withs) {
        RelationUtils.loadForce((T) this, SerializedLambda.resolveFieldNames(withs));
    }

    public final void loadForce(String... withs) {
        RelationUtils.loadForce((T) this, withs);
    }
}
