package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;

public class ModelSavingEvent<T extends Model<?>> extends ModelEvent<T> {

    public ModelSavingEvent(Object source, T model) {
        super(source, model);
    }
}
