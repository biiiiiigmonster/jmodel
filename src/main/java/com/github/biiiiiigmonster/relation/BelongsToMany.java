package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public class BelongsToMany<P extends Pivot<?>> extends Relation {
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
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<P> pivots = getPivotResult(models);
        List<R> results = getForeignResult(pivots);
        pivotMatch(models, pivots, results);

        return results;
    }

    protected <T extends Model<?>> List<P> getPivotResult(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        return getResult(localKeyValueList, foreignPivotField, this::byPivotRelatedRepository);
    }

    protected List<P> byPivotRelatedRepository(List<?> keys) {
        BaseMapper<P> pivotRepository = (BaseMapper<P>) RelationUtils.getRelatedRepository(pivotClass);
        QueryWrapper<P> pivotWrapper = new QueryWrapper<>();
        pivotWrapper.in(RelationUtils.getColumn(foreignPivotField), keys);
        return pivotRepository.selectList(pivotWrapper);
    }

    protected <R extends Model<?>> List<R> getForeignResult(List<P> pivots) {
        List<?> relatedPivotKeyValueList = relatedKeyValueList(pivots, relatedPivotField);
        return getResult(relatedPivotKeyValueList, foreignField, this::byRelatedRepository);
    }

    protected <R extends Model<?>> List<R> byRelatedRepository(List<?> keys) {
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), keys);
        return relatedRepository.selectList(wrapper);
    }

    protected <T extends Model<?>, R extends Model<?>> void pivotMatch(List<T> models, List<P> pivots, List<R> results) {
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
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
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
            // 删除中间表记录
            QueryWrapper<P> wrapper = new QueryWrapper<>();
            wrapper.eq(RelationUtils.getColumn(foreignPivotField), localValue)
                    .in(RelationUtils.getColumn(relatedPivotField), foreignValues);
            pivotDelete(wrapper);
        }
    }

    public void detachAll() {
        Object localValue = ReflectUtil.getFieldValue(model, localField);

        QueryWrapper<P> wrapper = new QueryWrapper<>();
        wrapper.eq(RelationUtils.getColumn(foreignPivotField), localValue);
        pivotDelete(wrapper);
    }

    protected void pivotDelete(QueryWrapper<P> wrapper) {
        BaseMapper<P> pivotMapper = (BaseMapper<P>) RelationUtils.getRelatedRepository(pivotClass);
        pivotMapper.delete(wrapper);
    }

    public <R extends Model<?>> void sync(List<R> syncModels, boolean detaching) {
        List<R> current = getEager(Lists.newArrayList(model));
        Set<Object> currentSet = current.stream().map(Model::primaryKeyValue).collect(Collectors.toSet());
        Set<Object> syncSet = syncModels.stream().map(Model::primaryKeyValue).collect(Collectors.toSet());

        attach(syncModels.stream().filter(m -> !currentSet.contains(m.primaryKeyValue())).collect(Collectors.toList()));
        if (detaching) {
            detach(current.stream().filter(m -> !syncSet.contains(m.primaryKeyValue())).collect(Collectors.toList()));
        }
    }

    public <R extends Model<?>> void toggle(List<R> toggleModels) {
        List<R> current = getEager(Lists.newArrayList(model));
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
