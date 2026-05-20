package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.ModelEventListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.event.ModelEventListenerMethodAdapter;
import org.springframework.transaction.event.TransactionalEventListenerFactory;

import java.lang.reflect.Method;

/**
 * 处理 {@link ModelEventListener} 注解方法的 {@link EventListenerFactory}。
 * <p>
 * 继承 {@link TransactionalEventListenerFactory} 以复用其事务感知的事件分发能力，
 * 仅在 {@link #supportsMethod} 上识别 {@link ModelEventListener}，并交由
 * {@link ModelEventListenerMethodAdapter} 处理 {@link ModelEventListener#models()} 条件合并。
 * <p>
 * 默认 order 为 40，优先级高于 {@link TransactionalEventListenerFactory}（默认 50），
 * 以确保 {@link ModelEventListener} 注解的方法优先由本工厂处理。
 */
public class ModelEventListenerFactory implements EventListenerFactory, Ordered {

    private int order = 40;

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public boolean supportsMethod(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, ModelEventListener.class);
    }

    @Override
    public ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method) {
        return new ModelEventListenerMethodAdapter(beanName, type, method);
    }
}
