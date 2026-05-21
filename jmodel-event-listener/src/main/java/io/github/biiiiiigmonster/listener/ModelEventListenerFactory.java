package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.ModelEventListener;
import io.github.biiiiiigmonster.event.ModelEvent;
import lombok.Setter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.event.TransactionalApplicationListenerMethodAdapter;
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
@Setter
public class ModelEventListenerFactory implements EventListenerFactory, Ordered {

    private int order = 40;

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

    /**
     * 在 {@link TransactionalApplicationListenerMethodAdapter} 基础上仅扩展
     * {@link ModelEventListener#models()} 条件合并能力，其余事务同步、事件分发、
     * order 解析等行为完全沿用父类实现。
     */
    static final class ModelEventListenerMethodAdapter extends TransactionalApplicationListenerMethodAdapter {

        private final String mergedCondition;

        ModelEventListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
            super(beanName, targetClass, method);
            ModelEventListener modelEventListener = AnnotatedElementUtils.findMergedAnnotation(method, ModelEventListener.class);
            if (modelEventListener == null) {
                throw new IllegalStateException("No @ModelEventListener annotation found on method: " + method);
            }
            validateEventParameter(method);
            this.mergedCondition = ModelEventListenerConditionBuilder.build(
                    modelEventListener.models(),
                    modelEventListener.condition());
        }

        private static void validateEventParameter(Method method) {
            if (method.getParameterCount() == 1) {
                Class<?> parameterType = method.getParameterTypes()[0];
                if (!ModelEvent.class.isAssignableFrom(parameterType)) {
                    throw new IllegalStateException(
                            "@ModelEventListener method parameter must be a ModelEvent subtype, but got: "
                                    + parameterType.getName() + " on method: " + method);
                }
            }
        }

        @Override
        protected String getCondition() {
            return this.mergedCondition;
        }
    }
}
