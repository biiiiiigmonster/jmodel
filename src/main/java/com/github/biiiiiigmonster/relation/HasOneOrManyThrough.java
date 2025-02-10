package com.github.biiiiiigmonster.relation;

import java.lang.reflect.Field;

public abstract class HasOneOrManyThrough extends Relation {
    public HasOneOrManyThrough(Field relatedField) {
        super(relatedField);
    }
}
