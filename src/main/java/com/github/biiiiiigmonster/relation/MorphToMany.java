package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unchecked")
public class MorphToMany<MP extends MorphPivot<?>> extends BelongsToMany<MP> {
    protected Class<MP> morphPivotClass;
    protected Field morphPivotType;
    protected boolean inverse;

    /**
     * @param relatedField      Post.tags                   Tag.posts
     * @param morphPivotClass   Morph pivot class
     * @param morphPivotType    Taggables.taggable_type     Taggables.taggable_type
     * @param foreignPivotField Taggables.taggable_id       Taggables.tag_id
     * @param relatedPivotField Taggables.tag_id            Taggables.taggable_id
     * @param foreignField      Tag.id                      Post.id
     * @param localField        Post.id                     Tag.id
     * @param inverse           inverse
     * @param withPivot         with pivot
     */
    public MorphToMany(Field relatedField, Class<MP> morphPivotClass, Field morphPivotType, Field foreignPivotField, Field relatedPivotField, Field foreignField, Field localField, boolean inverse, boolean withPivot) {
        super(relatedField, morphPivotClass, foreignPivotField, relatedPivotField, foreignField, localField, withPivot);

        this.morphPivotClass = morphPivotClass;
        this.morphPivotType = morphPivotType;
        this.inverse = inverse;
    }

    protected List<MP> byPivotRelatedRepository(List<?> keys) {
        BaseMapper<MP> morphPivotRepository = (BaseMapper<MP>) RelationUtils.getRelatedRepository(morphPivotClass);
        QueryWrapper<MP> pivotWrapper = new QueryWrapper<>();
        pivotWrapper.in(RelationUtils.getColumn(foreignPivotField), keys)
                .eq(RelationUtils.getColumn(morphPivotType), getMorphAlias());
        return morphPivotRepository.selectList(pivotWrapper);
    }

    protected Object[] additionalRelatedMethodArgs() {
        return new Object[]{getMorphAlias()};
    }

    protected String getMorphAlias() {
        return inverse
                ? Relation.getMorphAlias(foreignField.getDeclaringClass(), localField.getDeclaringClass())
                : Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }

    protected void pivotSave(MP pivot, Object localValue, Object foreignValue) {
        ReflectUtil.setFieldValue(pivot, morphPivotType, getMorphAlias());
        super.pivotSave(pivot, localValue, foreignValue);
    }

    protected void pivotDelete(QueryWrapper<MP> wrapper) {
        wrapper.eq(RelationUtils.getColumn(morphPivotType), getMorphAlias());
        super.pivotDelete(wrapper);
    }
}
