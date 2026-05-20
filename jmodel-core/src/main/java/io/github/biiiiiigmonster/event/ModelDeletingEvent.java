package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelDeletingEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelDeletingEvent(Object source, T model) {
        super(source, model);
    }
}
