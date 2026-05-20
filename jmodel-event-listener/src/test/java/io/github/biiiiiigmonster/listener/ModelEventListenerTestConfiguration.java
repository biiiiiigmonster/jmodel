package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.config.ModelEventListenerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = TestModelEventListener.class)
@Import(ModelEventListenerAutoConfiguration.class)
public class ModelEventListenerTestConfiguration {
}
