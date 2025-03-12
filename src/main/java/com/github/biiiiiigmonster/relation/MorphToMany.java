package com.github.biiiiiigmonster.relation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.lang.reflect.Field;
import java.util.List;

public class MorphToMany<MP extends MorphPivot<?>> extends BelongsToMany<MP> {
    protected Class<MP> morphPivotClass;
    protected Field morphPivotType;
    protected boolean inverse;

    /**
     * @param relatedField      Post.tags                   Tag.posts
     * @param morphPivotType    Taggables.taggable_type     Taggables.taggable_type
     * @param foreignPivotField Taggables.taggable_id       Taggables.tag_id
     * @param relatedPivotField Taggables.tag_id            Taggables.taggable_id
     * @param foreignField      Tag.id                      Post.id
     * @param localField        Post.id                     Tag.id
     */
    public MorphToMany(Field relatedField, Class<MP> morphPivotClass, Field morphPivotType, Field foreignPivotField, Field relatedPivotField, Field foreignField, Field localField, boolean inverse) {
        super(relatedField, morphPivotClass, foreignPivotField, relatedPivotField, foreignField, localField);

        this.morphPivotClass = morphPivotClass;
        this.morphPivotType = morphPivotType;
        this.inverse = inverse;
    }

    @SuppressWarnings("unchecked")
    protected List<MP> byPivotRelatedRepository(List<?> keys) {
        BaseMapper<MP> morphPivotRepository = (BaseMapper<MP>) RelationUtils.getRelatedRepository(morphPivotClass);
        QueryWrapper<MP> pivotWrapper = new QueryWrapper<>();
        pivotWrapper.in(RelationUtils.getColumn(foreignPivotField), keys)
                .eq(RelationUtils.getColumn(morphPivotType), getMorphAlias());
        return morphPivotRepository.selectList(pivotWrapper);
    }

    protected Object[] additionalRelatedMethodArgs(Object obj) {
        return new Object[]{obj, getMorphAlias()};
    }

    protected String getMorphAlias() {
        return inverse
                ? Relation.getMorphAlias(foreignField.getDeclaringClass(), localField.getDeclaringClass())
                : Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }
}
