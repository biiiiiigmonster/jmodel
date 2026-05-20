package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelUpdatingEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelUpdatingEvent(Object source, T model) {
        super(source, model);
    }
}
