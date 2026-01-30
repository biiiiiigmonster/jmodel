package io.github.biiiiiigmonster.relation.annotation;

import io.github.biiiiiigmonster.relation.annotation.config.Relation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface BelongsTo {
    String foreignKey() default "";
    String ownerKey() default "";
}
