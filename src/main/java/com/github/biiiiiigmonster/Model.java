package com.github.biiiiiigmonster;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.attribute.AttributeUtils;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationOption;
import com.github.biiiiiigmonster.relation.RelationType;
import com.github.biiiiiigmonster.relation.RelationUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 9:52
 */
@SuppressWarnings("unchecked")
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

    public Relation relation(SerializableFunction<T, ?> column) {
        return RelationOption.of(column).getRelation().setModel(this);
    }

    public Relation relation(String relation) {
        return RelationOption.of(getClass(), relation).getRelation().setModel(this);
    }

    public T find(Serializable id) {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        return relatedRepository.selectById(id);
    }

    public T first(Wrapper<Model<?>> queryWrapper) {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        List<T> list = relatedRepository.selectList((Wrapper<T>) queryWrapper);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    // wip: event
    public Boolean save() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        int insert = relatedRepository.insert((T) this);

        return insert > 0;
    }

    // wip: event
    public Boolean delete() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        int i = relatedRepository.deleteById((T) this);

        return i > 0;
    }

    public final <R> void append(SerializableFunction<T, R>... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    public final void append(String... attributes) {
        AttributeUtils.append((T) this, attributes);
    }

    public final <R> void load(SerializableFunction<T, R>... relations) {
        RelationUtils.load((T) this, relations);
    }

    public final void load(RelationOption<T>... relations) {
        RelationUtils.load((T) this, relations);
    }

    public final void load(String... relations) {
        RelationUtils.load((T) this, relations);
    }

    public final <R> void loadForce(SerializableFunction<T, R>... relations) {
        RelationUtils.loadForce((T) this, relations);
    }

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
                && model.getClass() == getClass()
                && primaryKeyValue().equals(model.primaryKeyValue());
    }

    public boolean isNot(Model<?> model) {
        return !is(model);
    }
}
