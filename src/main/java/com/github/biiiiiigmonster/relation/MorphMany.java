package com.github.biiiiiigmonster.relation;

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
     * @param morphType    Comment.commentable_type
     * @param foreignField Comment.commentable_id
     * @param localField   (Post|Video).id
     * @param chaperone    chaperone
     */
    public MorphMany(Field relatedField, Field morphType, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField, morphType, foreignField, localField, chaperone);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, List<R>> dictionary = results.stream()
                .filter(r -> ReflectUtil.getFieldValue(r, morphType).equals(getMorphAlias()))
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
