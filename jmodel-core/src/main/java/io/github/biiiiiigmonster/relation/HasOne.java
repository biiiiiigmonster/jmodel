package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasOne<T extends Model<?>> extends HasOneOrMany<T> {
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
    public <R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r));

        Field chaperoneField = chaperoneField();
        models.forEach(o -> {
            R value = dictionary.get(ReflectUtil.getFieldValue(o, localField));
            if (chaperone && value != null) {
                ReflectUtil.setFieldValue(value, chaperoneField, o);
            }
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
