package io.github.biiiiiigmonster.relation;

import io.github.biiiiiigmonster.Model;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes"})
public abstract class HasOneOrManyThrough<T extends Model<?>, TH extends Model<?>> extends Relation<T> {
    protected Class<TH> throughClass;
    protected Field foreignField;
    protected Field throughForeignField;
    protected Field localField;
    protected Field throughLocalField;

    public HasOneOrManyThrough(Field relatedField, List<RelationVia> viaList, Class<TH> throughClass, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField, viaList);

        this.throughClass = throughClass;
        this.foreignField = foreignField;
        this.throughForeignField = throughForeignField;
        this.localField = localField;
        this.throughLocalField = throughLocalField;
    }
}
