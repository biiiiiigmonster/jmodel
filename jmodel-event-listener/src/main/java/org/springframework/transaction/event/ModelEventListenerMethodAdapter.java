package org.springframework.transaction.event;

import io.github.biiiiiigmonster.ModelEventListener;
import io.github.biiiiiigmonster.event.ModelEvent;
import io.github.biiiiiigmonster.listener.ModelEventListenerConditionBuilder;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/**
 * {@link ApplicationListenerMethodTransactionalAdapter} 的扩展，将 {@link ModelEventListener#models()}
 * 合并到 SpEL 条件中，其余事务同步、事件分发等行为完全沿用父类实现。
 * <p>
 * 因父类 {@link ApplicationListenerMethodTransactionalAdapter} 在 Spring 框架中为包私有，
 * 本类必须置于 {@code org.springframework.transaction.event} 包下才能完成继承。
 */
public class ModelEventListenerMethodAdapter extends ApplicationListenerMethodTransactionalAdapter {

    private final String mergedCondition;

    public ModelEventListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
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
