package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import com.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import com.github.biiiiiigmonster.relation.annotation.HasMany;
import com.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import com.github.biiiiiigmonster.relation.annotation.HasOne;
import com.github.biiiiiigmonster.relation.annotation.HasOneThrough;
import com.github.biiiiiigmonster.relation.annotation.MorphMany;
import com.github.biiiiiigmonster.relation.annotation.MorphOne;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;
import com.github.biiiiiigmonster.relation.annotation.MorphToMany;
import com.github.biiiiiigmonster.relation.annotation.MorphedByMany;
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
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(field.getType());
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            return new com.github.biiiiiigmonster.relation.HasOne(
                    field,
                    ReflectUtil.getField(field.getType(), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey)
            );
        }
    },
    HAS_MANY(HasMany.class, true) {
        @Override
        public com.github.biiiiiigmonster.relation.HasMany getRelation(Field field) {
            HasMany relation = field.getAnnotation(HasMany.class);
            String foreignKey = StringUtils.isNotBlank(relation.localKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(RelationUtils.getGenericType(field));
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            return new com.github.biiiiiigmonster.relation.HasMany(
                    field,
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey)
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
        public com.github.biiiiiigmonster.relation.BelongsToMany getRelation(Field field) {
            BelongsToMany relation = field.getAnnotation(BelongsToMany.class);
            String foreignPivotKey = StringUtils.isNotBlank(relation.foreignPivotKey()) ? relation.foreignPivotKey() : RelationUtils.getForeignKey(field.getDeclaringClass());
            String relatedPivotKey = StringUtils.isNotBlank(relation.relatedPivotKey()) ? relation.relatedPivotKey() : RelationUtils.getForeignKey(RelationUtils.getGenericType(field));
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getPrimaryKey(RelationUtils.getGenericType(field));
            return new com.github.biiiiiigmonster.relation.BelongsToMany<>(
                    field,
                    relation.using(),
                    ReflectUtil.getField(relation.using(), foreignPivotKey),
                    ReflectUtil.getField(relation.using(), relatedPivotKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey)
            );
        }
    },
    HAS_ONE_THROUGH(HasOneThrough.class, false) {
        @Override
        public com.github.biiiiiigmonster.relation.HasOneThrough getRelation(Field field) {
            HasOneThrough relation = field.getAnnotation(HasOneThrough.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(field.getDeclaringClass());
            String throughForeignKey = StringUtils.isNotBlank(relation.throughForeignKey()) ? relation.throughForeignKey() : RelationUtils.getForeignKey(relation.through());
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            String throughLocalKey = StringUtils.isNotBlank(relation.throughLocalKey()) ? relation.throughLocalKey() : RelationUtils.getPrimaryKey(relation.through());
            return new com.github.biiiiiigmonster.relation.HasOneThrough<>(
                    field,
                    relation.through(),
                    ReflectUtil.getField(relation.through(), foreignKey),
                    ReflectUtil.getField(field.getType(), throughForeignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    ReflectUtil.getField(relation.through(), throughLocalKey)
            );
        }
    },
    HAS_MANY_THROUGH(HasManyThrough.class, true) {
        @Override
        public com.github.biiiiiigmonster.relation.HasManyThrough getRelation(Field field) {
            HasManyThrough relation = field.getAnnotation(HasManyThrough.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(field.getDeclaringClass());
            String throughForeignKey = StringUtils.isNotBlank(relation.throughForeignKey()) ? relation.throughForeignKey() : RelationUtils.getForeignKey(relation.through());
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            String throughLocalKey = StringUtils.isNotBlank(relation.throughLocalKey()) ? relation.throughLocalKey() : RelationUtils.getPrimaryKey(relation.through());
            return new com.github.biiiiiigmonster.relation.HasManyThrough<>(
                    field,
                    relation.through(),
                    ReflectUtil.getField(relation.through(), foreignKey),
                    ReflectUtil.getField(field.getType(), throughForeignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    ReflectUtil.getField(relation.through(), throughLocalKey)
            );
        }
    },
    MORPH_ONE(MorphOne.class, false) {
        @Override
        public com.github.biiiiiigmonster.relation.MorphOne getRelation(Field field) {
            MorphOne relation = field.getAnnotation(MorphOne.class);
            String type = StringUtils.isNotBlank(relation.type()) ? relation.type() : String.format("%sType", relation.name());
            String id = StringUtils.isNotBlank(relation.id()) ? relation.id() : String.format("%sId", relation.name());
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            return new com.github.biiiiiigmonster.relation.MorphOne(
                    field,
                    ReflectUtil.getField(field.getType(), type),
                    ReflectUtil.getField(field.getType(), id),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey)
            );
        }
    },
    MORPH_MANY(MorphMany.class, true) {
        @Override
        public com.github.biiiiiigmonster.relation.MorphMany getRelation(Field field) {
            MorphMany relation = field.getAnnotation(MorphMany.class);
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(field.getDeclaringClass());
            String type = StringUtils.isNotBlank(relation.type()) ? relation.type() : String.format("%sType", relation.name());
            String id = StringUtils.isNotBlank(relation.id()) ? relation.id() : String.format("%sId", relation.name());
            return new com.github.biiiiiigmonster.relation.MorphMany(
                    field,
                    ReflectUtil.getField(field.getType(), type),
                    ReflectUtil.getField(field.getType(), id),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey)
            );
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
            if (annotation.annotationType().isAnnotationPresent(com.github.biiiiiigmonster.relation.annotation.Relation.class)) {
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
