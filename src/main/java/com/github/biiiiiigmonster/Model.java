package com.github.biiiiiigmonster;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.attribute.AttributeUtils;
import com.github.biiiiiigmonster.event.ModelEventManager;
import com.github.biiiiiigmonster.relation.Pivot;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationOption;
import com.github.biiiiiigmonster.relation.RelationType;
import com.github.biiiiiigmonster.relation.RelationUtils;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author luyunfeng
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class Model<T extends Model<?>> implements ApplicationContextAware {
    @TableField(exist = false)
    private Pivot<?> pivot;
    
    private static ModelEventManager eventManager;

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
        T result = relatedRepository.selectById(id);
        
        // 检索后事件
        if (eventManager != null && result != null) {
            eventManager.fireRetrieved(result);
        }
        
        return result;
    }

    public T first(Wrapper<Model<?>> queryWrapper) {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        List<T> list = relatedRepository.selectList((Wrapper<T>) queryWrapper);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public Boolean save() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        int res;

        // 保存前事件
        if (eventManager != null) {
            eventManager.fireSaving((T) this);
        }

        if (primaryKeyValue() == null) {
            // 创建前事件
            if (eventManager != null) {
                eventManager.fireCreating((T) this);
            }
            
            res = relatedRepository.insert((T) this);
            
            // 创建后事件
            if (eventManager != null && res > 0) {
                eventManager.fireCreated((T) this);
            }
        } else {
            // 更新前事件
            if (eventManager != null) {
                eventManager.fireUpdating((T) this);
            }
            
            res = relatedRepository.updateById((T) this);
            
            // 更新后事件
            if (eventManager != null && res > 0) {
                eventManager.fireUpdated((T) this);
            }
        }
        
        // 保存后事件
        if (eventManager != null && res > 0) {
            eventManager.fireSaved((T) this);
        }

        return res > 0;
    }

    public Boolean delete() {
        BaseMapper<T> relatedRepository = (BaseMapper<T>) RelationUtils.getRelatedRepository(getClass());
        
        // 删除前事件
        if (eventManager != null) {
            eventManager.fireDeleting((T) this);
        }
        
        int i = relatedRepository.deleteById((T) this);
        
        // 删除后事件
        if (eventManager != null && i > 0) {
            eventManager.fireDeleted((T) this);
        }

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
     * 静音当前模型的事件
     */
    public static void muteEvents(Class<?> modelClass) {
        if (eventManager != null) {
            eventManager.mute(modelClass);
        }
    }
    
    /**
     * 取消静音当前模型的事件
     */
    public static void unmuteEvents(Class<?> modelClass) {
        if (eventManager != null) {
            eventManager.unmute(modelClass);
        }
    }
    
    /**
     * 静音当前模型的事件
     */
    public void muteEvents() {
        muteEvents(getClass());
    }
    
    /**
     * 取消静音当前模型的事件
     */
    public void unmuteEvents() {
        unmuteEvents(getClass());
    }
    
    /**
     * 检查当前模型的事件是否被静音
     */
    public static boolean isEventsMuted(Class<?> modelClass) {
        return eventManager != null && eventManager.isModelMuted(modelClass);
    }
    
    /**
     * 检查当前模型的事件是否被静音
     */
    public boolean isEventsMuted() {
        return isEventsMuted(getClass());
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (eventManager == null) {
            eventManager = applicationContext.getBean(ModelEventManager.class);
        }
    }
}
