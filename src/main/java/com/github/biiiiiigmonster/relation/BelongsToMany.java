package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;

public class BelongsToMany extends Relation {
    public BelongsToMany(Field relatedField) {
        super(relatedField);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        return List.of();
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {

    }
}
