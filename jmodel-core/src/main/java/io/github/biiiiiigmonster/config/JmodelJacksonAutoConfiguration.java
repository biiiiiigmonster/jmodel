package io.github.biiiiiigmonster.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Autoconfiguration that registers {@link JmodelJacksonModule} when Jackson is on the classpath.
 * <p>
 * Spring Boot automatically picks up any {@link com.fasterxml.jackson.databind.Module} beans
 * and registers them with the application's {@link ObjectMapper}.
 */
@Configuration
@ConditionalOnClass(ObjectMapper.class)
public class JmodelJacksonAutoConfiguration {

    @Bean
    public JmodelJacksonModule jmodelJacksonModule() {
        return new JmodelJacksonModule();
    }
}
