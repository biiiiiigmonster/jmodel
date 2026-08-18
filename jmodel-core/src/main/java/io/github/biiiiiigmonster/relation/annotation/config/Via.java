package io.github.biiiiiigmonster.relation.annotation.config;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.constraint.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Via {
    Class<? extends Model<?>> via();
    String viaForeignKey() default "";
    String localKey() default "";
    Constraint[] constraints() default {};
}
