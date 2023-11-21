package com.biiiiiigmonster.jmodel.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 18:08
 */
@Getter
public class QueryExecuted extends ApplicationEvent {
    private final String sql;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public QueryExecuted(Object source, String sql) {
        super(source);
        this.sql = sql;
    }
}
