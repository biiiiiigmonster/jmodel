package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes"})
public class HasOneDeep<T extends Model<?>> extends HasOneOrManyDeep<T> {
    public HasOneDeep(Field relatedField, List<RelationVia> viaList) {
        super(relatedField, viaList);
    }

    @Override
    public <R extends Model<?>> List<R> match(List<T> models, List<R> results) {
        List<Map<?, ?>> dictionaries = viaList.stream()
                .map(via -> (Map<?, ?>) via.getResults().stream()
                        .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, via.getForeignField()), r -> r, (v1, v2) -> v1)))
                .collect(Collectors.toList());

        List<R> matchResults = new ArrayList<>();

        models.forEach(o -> {
            Stream<?> stream = Stream.of(o);
            for (int i = 0; i < viaList.size(); i++) {
                Map<?, ?> viaDictionary = dictionaries.get(i);
                Field viaLocalField = viaList.get(i).getLocalField();
                stream = stream.map(m ->
                        viaDictionary.get(ReflectUtil.getFieldValue(m, viaLocalField))
                ).filter(Objects::nonNull);
            }

            R value = (R) stream.findFirst().orElse(null);
            if (value != null) {
                matchResults.add(value);
            }

            ReflectUtil.setFieldValue(o, relatedField, value);
        });

        return matchResults;
    }
}
