package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasMany<T extends Model<?>> extends HasOneOrMany<T> {
    /**
     * @param relatedField Post.comments
     * @param foreignField Comment.post_id
     * @param localField   Post.id
     * @param chaperone    chaperone
     */
    public HasMany(Field relatedField, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField, foreignField, localField, chaperone);
    }

    @Override
    public <R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, List<R>> dictionary = results.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignField)));

        Field chaperoneField = chaperoneField();
        models.forEach(o -> {
            List<R> valList = dictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>());
            if (chaperone) {
                valList.forEach(value -> ReflectUtil.setFieldValue(value, chaperoneField, o));
            }
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }
}
