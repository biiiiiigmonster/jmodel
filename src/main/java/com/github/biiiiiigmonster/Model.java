package com.github.biiiiiigmonster;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.attribute.AttributeUtils;
import com.github.biiiiiigmonster.relation.Pivot;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationOption;
import com.github.biiiiiigmonster.relation.RelationType;
import com.github.biiiiiigmonster.relation.RelationUtils;
import lombok.Getter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author luyunfeng
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class Model<T extends Model<?>> {
    @TableField(exist = false)
    private Pivot<?> pivot;

    public <R> R get(SerializableFunction<T, R> column) {
        R value = column.apply((T) this);
        if (value != null) {
            return value;
        }

        Field field = SerializedLambda.getField(column);
        if (RelationType.hasRelationAnnotation(field)) {
            load(column);
        } else if (AttributeUtils.hasAttributeAnnotation(field)) {
            append(column);
        }

        return column.apply((T) this);
    }

    public Object get(String column) {
        Object value = ReflectUtil.getFieldValue(this, column);
        if (value != null) {
            return value;
        }

        Field field = ReflectUtil.getField(getClass(), column);
        if (RelationType.hasRelationAnnotation(field)) {
            load(column);
        } else if (AttributeUtils.hasAttributeAnnotation(field)) {
            append(column);
        }

        return ReflectUtil.getFieldValue(this, column);
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

    /**
     * 保存关联模型
     * 支持一对一、一对多关联
     */
    @SafeVarargs
    public final <R> void save(SerializableFunction<T, R>... relations) {
        RelationUtils.saveRelations((T) this, relations);
    }

    /**
     * 保存关联模型（字符串方式）
     */
    public final void save(String... relations) {
        RelationUtils.saveRelations((T) this, relations);
    }

    /**
     * 创建并保存关联模型
     * 支持一对一、一对多关联
     */
    public final <R> R create(SerializableFunction<T, R> relation, R model) {
        return RelationUtils.createRelation((T) this, relation, model);
    }

    /**
     * 创建并保存关联模型（字符串方式）
     */
    public final <R> R create(String relation, R model) {
        return RelationUtils.createRelation((T) this, relation, model);
    }

    /**
     * 附加关联（多对多）
     */
    @SafeVarargs
    public final <R> void attach(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.attachRelations((T) this, relation, models);
    }

    /**
     * 附加关联（多对多，字符串方式）
     */
    public final <R> void attach(String relation, R... models) {
        RelationUtils.attachRelations((T) this, relation, models);
    }

    /**
     * 分离关联（多对多）
     */
    @SafeVarargs
    public final <R> void detach(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.detachRelations((T) this, relation, models);
    }

    /**
     * 分离关联（多对多，字符串方式）
     */
    public final <R> void detach(String relation, R... models) {
        RelationUtils.detachRelations((T) this, relation, models);
    }

    /**
     * 同步关联（多对多）
     */
    @SafeVarargs
    public final <R> void sync(SerializableFunction<T, List<R>> relation, R... models) {
        RelationUtils.syncRelations((T) this, relation, models);
    }

    /**
     * 同步关联（多对多，字符串方式）
     */
    public final <R> void sync(String relation, R... models) {
        RelationUtils.syncRelations((T) this, relation, models);
    }

    /**
     * 更新关联模型
     */
    public final <R> void update(SerializableFunction<T, R> relation, R model) {
        RelationUtils.updateRelation((T) this, relation, model);
    }

    /**
     * 更新关联模型（字符串方式）
     */
    public final <R> void update(String relation, R model) {
        RelationUtils.updateRelation((T) this, relation, model);
    }

    /**
     * 切换多对多关联
     * @param relation 关联方法引用
     * @param ids 要切换的ID列表
     * @param <R> 关联模型类型
     * @return 切换后的关联ID列表
     */
    public <R extends Model> List<Long> toggle(SerializableFunction<R, ?> relation, List<Long> ids) {
        return RelationUtils.toggle(this, relation, ids);
    }

    /**
     * 切换多对多关联（单个ID）
     * @param relation 关联方法引用
     * @param id 要切换的ID
     * @param <R> 关联模型类型
     * @return 切换后的关联ID列表
     */
    public <R extends Model> List<Long> toggle(SerializableFunction<R, ?> relation, Long id) {
        return toggle(relation, Arrays.asList(id));
    }

    /**
     * 切换多对多关联（字符串方式）
     * @param relationName 关联名称
     * @param ids 要切换的ID列表
     * @return 切换后的关联ID列表
     */
    public List<Long> toggle(String relationName, List<Long> ids) {
        return RelationUtils.toggle(this, relationName, ids);
    }

    /**
     * 切换多对多关联（字符串方式，单个ID）
     * @param relationName 关联名称
     * @param id 要切换的ID
     * @return 切换后的关联ID列表
     */
    public List<Long> toggle(String relationName, Long id) {
        return toggle(relationName, Arrays.asList(id));
    }
}
