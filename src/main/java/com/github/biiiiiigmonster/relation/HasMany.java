package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasMany extends HasOneOrMany {
    public HasMany(Field relatedField, Field localField, Field foreignField) {
        super(relatedField, localField, foreignField);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        if (ObjectUtil.isEmpty(results)) {
            return;
        }

        Map<?, List<R>> dictionary = results.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignField)));

        models.forEach(o -> {
            List<R> valList = dictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }
}
