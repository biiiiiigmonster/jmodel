package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BelongsToMany<P extends Pivot<?>> extends Relation {
    protected Class<P> pivotClass;
    protected Field foreignPivotField;
    protected Field relatedPivotField;
    protected Field foreignField;
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

    // todo: pivot custom
    protected Field withPivotField() {
        return ReflectUtil.getField(foreignField.getDeclaringClass(), "pivot");
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
    }

    public Class<P> getPivotClass() {
        return pivotClass;
    }

    public Field getForeignPivotField() {
        return foreignPivotField;
    }

    public Field getRelatedPivotField() {
        return relatedPivotField;
    }

    public Field getForeignField() {
        return foreignField;
    }

    public Field getLocalField() {
        return localField;
    }
}
