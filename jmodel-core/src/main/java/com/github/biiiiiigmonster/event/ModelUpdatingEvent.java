package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.Model;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ModelUpdatingEvent<T extends Model<?>> extends ApplicationEvent {
    private final T model;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param model model
     */
    public ModelUpdatingEvent(Object source, T model) {
        super(source);
        this.model = model;
    }
}
