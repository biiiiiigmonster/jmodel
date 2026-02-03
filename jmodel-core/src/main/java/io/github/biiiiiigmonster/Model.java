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
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author luyunfeng
 */
@Getter
@SuppressWarnings("unchecked")
public abstract class Model<T extends Model<?>> {
    private transient Pivot<?> pivot;

    // ==================== Dirty Tracking Fields ====================

    /**
     * 原始值快照（惰性初始化：首次 setter 调用时创建）
     * key: 字段名, value: 原始值
     * 初始值为 null，表示尚未建立快照
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

    public Boolean save() {
        DataDriver driver = DriverRegistry.getDriver((Class<? extends Model<?>>) getClass());
        boolean isNew = primaryKeyValue() == null;
        int res;

        // === Dirty Tracking: 保存前处理 ===
        // 1. 如果 original 为 null（从未调用过 setter），先建立快照
        if (this.$jmodel$original == null) {
            syncOriginal();
        }

        // 2. 快照对比（兜底检测：自快照建立后的直接赋值和反射赋值）
        $jmodel$detectUntrackedChanges();

        // 3. 保存前的变更快照（供 wasChanged 使用）
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

            // === Dirty Tracking: 保存成功后处理 ===
            this.$jmodel$savedChanges = currentChanges;
            syncOriginal();  // 重置快照为当前值
            this.$jmodel$changes.clear();  // 清空变更记录
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

    // ==================== Dirty Tracking Methods ====================

    /**
     * 记录字段变更（由增强后的 setter 调用）
     * 方法名使用 $jmodel$ 前缀避免与用户方法冲突
     *
     * @param field    字段名
     * @param oldValue 旧值（setter 调用前的值）
     * @param newValue 新值（setter 将要设置的值）
     */
    public void $jmodel$trackChange(String field, Object oldValue, Object newValue) {
        // 惰性初始化：首次追踪时创建快照
        if (this.$jmodel$original == null) {
            syncOriginal();
        }

        // 比较新值与原始值（不是 oldValue，因为可能有多次修改）
        Object originalValue = this.$jmodel$original.get(field);
        if (!Objects.equals(originalValue, newValue)) {
            this.$jmodel$changes.add(field);
        } else {
            // 如果恢复为原始值，移除变更标记
            this.$jmodel$changes.remove(field);
        }
    }

    /**
     * 将当前所有字段值同步为原始值，并清空变更记录
     */
    public void syncOriginal() {
        this.$jmodel$original = new HashMap<>();
        for (Field field : $jmodel$getTrackableFields()) {
            this.$jmodel$original.put(field.getName(), ReflectUtil.getFieldValue(this, field));
        }
        this.$jmodel$changes.clear();
    }

    /**
     * 将指定字段的当前值同步为原始值
     *
     * @param fields 要同步的字段名
     */
    public void syncOriginal(String... fields) {
        if (this.$jmodel$original == null) {
            this.$jmodel$original = new HashMap<>();
        }
        Set<String> fieldSet = new HashSet<>(Arrays.asList(fields));
        for (Field field : $jmodel$getTrackableFields()) {
            if (fieldSet.contains(field.getName())) {
                this.$jmodel$original.put(field.getName(), ReflectUtil.getFieldValue(this, field));
                this.$jmodel$changes.remove(field.getName());
            }
        }
    }

    /**
     * 获取可追踪的字段列表
     * 排除：关系字段、transient 字段、static 字段
     *
     * @return 可追踪字段数组
     */
    private Field[] $jmodel$getTrackableFields() {
        return Arrays.stream(ReflectUtil.getFields(getClass()))
                .filter(field -> {
                    // 排除 static 字段
                    if (Modifier.isStatic(field.getModifiers())) {
                        return false;
                    }
                    // 排除 transient 字段
                    if (Modifier.isTransient(field.getModifiers())) {
                        return false;
                    }
                    // 排除关系字段
                    if (RelationType.hasRelationAnnotation(field)) {
                        return false;
                    }
                    // 排除 Attribute 计算属性字段
                    if (AttributeUtils.hasAttributeAnnotation(field)) {
                        return false;
                    }
                    return true;
                })
                .toArray(Field[]::new);
    }

    // ==================== isDirty Methods (P1-03) ====================

