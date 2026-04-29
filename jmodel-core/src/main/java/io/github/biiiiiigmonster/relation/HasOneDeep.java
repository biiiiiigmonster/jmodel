package io.github.biiiiiigmonster.relation;

import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes"})
public class HasOneDeep<T extends Model<?>> extends HasOneOrManyDeep<T> {
    public HasOneDeep(Field relatedField, List<RelationVia> viaList) {
        super(relatedField, viaList);
    }

    @Override
    public <R extends Model<?>> List<R> match(List<T> models, List<R> results) {
        return Collections.emptyList();
    }
}
