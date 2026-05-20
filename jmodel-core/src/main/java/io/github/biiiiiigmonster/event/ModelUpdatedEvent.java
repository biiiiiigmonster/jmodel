package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelUpdatedEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelUpdatedEvent(Object source, T model) {
        super(source, model);
    }
}
