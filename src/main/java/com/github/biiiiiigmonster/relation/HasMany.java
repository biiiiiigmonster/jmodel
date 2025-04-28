package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasMany extends HasOneOrMany {
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
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, List<R>> dictionary = results.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignField)));

        models.forEach(o -> {
            List<R> valList = dictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>());
            valList.forEach(r -> inverseMatch(r, o));
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }
}
