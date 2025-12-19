package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.Model;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Model事件发布器
 * 负责在Model执行save/delete等操作时发布Spring事件
 *
 * @author jmodel-core
 */
@Component
public class ModelEventPublisher {

    private static ApplicationContext applicationContext;

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        applicationContext = context;
    }

    /**
     * 发布事件
     *
     * @param event 事件
     */
    public static void publishEvent(ApplicationEvent event) {
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        }
    }

    /**
     * 发布ModelSavingEvent
     */
    public static <T extends Model<?>> void publishSaving(T model) {
        publishEvent(new ModelSavingEvent<>(model, model));
    }

    /**
     * 发布ModelSavedEvent
     */
    public static <T extends Model<?>> void publishSaved(T model) {
        publishEvent(new ModelSavedEvent<>(model, model));
    }

    /**
     * 发布ModelCreatingEvent
     */
    public static <T extends Model<?>> void publishCreating(T model) {
        publishEvent(new ModelCreatingEvent<>(model, model));
    }

    /**
     * 发布ModelCreatedEvent
     */
    public static <T extends Model<?>> void publishCreated(T model) {
        publishEvent(new ModelCreatedEvent<>(model, model));
    }

    /**
     * 发布ModelUpdatingEvent
     */
    public static <T extends Model<?>> void publishUpdating(T model) {
        publishEvent(new ModelUpdatingEvent<>(model, model));
    }

    /**
     * 发布ModelUpdatedEvent
     */
    public static <T extends Model<?>> void publishUpdated(T model) {
        publishEvent(new ModelUpdatedEvent<>(model, model));
    }

    /**
     * 发布ModelDeletingEvent
     */
    public static <T extends Model<?>> void publishDeleting(T model) {
        publishEvent(new ModelDeletingEvent<>(model, model));
    }

    /**
     * 发布ModelDeletedEvent
     */
    public static <T extends Model<?>> void publishDeleted(T model) {
        publishEvent(new ModelDeletedEvent<>(model, model));
    }
}
