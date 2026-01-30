package io.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.DataDriver;
import io.github.biiiiiigmonster.driver.DriverRegistry;
import io.github.biiiiiigmonster.driver.QueryCondition;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class BelongsToMany<T extends Model<?>, P extends Pivot<?>> extends Relation<T> {
    @Getter
    protected Class<P> pivotClass;
    @Getter
    protected Field foreignPivotField;
    @Getter
    protected Field relatedPivotField;
    @Getter
    protected Field foreignField;
    @Getter
    protected Field localField;
    protected boolean withPivot;

    /**
     * @param relatedField      User.roles
     * @param pivotClass        Pivot class
     * @param foreignPivotField UserRole.user_id
     * @param relatedPivotField UserRole.role_id
     * @param localField        User.id
     * @param foreignField      Role.id
     * @param withPivot         with pivot
     */
    public BelongsToMany(Field relatedField, Class<P> pivotClass, Field foreignPivotField, Field relatedPivotField, Field foreignField, Field localField, boolean withPivot) {
        super(relatedField);

        this.pivotClass = pivotClass;
        this.foreignPivotField = foreignPivotField;
        this.relatedPivotField = relatedPivotField;
        this.foreignField = foreignField;
        this.localField = localField;
        this.withPivot = withPivot;
    }

    @Override
    public <R extends Model<?>> List<R> getEager(List<T> models) {
        List<P> pivots = getPivotResult(models);
        List<R> results = getForeignResult(pivots);
        pivotMatch(models, pivots, results);

        return results;
    }

    protected List<P> getPivotResult(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (CollectionUtils.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        return getResult(getPivotClass(), pivotConditionEnhancer(localKeyValueList));
    }

    protected Consumer<QueryCondition<P>> pivotConditionEnhancer(List<?> keys) {
        String columnName = RelationUtils.getColumn(foreignPivotField);
        return condition -> condition.in(columnName, keys);
    }

    protected <R extends Model<?>> List<R> getForeignResult(List<P> pivots) {
        List<?> relatedPivotKeyValueList = relatedKeyValueList(pivots, relatedPivotField);
        return getResult(relatedPivotKeyValueList, foreignField);
    }

    protected <R extends Model<?>> void pivotMatch(List<T> models, List<P> pivots, List<R> results) {
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r));
        Map<?, List<P>> pivotDictionary = pivots.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignPivotField)));

        Field withPivotField = withPivotField();
        models.forEach(o -> {
            List<R> valList = pivotDictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>())
                    .stream()
                    .map(p -> {
                        R r = dictionary.get(ReflectUtil.getFieldValue(p, relatedPivotField));
                        if (withPivot && r != null) {
                            ReflectUtil.setFieldValue(r, withPivotField, p);
                        }
                        return r;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }

    protected Field withPivotField() {
        return ReflectUtil.getField(foreignField.getDeclaringClass(), "pivot");
    }

    @Override
    public <R extends Model<?>> void match(List<T> models, List<R> results) {
    }

    public <R extends Model<?>> void attach(List<R> attachModels) {
        Object localValue = ReflectUtil.getFieldValue(model, localField);
        for (R attachModel : attachModels) {
            Object foreignValue = ReflectUtil.getFieldValue(attachModel, foreignField);
            try {
                P pivot = pivotClass.getDeclaredConstructor().newInstance();
                pivotSave(pivot, localValue, foreignValue);
            } catch (Exception e) {
                log.error("Failed to create pivot record", e);
            }
        }
    }

    protected void pivotSave(P pivot, Object localValue, Object foreignValue) {
        ReflectUtil.setFieldValue(pivot, foreignPivotField, localValue);
        ReflectUtil.setFieldValue(pivot, relatedPivotField, foreignValue);

        pivot.save();
    }

    public <R extends Model<?>> void detach(List<R> detachModels) {
        Object localValue = ReflectUtil.getFieldValue(model, localField);
        List<Object> foreignValues = new ArrayList<>();

        for (R relatedModel : detachModels) {
            Object foreignValue = ReflectUtil.getFieldValue(relatedModel, foreignField);
            foreignValues.add(foreignValue);
        }

        if (!foreignValues.isEmpty()) {
            // 删除中间表记录 - 使用驱动接口查询后删除
            pivotDeleteByCondition(localValue, foreignValues);
        }
    }

    public void detachAll() {
        Object localValue = ReflectUtil.getFieldValue(model, localField);
        pivotDeleteByCondition(localValue, null);
    }

    /**
     * 删除中间表记录
     *
     * @param localValue    本地键值
     * @param foreignValues 外键值列表，如果为 null 则删除所有匹配本地键的记录
     */
    protected void pivotDeleteByCondition(Object localValue, List<Object> foreignValues) {
        DataDriver driver = DriverRegistry.getDriver(pivotClass);
        String foreignPivotColumn = RelationUtils.getColumn(foreignPivotField);

        QueryCondition<P> condition = QueryCondition.create(pivotClass)
                .eq(foreignPivotColumn, localValue);

        if (foreignValues != null && !foreignValues.isEmpty()) {
            String relatedPivotColumn = RelationUtils.getColumn(relatedPivotField);
            condition.in(relatedPivotColumn, foreignValues);
        }

        // 查询要删除的记录
        List<P> toDelete = driver.findByCondition(condition);
        // 逐个删除
        for (P pivot : toDelete) {
            pivot.delete();
        }
    }

    public <R extends Model<?>> void sync(List<R> syncModels, boolean detaching) {
        List<R> current = getEager(ListUtil.toList(model));
        Set<Object> currentSet = current.stream().map(Model::primaryKeyValue).collect(Collectors.toSet());
        Set<Object> syncSet = syncModels.stream().map(Model::primaryKeyValue).collect(Collectors.toSet());

        attach(syncModels.stream().filter(m -> !currentSet.contains(m.primaryKeyValue())).collect(Collectors.toList()));
        if (detaching) {
            detach(current.stream().filter(m -> !syncSet.contains(m.primaryKeyValue())).collect(Collectors.toList()));
        }
    }

    public <R extends Model<?>> void toggle(List<R> toggleModels) {
        List<R> current = getEager(ListUtil.toList(model));
        Set<Object> currentSet = current.stream().map(Model::primaryKeyValue).collect(Collectors.toSet());
        List<R> detach = new ArrayList<>();
        List<R> attach = new ArrayList<>();

        for (R relatedModel : toggleModels) {
            if (currentSet.contains(relatedModel.primaryKeyValue())) {
                detach.add(relatedModel);
            } else {
                attach.add(relatedModel);
            }
        }

        detach(detach);
        attach(attach);
    }
}
