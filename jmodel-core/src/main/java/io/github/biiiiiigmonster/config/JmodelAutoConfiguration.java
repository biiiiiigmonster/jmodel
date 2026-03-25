package io.github.biiiiiigmonster.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "io.github.biiiiiigmonster")
@EnableConfigurationProperties(CoreProperties.class)
public class JmodelAutoConfiguration {
}
