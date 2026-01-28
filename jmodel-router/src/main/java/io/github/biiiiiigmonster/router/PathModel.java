package io.github.biiiiiigmonster.router;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface PathModel {
    @AliasFor("name")
    String value() default "";
    @AliasFor("value")
    String name() default "";
    String routeKey() default "";
    boolean required() default true;
    boolean scopeBinding() default false;
}
