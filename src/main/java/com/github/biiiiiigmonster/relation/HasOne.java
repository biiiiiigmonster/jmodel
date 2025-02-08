package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasOne extends HasOneOrMany {
    public HasOne(Field relatedField, Field localField, Field foreignField) {
        super(relatedField, localField, foreignField);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        if (ObjectUtil.isEmpty(results)) {
            return;
        }

        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));

        models.forEach(o -> {
            R value = dictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), null);
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
