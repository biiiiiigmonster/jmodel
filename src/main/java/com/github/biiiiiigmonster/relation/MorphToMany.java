package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        IService<MP> morphPivotRepository = (IService<MP>) RelationUtils.getRelatedRepository(morphPivotClass);
        Class<?> morphPivotTypeClass = inverse ? foreignField.getDeclaringClass() : localField.getDeclaringClass();
        QueryWrapper<MP> pivotWrapper = new QueryWrapper<>();
        pivotWrapper.eq(RelationUtils.getColumn(morphPivotType), Relation.getMorphAlias(morphPivotTypeClass))
                .in(RelationUtils.getColumn(foreignPivotField), localKeyValueList);
        List<MP> morphPivots = morphPivotRepository.list(pivotWrapper);
        List<?> relatedPivotKeyValueList = relatedKeyValueList(morphPivots, relatedPivotField);
        if (ObjectUtil.isEmpty(relatedPivotKeyValueList)) {
            return new ArrayList<>();
        }

        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), relatedPivotKeyValueList);
        List<R> results = relatedRepository.list(wrapper);
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r));
        Map<?, List<MP>> morphPivotDictionary = morphPivots.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignPivotField)));
        models.forEach(o -> {
            List<R> valList = morphPivotDictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>())
                    .stream()
                    .map(p -> dictionary.get(ReflectUtil.getFieldValue(p, relatedPivotField)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });

        return results;
    }
}
