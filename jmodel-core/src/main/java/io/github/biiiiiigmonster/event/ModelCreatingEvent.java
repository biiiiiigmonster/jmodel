package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelCreatingEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelCreatingEvent(Object source, T model) {
        super(source, model);
    }
}
