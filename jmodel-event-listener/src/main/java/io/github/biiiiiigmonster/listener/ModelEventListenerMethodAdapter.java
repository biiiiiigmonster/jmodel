package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.ModelEventListener;
import io.github.biiiiiigmonster.event.ModelEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.event.TransactionalApplicationListenerMethodAdapter;

import java.lang.reflect.Method;

public class ModelEventListenerMethodAdapter extends TransactionalApplicationListenerMethodAdapter {
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
