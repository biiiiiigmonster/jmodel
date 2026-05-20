package io.github.biiiiiigmonster.config;

import io.github.biiiiiigmonster.event.listener.ModelEventListenerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.event.TransactionalEventListener;

@Configuration
@ConditionalOnClass(TransactionalEventListener.class)
public class ModelEventListenerAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ModelEventListenerFactory modelEventListenerFactory() {
        return new ModelEventListenerFactory();
    }
}