    /**
     * 判断是否有任何字段被修改
     *
     * @return 如果有字段被修改返回 true
     */
    public boolean isDirty() {
        // 如果从未建立快照，尝试进行快照对比
        if (this.$jmodel$original == null) {
            return false;
        }
        // 先执行快照对比检测未追踪的变更
        $jmodel$detectUntrackedChanges();
        return !this.$jmodel$changes.isEmpty();
    }

    /**
     * 判断指定字段是否被修改
     *
     * @param fields 字段名
     * @return 如果指定的任一字段被修改返回 true
     */
    public boolean isDirty(String... fields) {
        if (this.$jmodel$original == null) {
            return false;
        }
        // 先执行快照对比检测未追踪的变更
        $jmodel$detectUntrackedChanges();
        for (String field : fields) {
            if (this.$jmodel$changes.contains(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定字段是否被修改（类型安全）
     *
     * @param columns 字段引用
     * @return 如果指定的任一字段被修改返回 true
     */
    @SafeVarargs
    public final <R> boolean isDirty(SerializableFunction<T, R>... columns) {
        String[] fields = Arrays.stream(columns)
                .map(col -> SerializedLambda.getField(col).getName())
                .toArray(String[]::new);
        return isDirty(fields);
    }

    // ==================== getDirty Methods (P1-04) ====================

    /**
     * 获取所有脏字段及其当前值
     *
     * @return Map<字段名, 当前值>
     */
    public Map<String, Object> getDirty() {
        if (this.$jmodel$original == null) {
            return new HashMap<>();
        }
        // 先执行快照对比检测未追踪的变更
        $jmodel$detectUntrackedChanges();
        Map<String, Object> dirty = new HashMap<>();
        for (String fieldName : this.$jmodel$changes) {
            dirty.put(fieldName, ReflectUtil.getFieldValue(this, fieldName));
        }
        return dirty;
    }

    /**
     * 获取指定字段的脏数据（only 过滤）
     *
     * @param fields 要获取的字段名
     * @return 只包含指定字段的脏数据
     */
    public Map<String, Object> getDirty(String... fields) {
        Map<String, Object> allDirty = getDirty();
        Set<String> fieldSet = new HashSet<>(Arrays.asList(fields));
        return allDirty.entrySet().stream()
                .filter(entry -> fieldSet.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // ==================== getOriginal Methods (P1-05) ====================

    /**
     * 获取所有原始值
     *
     * @return 原始值的副本
     */
    public Map<String, Object> getOriginal() {
        if (this.$jmodel$original == null) {
            return new HashMap<>();
        }
        return new HashMap<>(this.$jmodel$original);
    }

    /**
     * 获取指定字段的原始值
     *
     * @param field 字段名
     * @return 原始值，如果不存在返回 null
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
     * @return 原始值，如果不存在返回默认值
     */
    public Object getOriginal(String field, Object defaultValue) {
        Object value = getOriginal(field);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取指定字段的原始值（类型安全）
     *
     * @param column 字段引用
     * @return 原始值
     */
    public <R> R getOriginal(SerializableFunction<T, R> column) {
        String fieldName = SerializedLambda.getField(column).getName();
        return (R) getOriginal(fieldName);
    }

    // ==================== wasChanged & getChanges Methods (P1-06) ====================

    /**
     * 判断最近一次 save 是否有任何变更
     *
     * @return 如果最近一次 save 有变更返回 true
     */
    public boolean wasChanged() {
        return !this.$jmodel$savedChanges.isEmpty();
    }

    /**
     * 判断指定字段在最近一次 save 中是否变更
     *
     * @param fields 字段名
     * @return 如果指定的任一字段在最近 save 中变更返回 true
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
     * @return 变更字段及其保存时的值
     */
    public Map<String, Object> getChanges() {
        return new HashMap<>(this.$jmodel$savedChanges);
    }

    // ==================== Internal Detection Method (P1-09) ====================

    /**
     * 快照对比检测未追踪的变更
     * 检测直接字段赋值和反射赋值导致的变更
     */
    private void $jmodel$detectUntrackedChanges() {
        if (this.$jmodel$original == null) {
            return;
        }
        for (Field field : $jmodel$getTrackableFields()) {
            String name = field.getName();
            Object original = this.$jmodel$original.get(name);
            Object current = ReflectUtil.getFieldValue(this, field);

            if (!Objects.equals(original, current)) {
                this.$jmodel$changes.add(name);
            }
        }
    }
}
