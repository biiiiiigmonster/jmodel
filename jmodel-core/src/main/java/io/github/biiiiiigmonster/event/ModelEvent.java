package io.github.biiiiiigmonster.event;

import io.github.biiiiiigmonster.Model;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 模型事件基类，所有 {@code ModelXxxEvent} 均继承此类。
 *
 * @param <T> 模型类型
 */
@Getter
public abstract class ModelEvent<T extends Model<?>> extends ApplicationEvent {

    private final T model;

    protected ModelEvent(Object source, T model) {
        super(source);
        this.model = model;
    }
}
