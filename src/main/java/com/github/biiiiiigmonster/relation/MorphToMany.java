package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    protected <T extends Model<?>> List<MP> getPivotResult(List<T> models) {
        List<MP> morphPivots = new ArrayList<>();
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isNotEmpty(localKeyValueList)) {
            BaseMapper<MP> morphPivotRepository = (BaseMapper<MP>) RelationUtils.getRelatedRepository(morphPivotClass);
            String morphAlias = inverse
                    ? Relation.getMorphAlias(foreignField.getDeclaringClass(), localField.getDeclaringClass())
                    : Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
            QueryWrapper<MP> pivotWrapper = new QueryWrapper<>();
            pivotWrapper.in(RelationUtils.getColumn(foreignPivotField), localKeyValueList)
                    .eq(RelationUtils.getColumn(morphPivotType), morphAlias);
            morphPivots = morphPivotRepository.selectList(pivotWrapper);
        }

        return morphPivots;
    }
}
