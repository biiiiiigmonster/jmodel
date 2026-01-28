package io.github.biiiiiigmonster.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchEvent {
    @AliasFor("events")
    Class<? extends ApplicationEvent>[] value() default {};
    @AliasFor("value")
    Class<? extends ApplicationEvent>[] events() default {};
    String condition() default "";
}
