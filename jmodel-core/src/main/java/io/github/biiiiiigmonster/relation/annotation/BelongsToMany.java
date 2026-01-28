package io.github.biiiiiigmonster.relation.annotation;

import io.github.biiiiiigmonster.relation.Pivot;
import io.github.biiiiiigmonster.relation.annotation.config.Relation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface BelongsToMany {
    Class<? extends Pivot<?>> using();
    String foreignPivotKey() default "";
    String relatedPivotKey() default "";
    String localKey() default "";
    String foreignKey() default "";
    boolean withPivot() default false;
}
