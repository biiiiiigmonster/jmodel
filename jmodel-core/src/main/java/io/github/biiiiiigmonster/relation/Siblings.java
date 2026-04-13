package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Siblings<T extends Model<?>> extends Relation<T> {
    protected Field parentField;

    /**
     * @param relatedField Post.siblingsUserPosts
     * @param parentField  Post.user_id
     */
    public Siblings(Field relatedField, @NonNull Field parentField) {
        super(relatedField);

        this.parentField = parentField;
    }

    @Override
    public <R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> parentKeyValueList = relatedKeyValueList(models, parentField);
        return getResult(parentKeyValueList, parentField);
    }

    @Override
    public <R extends Model<?>> List<R> match(List<T> models, List<R> results) {
        Map<?, Map<Object, R>> dictionary = results.stream()
                .collect(Collectors.groupingBy(
                        r -> ReflectUtil.getFieldValue(r, parentField),
                        Collectors.toMap(Model::primaryKeyValue, r -> r)
                ));

        List<R> matchResults = new ArrayList<>();

        models.forEach(o -> {
            List<R> siblingsList = new ArrayList<>();
            Map<Object, R> valMap = dictionary.get(ReflectUtil.getFieldValue(o, parentField));
            if (valMap != null) {
                Map<Object, R> copyMap = new HashMap<>(valMap);
                copyMap.remove(o.primaryKeyValue());
                siblingsList = new ArrayList<>(copyMap.values());
            }
            matchResults.addAll(siblingsList);
            ReflectUtil.setFieldValue(o, relatedField, siblingsList);
        });

        return matchResults;
    }
}
