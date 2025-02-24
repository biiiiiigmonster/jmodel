package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MorphToMany extends BelongsToMany {
    protected Field morphPivotType;

    /**
     * @param relatedField      Post.tags                   Tag.posts
     * @param morphPivotType    Taggables.taggable_type     Taggables.taggable_type
     * @param foreignPivotField Taggables.taggable_id       Taggables.taggable_id
     * @param relatedPivotField Taggables.tag_id            Taggables.tag_id
     * @param foreignField      Tag.id                      Post.id
     * @param localField        Post.id                     Tag.id
     */
    public MorphToMany(Field relatedField, Field morphPivotType, Field foreignPivotField, Field relatedPivotField, Field foreignField, Field localField) {
        super(relatedField, foreignPivotField, relatedPivotField, foreignField, localField);

        this.morphPivotType = morphPivotType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        IService<?> morphPivotRepository = RelationUtils.getRelatedRepository(foreignPivotField.getDeclaringClass());
        QueryChainWrapper<?> pivotWrapper = morphPivotRepository.query()
                .eq(RelationUtils.getColumn(morphPivotType), Relation.getMorphAlias(localField.getDeclaringClass()))
                .in(RelationUtils.getColumn(foreignPivotField), localKeyValueList);
        List<MorphPivot<?>> morphPivots = (List<MorphPivot<?>>) pivotWrapper.list();
        List<?> relatedPivotKeyValueList = relatedKeyValueList(morphPivots, relatedPivotField);
        if (ObjectUtil.isEmpty(relatedPivotKeyValueList)) {
            return new ArrayList<>();
        }

        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query().in(RelationUtils.getColumn(foreignField), relatedPivotKeyValueList);
        List<R> results = relatedRepository.list(wrapper);
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));
        Map<?, List<MorphPivot<?>>> morphPivotDictionary = morphPivots.stream()
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
