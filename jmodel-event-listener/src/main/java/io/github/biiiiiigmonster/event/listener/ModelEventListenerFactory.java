package io.github.biiiiiigmonster.event.listener;

import io.github.biiiiiigmonster.ModelEventListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

/**
 * 处理 {@link ModelEventListener} 注解方法的 {@link EventListenerFactory}。
 * 优先级高于 {@link org.springframework.transaction.event.TransactionalEventListenerFactory}。
 */
public class ModelEventListenerFactory implements EventListenerFactory, Ordered {

    static final int ORDER = 40;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supportsMethod(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, ModelEventListener.class);
    }

    @Override
    public ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method) {
        return new ModelEventListenerMethodAdapter(beanName, type, method);
    }

    static final class ModelEventListenerMethodAdapter extends ApplicationListenerMethodAdapter {

        private final TransactionalEventListener transactionalEventListener;
        private final String mergedCondition;

        ModelEventListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
            super(beanName, targetClass, method);
            ModelEventListener modelEventListener = AnnotatedElementUtils.findMergedAnnotation(method, ModelEventListener.class);
            if (modelEventListener == null) {
                throw new IllegalStateException("No @ModelEventListener annotation found on method: " + method);
            }
            this.transactionalEventListener = AnnotatedElementUtils.findMergedAnnotation(method, TransactionalEventListener.class);
            if (this.transactionalEventListener == null) {
                throw new IllegalStateException("No @TransactionalEventListener annotation found on method: " + method);
            }
            this.mergedCondition = ModelEventListenerConditionBuilder.build(
                    modelEventListener.models(),
                    modelEventListener.condition());
        }

        @Override
        protected String getCondition() {
            return this.mergedCondition;
        }

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (TransactionSynchronizationManager.isSynchronizationActive()
                    && TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronizationAdapter() {
                            @Override
                            public int getOrder() {
                                return ModelEventListenerMethodAdapter.this.getOrder();
                            }

                            @Override
                            public void beforeCommit(boolean readOnly) {
                                if (transactionalEventListener.phase() == TransactionPhase.BEFORE_COMMIT) {
                                    processEvent(event);
                                }
                            }

                            @Override
                            public void afterCompletion(int status) {
                                TransactionPhase phase = transactionalEventListener.phase();
                                if (phase == TransactionPhase.AFTER_COMMIT
                                        && status == TransactionSynchronization.STATUS_COMMITTED) {
                                    processEvent(event);
                                } else if (phase == TransactionPhase.AFTER_ROLLBACK
                                        && status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                                    processEvent(event);
                                } else if (phase == TransactionPhase.AFTER_COMPLETION) {
                                    processEvent(event);
                                }
                            }
                        });
            } else if (this.transactionalEventListener.fallbackExecution()) {
                if (this.transactionalEventListener.phase() == TransactionPhase.AFTER_ROLLBACK && logger.isWarnEnabled()) {
                    logger.warn("Processing " + event + " as a fallback execution on AFTER_ROLLBACK phase");
                }
                processEvent(event);
            } else if (logger.isDebugEnabled()) {
                logger.debug("No transaction is active - skipping " + event);
            }
        }
    }
}
