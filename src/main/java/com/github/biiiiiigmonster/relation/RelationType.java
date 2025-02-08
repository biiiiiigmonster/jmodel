package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.relation.annotations.BelongsTo;
import com.github.biiiiiigmonster.relation.annotations.BelongsToMany;
import com.github.biiiiiigmonster.relation.annotations.HasMany;
import com.github.biiiiiigmonster.relation.annotations.HasManyThrough;
import com.github.biiiiiigmonster.relation.annotations.HasOne;
import com.github.biiiiiigmonster.relation.annotations.HasOneThrough;
import com.github.biiiiiigmonster.relation.annotations.MorphMany;
import com.github.biiiiiigmonster.relation.annotations.MorphOne;
import com.github.biiiiiigmonster.relation.annotations.MorphTo;
import com.github.biiiiiigmonster.relation.annotations.MorphToMany;
import com.github.biiiiiigmonster.relation.annotations.MorphedByMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
public enum RelationType {
    HAS_ONE(HasOne.class, false) {
        @Override
        public com.github.biiiiiigmonster.relation.HasOne getRelation(Field field) {
            HasOne relation = field.getAnnotation(HasOne.class);
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(field.getDeclaringClass());
            return new com.github.biiiiiigmonster.relation.HasOne(
                    field,
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    ReflectUtil.getField(field.getType(), foreignKey)
            );
        }
    },
    HAS_MANY(HasMany.class, true) {
        @Override
        public com.github.biiiiiigmonster.relation.HasMany getRelation(Field field) {
            HasMany relation = field.getAnnotation(HasMany.class);
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            String foreignKey = StringUtils.isNotBlank(relation.localKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(RelationUtils.getGenericType(field));
            return new com.github.biiiiiigmonster.relation.HasMany(
                    field,
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey)
            );
        }
    },
    BELONGS_TO(BelongsTo.class, false) {
        @Override
        public com.github.biiiiiigmonster.relation.BelongsTo getRelation(Field field) {
            BelongsTo relation = field.getAnnotation(BelongsTo.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getPrimaryKey(field.getType());
            String ownerKey = StringUtils.isNotBlank(relation.ownerKey()) ? relation.ownerKey() : RelationUtils.getForeignKey(field.getDeclaringClass());
            return new com.github.biiiiiigmonster.relation.BelongsTo(
                    field,
                    ReflectUtil.getField(field.getType(), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), ownerKey)
            );
        }
    },
    BELONGS_TO_MANY(BelongsToMany.class, true) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    HAS_ONE_THROUGH(HasOneThrough.class, false) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    HAS_MANY_THROUGH(HasManyThrough.class, true) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    MORPH_ONE(MorphOne.class, false) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    MORPH_MANY(MorphMany.class, true) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    MORPH_TO(MorphTo.class, false) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    MORPH_TO_MANY(MorphToMany.class, true) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    MORPHED_BY_MANY(MorphedByMany.class, true) {
        @Override
        public Relation getRelation(Field field) {
            return null;
        }
    },
    ;

    private final Class<?> relationAnnotationClazz;
    private final boolean resultList;

    public static RelationType of(Field field) {
        Annotation relationAnnotation = getRelationAnnotation(field);
        if (relationAnnotation != null) {
            for (RelationType type : values()) {
                if (type.relationAnnotationClazz.equals(relationAnnotation.getClass())) {
                    return type;
                }
            }
        }

        return null;
    }

    public static Annotation getRelationAnnotation(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(com.github.biiiiiigmonster.relation.annotations.Relation.class)) {
                return annotation;
            }
        }

        return null;
    }

    public static boolean hasRelationAnnotation(Field field) {
        return getRelationAnnotation(field) != null;
    }

    public abstract Relation getRelation(Field field);
}
