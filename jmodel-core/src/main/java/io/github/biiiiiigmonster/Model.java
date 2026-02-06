package io.github.biiiiiigmonster;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.attribute.AttributeUtils;
import io.github.biiiiiigmonster.driver.DataDriver;
import io.github.biiiiiigmonster.driver.DriverRegistry;
import io.github.biiiiiigmonster.event.ModelEventPublisher;
import io.github.biiiiiigmonster.relation.Pivot;
import io.github.biiiiiigmonster.relation.RelationOption;
import io.github.biiiiiigmonster.relation.RelationType;
import io.github.biiiiiigmonster.relation.RelationUtils;
import io.github.biiiiiigmonster.tracking.TrackingUtils;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author luyunfeng
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class Model<T extends Model<?>> {
    private transient Pivot<?> pivot;

    // ==================== Dirty Tracking Internal Fields ====================

    /**
     * 原始值快照（惰性初始化）
     * key: 字段名, value: 原始值
     * null 表示尚未建立快照（UNTRACKED 状态）
     */
    private transient Map<String, Object> $jmodel$original = null;

    /**
     * 已变更的字段名集合（setter 调用时记录）
     */
    private transient Set<String> $jmodel$changes = new HashSet<>();

    /**
     * 最近一次 save 涉及的变更（save 后保留，供 wasChanged 使用）
     */
    private transient Map<String, Object> $jmodel$savedChanges = new HashMap<>();

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

    // ==================== Dirty Tracking: Track Change ====================

    /**
     * 记录字段变更（由增强后的 setter 调用）
     * <p>
     * 方法名使用 $jmodel$ 前缀避免与用户方法冲突。
     * 在未追踪状态（original == null）下不做任何事。
     *
     * @param field    字段名
     * @param oldValue 旧值（当前字段值）
     * @param newValue 新值（即将设置的值）
     */
    public void $jmodel$trackChange(String field, Object oldValue, Object newValue) {
        // 未追踪状态下不做任何事
        if (this.$jmodel$original == null) {
            return;
        }

        // 追踪状态下：与原始值对比，决定是否标记为变更
        Object originalValue = this.$jmodel$original.get(field);
        if (!Objects.equals(originalValue, newValue)) {
            this.$jmodel$changes.add(field);
        } else {
            // 如果恢复为原始值，移除变更标记
            this.$jmodel$changes.remove(field);
        }
    }

    // ==================== Dirty Tracking: Sync Original ====================

    /**
     * 将当前所有字段值同步为原始值，并清空变更记录。
     * <p>
     * 调用后实体进入 TRACKING 状态，后续 setter 修改将被追踪。
     */
    public void syncOriginal() {
        this.$jmodel$original = new HashMap<>();
        for (Field field : TrackingUtils.getTrackableFields(getClass())) {
            Object value = ReflectUtil.getFieldValue(this, field);
            this.$jmodel$original.put(field.getName(), value);
        }
        this.$jmodel$changes.clear();
    }

    /**
     * 将指定字段的当前值同步为原始值。
     * <p>
     * 如果尚未开始追踪，则先执行全量同步。
     *
     * @param fields 字段名
     */
    public void syncOriginal(String... fields) {
        if (this.$jmodel$original == null) {
            syncOriginal();
            return;
        }
        for (String field : fields) {
            Object value = ReflectUtil.getFieldValue(this, field);
            this.$jmodel$original.put(field, value);
            this.$jmodel$changes.remove(field);
        }
    }

    // ==================== Dirty Tracking: isDirty ====================

    /**
     * 判断是否有任何字段被修改
     */
    public boolean isDirty() {
        return !this.$jmodel$changes.isEmpty();
    }

    /**
     * 判断指定字段是否被修改（任一匹配即返回 true）
     *
     * @param fields 字段名
     */
    public boolean isDirty(String... fields) {
        for (String field : fields) {
            if (this.$jmodel$changes.contains(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定字段是否被修改（类型安全版本）
     *
     * @param columns 字段引用（如 User::getName）
     */
    @SafeVarargs
    public final <R> boolean isDirty(SerializableFunction<T, R>... columns) {
        List<String> fieldNames = SerializedLambda.resolveFieldNames(columns);
        return isDirty(fieldNames.toArray(new String[0]));
    }

    // ==================== Dirty Tracking: getDirty ====================

    /**
     * 获取所有脏字段及其当前值
     *
     * @return Map&lt;字段名, 当前值&gt;
     */
    public Map<String, Object> getDirty() {
        Map<String, Object> dirty = new HashMap<>();
        for (String field : this.$jmodel$changes) {
            dirty.put(field, ReflectUtil.getFieldValue(this, field));
        }
        return dirty;
    }

    /**
     * 获取指定字段的脏数据（only 过滤）
     *
     * @param fields 要获取的字段名
     */
    public Map<String, Object> getDirty(String... fields) {
        Map<String, Object> dirty = new HashMap<>();
        for (String field : fields) {
            if (this.$jmodel$changes.contains(field)) {
                dirty.put(field, ReflectUtil.getFieldValue(this, field));
            }
        }
        return dirty;
    }

    // ==================== Dirty Tracking: getOriginal ====================

    /**
     * 获取所有原始值
     *
     * @return 不可变的原始值 Map，未追踪时返回空 Map
     */
    public Map<String, Object> getOriginal() {
        if (this.$jmodel$original == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(this.$jmodel$original);
    }

    /**
     * 获取指定字段的原始值
     *
     * @param field 字段名
     * @return 原始值，未追踪时返回 null
     */
    public Object getOriginal(String field) {
        if (this.$jmodel$original == null) {
            return null;
        }
        return this.$jmodel$original.get(field);
    }

    /**
     * 获取指定字段的原始值，如果不存在则返回默认值
     *
     * @param field        字段名
     * @param defaultValue 默认值
     */
    public Object getOriginal(String field, Object defaultValue) {
        if (this.$jmodel$original == null) {
            return defaultValue;
        }
        return this.$jmodel$original.getOrDefault(field, defaultValue);
    }

    /**
     * 获取指定字段的原始值（类型安全版本）
     *
     * @param column 字段引用（如 User::getName）
     */
    public <R> R getOriginal(SerializableFunction<T, R> column) {
        Field field = SerializedLambda.getField(column);
        return (R) getOriginal(field.getName());
    }

    // ==================== Dirty Tracking: wasChanged / getChanges ====================

    /**
     * 判断最近一次 save 是否有任何变更
     */
    public boolean wasChanged() {
        return !this.$jmodel$savedChanges.isEmpty();
    }

    /**
     * 判断指定字段在最近一次 save 中是否变更（任一匹配即返回 true）
     *
     * @param fields 字段名
     */
    public boolean wasChanged(String... fields) {
        for (String field : fields) {
            if (this.$jmodel$savedChanges.containsKey(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最近一次 save 的所有变更
     *
     * @return 不可变的变更 Map
     */
    public Map<String, Object> getChanges() {
        return Collections.unmodifiableMap(this.$jmodel$savedChanges);
    }

    // ==================== Dirty Tracking: Detect Untracked Changes ====================

    /**
     * 快照对比（兜底检测）
     * <p>
     * 遍历所有可追踪字段，对比 original 与当前值，
     * 检测未通过 setter 追踪到的变更（如直接字段赋值、反射赋值）。
     */
    private void detectUntrackedChanges() {
        if (this.$jmodel$original == null) {
            return;
        }

        for (Field field : TrackingUtils.getTrackableFields(getClass())) {
            String name = field.getName();
            Object originalValue = this.$jmodel$original.get(name);
            Object currentValue = ReflectUtil.getFieldValue(this, field);

            if (!Objects.equals(originalValue, currentValue)) {
                this.$jmodel$changes.add(name);
            }
        }
    }

    // ==================== save / delete ====================

    public Boolean save() {
        DataDriver driver = DriverRegistry.getDriver((Class<? extends Model<?>>) getClass());
        boolean isNew = primaryKeyValue() == null;
        int res;

        // 1. 记录是否处于未追踪状态
        boolean wasUntracked = (this.$jmodel$original == null);

        // 2. 如果未追踪，先建立快照（用于 wasChanged 检测）
        if (wasUntracked) {
            syncOriginal();
        }

        // 3. 快照对比（兜底检测：直接赋值和反射赋值）
        detectUntrackedChanges();

        // 4. 保存前的变更快照（供 wasChanged 使用）
        Map<String, Object> currentChanges = new HashMap<>(getDirty());

        // 发布saving事件（通用）
        ModelEventPublisher.publishSaving((T) this);

        if (isNew) {
            // 发布creating事件
            ModelEventPublisher.publishCreating((T) this);
            res = driver.insert(this);
            if (res > 0) {
                // 发布created事件
                ModelEventPublisher.publishCreated((T) this);
            }
        } else {
            // 发布updating事件
            ModelEventPublisher.publishUpdating((T) this);
            res = driver.update(this);
            if (res > 0) {
                // 发布updated事件
                ModelEventPublisher.publishUpdated((T) this);
            }
        }

        if (res > 0) {
            // 发布saved事件（通用）
            ModelEventPublisher.publishSaved((T) this);

            // 保存成功后：记录本次变更，重置追踪状态
            this.$jmodel$savedChanges = currentChanges;
            syncOriginal();
        }

        return res > 0;
    }

    public Boolean delete() {
        DataDriver driver = DriverRegistry.getDriver((Class<? extends Model<?>>) getClass());

        // 发布deleting事件
        ModelEventPublisher.publishDeleting((T) this);

        int result = driver.delete(this);

        if (result > 0) {
            // 发布deleted事件
            ModelEventPublisher.publishDeleted((T) this);
        }

        return result > 0;
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
        return ReflectUtil.getFieldValue(this, RelationUtils.getPrimaryKey((Class<? extends Model<?>>) getClass()));
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
