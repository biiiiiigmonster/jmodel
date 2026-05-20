package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelCreatedEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelCreatedEvent(Object source, T model) {
        super(source, model);
    }
}
