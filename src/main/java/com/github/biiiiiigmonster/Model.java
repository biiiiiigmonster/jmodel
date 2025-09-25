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
import java.util.*;

/**
 * @author luyunfeng
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class Model<T extends Model<?>> {
    @TableField(exist = false)
    private Pivot<?> pivot;

    @TableField(exist = false)
    private Map<String, Object> originalAttributes = new HashMap<>();

    @TableField(exist = false)
    private boolean exists = false;

    @TableField(exist = false)
    private boolean wasRecentlyCreated = false;

    public Model() {
        // 新模型默认是脏的，因为还没有保存
        this.exists = false;
        this.wasRecentlyCreated = true;
    }

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
        T model = relatedRepository.selectById(id);
        if (model != null) {
            model.markAsExisting();
        }
        return model;
    }

    public T first(Wrapper<Model<?>> queryWrapper) {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        List<T> list = relatedRepository.selectList((Wrapper<T>) queryWrapper);
        if (list.isEmpty()) {
            return null;
        }
        T model = list.get(0);
        model.markAsExisting();
        return model;
    }

    public Boolean save() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        int res;
        
        if (primaryKeyValue() == null || !exists) {
            // 新记录，执行插入
            res = relatedRepository.insert((T) this);
            if (res > 0) {
                exists = true;
                wasRecentlyCreated = true;
                syncOriginal();
            }
        } else {
            // 已存在的记录，检查是否有脏字段
            if (isDirty()) {
                // 只更新脏字段
                res = updateDirtyFields(relatedRepository);
            } else {
                // 没有脏字段，不需要更新
                res = 1;
            }
        }

        return res > 0;
    }

    /**
     * 只更新脏字段
     */
    private int updateDirtyFields(BaseMapper<T> repository) {
        // 获取脏字段
        Map<String, Object> dirtyAttributes = getDirty();
        
        if (dirtyAttributes.isEmpty()) {
            return 1; // 没有脏字段，不需要更新
        }

        // 直接更新当前对象，MyBatis-Plus会自动处理只更新非null字段
        int result = repository.updateById((T) this);
        
        if (result > 0) {
            // 更新成功后同步原始值
            syncOriginal();
        }
        
        return result;
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

    /**
     * 检查模型是否有脏字段（被修改的字段）
     */
    public boolean isDirty() {
        return !getDirty().isEmpty();
    }

    /**
     * 检查模型是否干净（没有脏字段）
     */
    public boolean isClean() {
        return !isDirty();
    }

    /**
     * 检查指定字段是否为脏字段
     */
    public boolean isDirty(String attribute) {
        return getDirty().containsKey(attribute);
    }

    /**
     * 检查指定字段是否为脏字段（使用lambda表达式）
     */
    public <R> boolean isDirty(SerializableFunction<T, R> column) {
        String attribute = SerializedLambda.getField(column).getName();
        return isDirty(attribute);
    }

    /**
     * 获取所有脏字段及其值
     */
    public Map<String, Object> getDirty() {
        Map<String, Object> dirty = new HashMap<>();
        
        // 获取所有字段
        Field[] fields = getClass().getDeclaredFields();
        
        for (Field field : fields) {
            // 跳过非数据库字段
            if (isNonDatabaseField(field)) {
                continue;
            }
            
            String fieldName = field.getName();
            Object currentValue = ReflectUtil.getFieldValue(this, fieldName);
            Object originalValue = originalAttributes.get(fieldName);
            
            // 比较当前值和原始值
            if (!Objects.equals(currentValue, originalValue)) {
                dirty.put(fieldName, currentValue);
            }
        }
        
        return dirty;
    }

    /**
     * 获取指定字段的脏值
     */
    public Object getDirty(String attribute) {
        return getDirty().get(attribute);
    }

    /**
     * 获取指定字段的脏值（使用lambda表达式）
     */
    public <R> R getDirty(SerializableFunction<T, R> column) {
        String attribute = SerializedLambda.getField(column).getName();
        return (R) getDirty(attribute);
    }

    /**
     * 获取原始值
     */
    public Object getOriginal(String attribute) {
        return originalAttributes.get(attribute);
    }

    /**
     * 获取原始值（使用lambda表达式）
     */
    public <R> R getOriginal(SerializableFunction<T, R> column) {
        String attribute = SerializedLambda.getField(column).getName();
        return (R) getOriginal(attribute);
    }

    /**
     * 同步原始值（将当前值设为原始值）
     */
    public void syncOriginal() {
        originalAttributes.clear();
        
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (isNonDatabaseField(field)) {
                continue;
            }
            
            String fieldName = field.getName();
            Object currentValue = ReflectUtil.getFieldValue(this, fieldName);
            originalAttributes.put(fieldName, currentValue);
        }
        
        exists = true;
        wasRecentlyCreated = false;
    }

    /**
     * 同步指定字段的原始值
     */
    public void syncOriginal(String... attributes) {
        for (String attribute : attributes) {
            Object currentValue = ReflectUtil.getFieldValue(this, attribute);
            originalAttributes.put(attribute, currentValue);
        }
    }

    /**
     * 同步指定字段的原始值（使用lambda表达式）
     */
    @SafeVarargs
    public final <R> void syncOriginal(SerializableFunction<T, R>... columns) {
        for (SerializableFunction<T, R> column : columns) {
            String attribute = SerializedLambda.getField(column).getName();
            Object currentValue = ReflectUtil.getFieldValue(this, attribute);
            originalAttributes.put(attribute, currentValue);
        }
    }

    /**
     * 检查模型是否存在于数据库中
     */
    public boolean exists() {
        return exists;
    }

    /**
     * 检查模型是否为新创建的
     */
    public boolean wasRecentlyCreated() {
        return wasRecentlyCreated;
    }

    /**
     * 标记模型为已存在（从数据库加载）
     */
    public void markAsExisting() {
        exists = true;
        wasRecentlyCreated = false;
        syncOriginal();
    }

    /**
     * 判断字段是否为非数据库字段
     */
    private boolean isNonDatabaseField(Field field) {
        // 跳过静态字段
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            return true;
        }
        
        // 跳过标记为@TableField(exist = false)的字段
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && !tableField.exist()) {
            return true;
        }
        
        // 跳过脏跟踪相关字段
        return "originalAttributes".equals(field.getName()) ||
               "exists".equals(field.getName()) ||
               "wasRecentlyCreated".equals(field.getName()) ||
               "pivot".equals(field.getName());
    }
}
