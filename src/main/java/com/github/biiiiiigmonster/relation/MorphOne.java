package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MorphOne extends MorphOneOrMany {
    /**
     * @param relatedField (Post|User).image
     * @param morphType Image.imageable_type
     * @param foreignField Image.imageable_id
     * @param localField (Post|User).id
     */
    public MorphOne(Field relatedField, Field morphType, Field foreignField, Field localField) {
        super(relatedField, morphType, foreignField, localField);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        if (ObjectUtil.isEmpty(results)) {
            return;
        }

        Map<?, R> dictionary = results.stream()
                .filter(r -> ReflectUtil.getFieldValue(r, morphType) == Relation.getMorphAlias(localField.getDeclaringClass()))
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));

        models.forEach(o -> {
            R value = dictionary.get(ReflectUtil.getFieldValue(o, localField));
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
