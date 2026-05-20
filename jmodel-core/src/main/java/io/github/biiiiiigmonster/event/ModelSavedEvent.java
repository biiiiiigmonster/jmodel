package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelSavedEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelSavedEvent(Object source, T model) {
        super(source, model);
    }
}
