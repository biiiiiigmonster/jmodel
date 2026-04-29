package io.github.biiiiiigmonster.relation;

import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings({"rawtypes"})
public abstract class HasOneOrManyDeep<T extends Model<?>> extends Relation<T>{
    public HasOneOrManyDeep(Field relatedField, List<RelationVia> viaList) {
        super(relatedField, viaList);
    }
}
