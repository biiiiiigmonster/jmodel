package io.github.biiiiiigmonster.relation.annotation.config;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.constraint.Constraint;

public @interface Via {
    Class<? extends Model<?>> via();
    String viaForeignKey() default "";
    String localKey() default "";
    Constraint[] constraints() default {};
}
