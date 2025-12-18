package com.github.biiiiiigmonster;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.attribute.AttributeUtils;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
import com.github.biiiiiigmonster.driver.QueryCondition;
import com.github.biiiiiigmonster.driver.annotation.Transient;
import com.github.biiiiiigmonster.relation.Pivot;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationOption;
import com.github.biiiiiigmonster.relation.RelationType;
import com.github.biiiiiigmonster.relation.RelationUtils;
import lombok.Getter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author luyunfeng
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class Model<T extends Model<?>> {
    @Transient
    private Pivot<?> pivot;

    public <R> R get(SerializableFunction<T, R> column) {
        R value = column.apply((T) this);
        if (value != null) {
            return value;
        }

        Field field = SerializedLambda.getField(column);
        get(field);

        return column.apply((T) this);
    }

    public Object get(String column) {
        return get(column, Object.class);
    }

    public <R> R get(String column, Class<R> type) {
        Object value = ReflectUtil.getFieldValue(this, column);
        if (value != null) {
            return type.cast(value);
        }

        Field field = ReflectUtil.getField(getClass(), column);
        get(field);

        return type.cast(ReflectUtil.getFieldValue(this, column));
    }

    private void get(Field field) {
        if (RelationType.hasRelationAnnotation(field)) {
            load(field.getName());
        } else if (AttributeUtils.hasAttributeAnnotation(field)) {
            append(field.getName());
        }
    }

    public Relation relation(SerializableFunction<T, ?> column) {
        return RelationOption.of(column).getRelation().setModel(this);
    }

    public Relation relation(String relation) {
        return RelationOption.of(getClass(), relation).getRelation().setModel(this);
    }

    public T find(Serializable id) {
        DataDriver<T> driver = DriverRegistry.getDriver((Class<T>) getClass());
        return driver.findById((Class<T>) getClass(), id);
    }

    public T first(QueryCondition condition) {
        DataDriver<T> driver = DriverRegistry.getDriver((Class<T>) getClass());
        List<T> results = driver.findByCondition((Class<T>) getClass(), condition);
        return results.isEmpty() ? null : results.get(0);
    }

    // wip: event
    public Boolean save() {
        DataDriver<T> driver = DriverRegistry.getDriver((Class<T>) getClass());
        if (primaryKeyValue() == null) {
            return driver.insert((T) this);
        } else {
            return driver.update((T) this);
        }
    }

    // wip: event
    public Boolean delete() {
        DataDriver<T> driver = DriverRegistry.getDriver((Class<T>) getClass());
        return driver.delete((T) this);
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

    public Object primaryKeyValue() {
        return ReflectUtil.getFieldValue(this, RelationUtils.getPrimaryKey(getClass()));
    }

    public boolean is(Model<?> model) {
        return model != null
                && getClass().equals(model.getClass())
                && primaryKeyValue().equals(model.primaryKeyValue());
    }

    public boolean isNot(Model<?> model) {
        return !is(model);
    }

    public final <R extends Model<?>> void associate(SerializableFunction<T, R> relation, R model) {
        RelationUtils.associateRelations((T) this, relation, model);
    }

    public final <R extends Model<?>> void associate(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.associateRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void associate(SerializableFunction<T, List<R>> relation, List<R> models) {
        RelationUtils.associateRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void associate(String relation, R... models) {
        RelationUtils.associateRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void associate(String relation, List<R> models) {
        RelationUtils.associateRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void attach(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.attachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void attach(SerializableFunction<T, List<R>> relation, List<R> models) {
        RelationUtils.attachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void attach(String relation, R... models) {
        RelationUtils.attachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void attach(String relation, List<R> models) {
        RelationUtils.attachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void detach(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.detachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void detach(SerializableFunction<T, List<R>> relation, List<R> models) {
        RelationUtils.detachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void detach(String relation, R... models) {
        RelationUtils.detachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void detach(String relation, List<R> models) {
        RelationUtils.detachRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void sync(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.syncRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void sync(SerializableFunction<T, List<R>> relation, List<R> models) {
        RelationUtils.syncRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void sync(String relation, R... models) {
        RelationUtils.syncRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void sync(String relation, List<R> models) {
        RelationUtils.syncRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void syncWithoutDetaching(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.syncWithoutDetachingRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void syncWithoutDetaching(SerializableFunction<T, List<R>> relation, List<R> models) {
        RelationUtils.syncWithoutDetachingRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void syncWithoutDetaching(String relation, R... models) {
        RelationUtils.syncWithoutDetachingRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void syncWithoutDetaching(String relation, List<R> models) {
        RelationUtils.syncWithoutDetachingRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void toggle(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.toggleRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void toggle(SerializableFunction<T, List<R>> relation, List<R> models) {
        RelationUtils.toggleRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void toggle(String relation, R... models) {
        RelationUtils.toggleRelations((T) this, relation, models);
    }

    public final <R extends Model<?>> void toggle(String relation, List<R> models) {
        RelationUtils.toggleRelations((T) this, relation, models);
    }
}
