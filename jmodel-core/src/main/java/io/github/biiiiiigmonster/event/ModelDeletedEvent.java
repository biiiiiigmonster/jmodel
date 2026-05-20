package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelDeletedEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelDeletedEvent(Object source, T model) {
        super(source, model);
    }
}
