package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasOne extends HasOneOrMany {
    /**
     * @param relatedField User.phone
     * @param foreignField Phone.user_id
     * @param localField   User.id
     * @param chaperone    chaperone
     */
    public HasOne(Field relatedField, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField, foreignField, localField, chaperone);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r));

        models.forEach(o -> {
            R value = dictionary.get(ReflectUtil.getFieldValue(o, localField));
            inverseMatch(value, o);
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
