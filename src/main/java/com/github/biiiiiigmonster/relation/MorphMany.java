package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MorphMany extends MorphOneOrMany {
    /**
     * @param relatedField (Post|Video).comments
     * @param morphType Comment.commentable_type
     * @param foreignField Comment.commentable_id
     * @param localField (Post|Video).id
     */
    public MorphMany(Field relatedField, Field morphType, Field foreignField, Field localField) {
        super(relatedField, morphType, foreignField, localField);
    }

    @Override
    public <T extends Model<?>> void match(List<T> models, List<Model<?>> results) {
        if (ObjectUtil.isEmpty(results)) {
            return;
        }

        Map<?, List<Model<?>>> dictionary = results.stream()
                .filter(r -> ReflectUtil.getFieldValue(r, morphType) == Relation.getMorphAlias(localField.getDeclaringClass()))
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignField)));

        models.forEach(o -> {
            List<Model<?>> valList = dictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }
}
