package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import io.github.biiiiiigmonster.relation.annotation.HasOne;
import io.github.biiiiiigmonster.relation.annotation.HasOneThrough;
import io.github.biiiiiigmonster.relation.annotation.MorphMany;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import io.github.biiiiiigmonster.relation.annotation.MorphTo;
import io.github.biiiiiigmonster.relation.annotation.MorphToMany;
import io.github.biiiiiigmonster.relation.annotation.MorphedByMany;
import io.github.biiiiiigmonster.relation.exception.RelationNotFoundException;
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
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.HasOne<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            HasOne relation = field.getAnnotation(HasOne.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(clazz);
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            return new io.github.biiiiiigmonster.relation.HasOne<>(
                    field,
                    ReflectUtil.getField(field.getType(), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    relation.chaperone()
            );
        }
    },
    HAS_MANY(HasMany.class, true) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.HasMany<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            HasMany relation = field.getAnnotation(HasMany.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(clazz);
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            return new io.github.biiiiiigmonster.relation.HasMany<>(
                    field,
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    relation.chaperone()
            );
        }
    },
    BELONGS_TO(BelongsTo.class, false) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.BelongsTo<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            BelongsTo relation = field.getAnnotation(BelongsTo.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey())
                    ? relation.foreignKey() : RelationUtils.getForeignKey((Class<? extends Model<?>>) field.getType());
            String ownerKey = StringUtils.isNotBlank(relation.ownerKey())
                    ? relation.ownerKey() : RelationUtils.getPrimaryKey((Class<? extends Model<?>>) field.getType());
            return new io.github.biiiiiigmonster.relation.BelongsTo<>(
                    field,
                    ReflectUtil.getField(field.getDeclaringClass(), foreignKey),
                    ReflectUtil.getField(field.getType(), ownerKey)
            );
        }
    },
    BELONGS_TO_MANY(BelongsToMany.class, true) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.BelongsToMany<T, ?> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            BelongsToMany relation = field.getAnnotation(BelongsToMany.class);
            String foreignPivotKey = StringUtils.isNotBlank(relation.foreignPivotKey())
                    ? relation.foreignPivotKey() : RelationUtils.getForeignKey(clazz);
            String relatedPivotKey = StringUtils.isNotBlank(relation.relatedPivotKey())
                    ? relation.relatedPivotKey() : RelationUtils.getForeignKey(RelationUtils.getGenericType(field));
            String localKey = StringUtils.isNotBlank(relation.localKey())
                    ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey())
                    ? relation.foreignKey() : RelationUtils.getPrimaryKey(RelationUtils.getGenericType(field));
            return new io.github.biiiiiigmonster.relation.BelongsToMany<>(
                    field,
                    relation.using(),
                    ReflectUtil.getField(relation.using(), foreignPivotKey),
                    ReflectUtil.getField(relation.using(), relatedPivotKey),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    relation.withPivot()
            );
        }
    },
    HAS_ONE_THROUGH(HasOneThrough.class, false) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.HasOneThrough<T, ?> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            HasOneThrough relation = field.getAnnotation(HasOneThrough.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey())
                    ? relation.foreignKey() : RelationUtils.getForeignKey(clazz);
            String throughForeignKey = StringUtils.isNotBlank(relation.throughForeignKey())
                    ? relation.throughForeignKey() : RelationUtils.getForeignKey(relation.through());
            String localKey = StringUtils.isNotBlank(relation.localKey())
                    ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            String throughLocalKey = StringUtils.isNotBlank(relation.throughLocalKey())
                    ? relation.throughLocalKey() : RelationUtils.getPrimaryKey(relation.through());
            return new io.github.biiiiiigmonster.relation.HasOneThrough<>(
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
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.HasManyThrough<T, ?> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            HasManyThrough relation = field.getAnnotation(HasManyThrough.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey())
                    ? relation.foreignKey() : RelationUtils.getForeignKey(clazz);
            String throughForeignKey = StringUtils.isNotBlank(relation.throughForeignKey())
                    ? relation.throughForeignKey() : RelationUtils.getForeignKey(relation.through());
            String localKey = StringUtils.isNotBlank(relation.localKey())
                    ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            String throughLocalKey = StringUtils.isNotBlank(relation.throughLocalKey())
                    ? relation.throughLocalKey() : RelationUtils.getPrimaryKey(relation.through());
            return new io.github.biiiiiigmonster.relation.HasManyThrough<>(
                    field,
                    relation.through(),
                    ReflectUtil.getField(relation.through(), foreignKey),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), throughForeignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    ReflectUtil.getField(relation.through(), throughLocalKey)
            );
        }
    },
    MORPH_ONE(MorphOne.class, false) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.MorphOne<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            MorphOne relation = field.getAnnotation(MorphOne.class);
            Morph morph = Relation.getMorph(field.getType());
            String type = StringUtils.isNotBlank(relation.type()) ? relation.type() : morph.getType();
            String id = StringUtils.isNotBlank(relation.id()) ? relation.id() : morph.getId();
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            return new io.github.biiiiiigmonster.relation.MorphOne<>(
                    field,
                    ReflectUtil.getField(field.getType(), type),
                    ReflectUtil.getField(field.getType(), id),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    relation.chaperone()
            );
        }
    },
    MORPH_MANY(MorphMany.class, true) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.MorphMany<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            MorphMany relation = field.getAnnotation(MorphMany.class);
            Morph morph = Relation.getMorph(RelationUtils.getGenericType(field));
            String type = StringUtils.isNotBlank(relation.type()) ? relation.type() : morph.getType();
            String id = StringUtils.isNotBlank(relation.id()) ? relation.id() : morph.getId();
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            return new io.github.biiiiiigmonster.relation.MorphMany<>(
                    field,
                    ReflectUtil.getField(RelationUtils.getGenericType(field), type),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), id),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    relation.chaperone()
            );
        }
    },
    MORPH_TO(MorphTo.class, false) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.MorphTo<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            MorphTo relation = field.getAnnotation(MorphTo.class);
            Morph morph = Relation.getMorph(field.getDeclaringClass());
            String type = StringUtils.isNotBlank(relation.type()) ? relation.type() : morph.getType();
            String id = StringUtils.isNotBlank(relation.id()) ? relation.id() : morph.getId();
            String ownerKey = StringUtils.isNotBlank(relation.ownerKey())
                    ? relation.ownerKey() : RelationUtils.getPrimaryKey((Class<? extends Model<?>>) field.getType());
            return new io.github.biiiiiigmonster.relation.MorphTo<>(
                    field,
                    ReflectUtil.getField(field.getDeclaringClass(), type),
                    ReflectUtil.getField(field.getDeclaringClass(), id),
                    ReflectUtil.getField(field.getType(), ownerKey)
            );
        }
    },
    MORPH_TO_MANY(MorphToMany.class, true) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.MorphToMany<T, ?> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            MorphToMany relation = field.getAnnotation(MorphToMany.class);
            Morph morph = Relation.getMorph(relation.using());
            String pivotType = StringUtils.isNotBlank(relation.pivotType()) ? relation.pivotType() : morph.getType();
            String pivotId = StringUtils.isNotBlank(relation.pivotId()) ? relation.pivotId() : morph.getId();
            String relatedPivotKey = StringUtils.isNotBlank(relation.relatedPivotKey())
                    ? relation.relatedPivotKey() : RelationUtils.getForeignKey(RelationUtils.getGenericType(field));
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey())
                    ? relation.foreignKey() : RelationUtils.getPrimaryKey(RelationUtils.getGenericType(field));
            String localKey = StringUtils.isNotBlank(relation.localKey())
                    ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);
            return new io.github.biiiiiigmonster.relation.MorphToMany<>(
                    field,
                    relation.using(),
                    ReflectUtil.getField(relation.using(), pivotType),
                    ReflectUtil.getField(relation.using(), pivotId),
                    ReflectUtil.getField(relation.using(), relatedPivotKey),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), localKey),
                    false,
                    relation.withPivot()
            );
        }
    },
    MORPHED_BY_MANY(MorphedByMany.class, true) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.MorphToMany<T, ?> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            MorphedByMany relation = field.getAnnotation(MorphedByMany.class);
            Morph morph = Relation.getMorph(relation.using());
            String pivotType = StringUtils.isNotBlank(relation.pivotType()) ? relation.pivotType() : morph.getType();
            String pivotId = StringUtils.isNotBlank(relation.pivotId()) ? relation.pivotId() : morph.getId();
            String foreignPivotKey = StringUtils.isNotBlank(relation.foreignPivotKey())
                    ? relation.foreignPivotKey() : RelationUtils.getForeignKey(RelationUtils.getGenericType(field));
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey())
                    ? relation.foreignKey() : RelationUtils.getPrimaryKey(RelationUtils.getGenericType(field));
            String ownerKey = StringUtils.isNotBlank(relation.ownerKey())
                    ? relation.ownerKey() : RelationUtils.getPrimaryKey(clazz);
            return new io.github.biiiiiigmonster.relation.MorphToMany<>(
                    field,
                    relation.using(),
                    ReflectUtil.getField(relation.using(), pivotType),
                    ReflectUtil.getField(relation.using(), foreignPivotKey),
                    ReflectUtil.getField(relation.using(), pivotId),
                    ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey),
                    ReflectUtil.getField(field.getDeclaringClass(), ownerKey),
                    true,
                    relation.withPivot()
            );
        }
    },
    ;

    private final Class<?> relationAnnotationClazz;
    private final boolean resultList;

    public static RelationType of(Field field) {
        Annotation relationAnnotation = getRelationAnnotation(field);
        if (relationAnnotation != null) {
            for (RelationType type : values()) {
                if (type.relationAnnotationClazz.equals(relationAnnotation.annotationType())) {
                    return type;
                }
            }
        }

        throw new RelationNotFoundException(RelationUtils.getGenericType(field), field.getName());
    }

    public static Annotation getRelationAnnotation(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(io.github.biiiiiigmonster.relation.annotation.config.Relation.class)) {
                return annotation;
            }
        }

        return null;
    }

    public static boolean hasRelationAnnotation(Field field) {
        return getRelationAnnotation(field) != null;
    }

    public abstract <T extends Model<?>> Relation<T> getRelation(RelationOption<T> relationOption);
}
