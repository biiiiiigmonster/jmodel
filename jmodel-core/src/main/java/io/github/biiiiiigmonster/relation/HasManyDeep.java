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
public class HasManyDeep<T extends Model<?>> extends HasOneOrManyDeep<T> {
    public HasManyDeep(Field relatedField, List<RelationVia> viaList) {
        super(relatedField, viaList);
    }

    @Override
    public <R extends Model<?>> List<R> match(List<T> models, List<R> results) {
        List<Map<?, List<?>>> dictionaries = viaList.stream()
                .map(via -> (Map<?, List<?>>) via.getResults().stream()
                        .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, via.getForeignField()))))
                .collect(Collectors.toList());

        List<R> matchResults = new ArrayList<>();

        models.forEach(o -> {
            Stream<?> stream = Stream.of(o);
            for (int i = 0; i < viaList.size(); i++) {
                Map<?, List<?>> viaDictionary = dictionaries.get(i);
                Field viaLocalField = viaList.get(i).getLocalField();
                stream = stream.flatMap(m ->
                        viaDictionary.getOrDefault(ReflectUtil.getFieldValue(m, viaLocalField), new ArrayList<>()).stream()
                ).filter(Objects::nonNull);
            }

            List<R> valList = (List<R>) stream.collect(Collectors.toList());
            matchResults.addAll(valList);
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });

        return matchResults;
    }
}
