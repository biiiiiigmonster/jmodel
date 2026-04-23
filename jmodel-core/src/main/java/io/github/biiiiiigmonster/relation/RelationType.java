package io.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.QueryCondition;
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
import io.github.biiiiiigmonster.relation.annotation.SiblingMany;
import io.github.biiiiiigmonster.relation.constraint.ConstraintApplier;
import io.github.biiiiiigmonster.relation.exception.RelationNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

@Getter
@AllArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public enum RelationType {
    HAS_ONE(HasOne.class, false) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.HasOne<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());
            HasOne relation = field.getAnnotation(HasOne.class);
            String foreignKey = StringUtils.isNotBlank(relation.foreignKey()) ? relation.foreignKey() : RelationUtils.getForeignKey(clazz);
            String localKey = StringUtils.isNotBlank(relation.localKey()) ? relation.localKey() : RelationUtils.getPrimaryKey(clazz);

            Field foreignField = ReflectUtil.getField(field.getType(), foreignKey);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(localField, foreignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.HasOne<>(
                    field,
                    ListUtil.toList(via),
                    foreignField,
                    localField,
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

            Field foreignField = ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(localField, foreignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.HasMany<>(
                    field,
                    ListUtil.toList(via),
                    foreignField,
                    localField,
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

            Field foreignField = ReflectUtil.getField(field.getDeclaringClass(), foreignKey);
            Field ownerField = ReflectUtil.getField(field.getType(), ownerKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(foreignField, ownerField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.BelongsTo<>(
                    field,
                    ListUtil.toList(via),
                    foreignField,
                    ownerField
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

            Field foreignPivotField = ReflectUtil.getField(relation.using(), foreignPivotKey);
            Field relatedPivotField = ReflectUtil.getField(relation.using(), relatedPivotKey);
            Field foreignField = ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            RelationVia<?> pivotVia = new RelationVia<>(localField, foreignPivotField, ConstraintApplier.toConsumers(relation.using(), relation.pivotConstraints()));
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(relatedPivotField, foreignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.BelongsToMany<>(
                    field,
                    ListUtil.toList(pivotVia, via),
                    relation.using(),
                    foreignPivotField,
                    relatedPivotField,
                    foreignField,
                    localField,
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

            Field foreignField = ReflectUtil.getField(relation.through(), foreignKey);
            Field throughForeignField = ReflectUtil.getField(field.getType(), throughForeignKey);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Field throughLocalField = ReflectUtil.getField(relation.through(), throughLocalKey);
            RelationVia<?> throughVia = new RelationVia<>(localField, foreignField, ConstraintApplier.toConsumers(relation.through(), relation.throughConstraints()));
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(throughLocalField, throughForeignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.HasOneThrough<>(
                    field,
                    ListUtil.toList(throughVia, via),
                    relation.through(),
                    foreignField,
                    throughForeignField,
                    localField,
                    throughLocalField
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

            Field foreignField = ReflectUtil.getField(relation.through(), foreignKey);
            Field throughForeignField = ReflectUtil.getField(RelationUtils.getGenericType(field), throughForeignKey);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Field throughLocalField = ReflectUtil.getField(relation.through(), throughLocalKey);
            RelationVia<?> throughVia = new RelationVia<>(localField, foreignField, ConstraintApplier.toConsumers(relation.through(), relation.throughConstraints()));
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(throughLocalField, throughForeignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.HasManyThrough<>(
                    field,
                    ListUtil.toList(throughVia, via),
                    relation.through(),
                    foreignField,
                    throughForeignField,
                    localField,
                    throughLocalField
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

            Field typeField = ReflectUtil.getField(field.getType(), type);
            Field idField = ReflectUtil.getField(field.getType(), id);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            List<Consumer<QueryCondition>> consumerList = ConstraintApplier.toConsumers(entityClass, relation.constraints());
            consumerList.add(cond -> cond.eq(type, Relation.getMorphAlias(clazz, entityClass)));
            RelationVia<?> via = new RelationVia<>(localField, idField, consumerList);
            return new io.github.biiiiiigmonster.relation.MorphOne<>(
                    field,
                    ListUtil.toList(via),
                    typeField,
                    idField,
                    localField,
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

            Field typeField = ReflectUtil.getField(RelationUtils.getGenericType(field), type);
            Field idField = ReflectUtil.getField(RelationUtils.getGenericType(field), id);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            List<Consumer<QueryCondition>> consumerList = ConstraintApplier.toConsumers(entityClass, relation.constraints());
            consumerList.add(cond -> cond.eq(type, Relation.getMorphAlias(clazz, entityClass)));
            RelationVia<?> via = new RelationVia<>(localField, idField, consumerList);
            return new io.github.biiiiiigmonster.relation.MorphMany<>(
                    field,
                    ListUtil.toList(via),
                    typeField,
                    idField,
                    localField,
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

            Field typeField = ReflectUtil.getField(field.getDeclaringClass(), type);
            Field idField = ReflectUtil.getField(field.getDeclaringClass(), id);
            Field ownerField = ReflectUtil.getField(field.getType(), ownerKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(idField, ownerField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.MorphTo<>(
                    field,
                    ListUtil.toList(via),
                    typeField,
                    idField,
                    ownerField
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

            Field pivotTypeField = ReflectUtil.getField(relation.using(), pivotType);
            Field pivotIdField = ReflectUtil.getField(relation.using(), pivotId);
            Field relatedPivotField = ReflectUtil.getField(relation.using(), relatedPivotKey);
            Field foreignField = ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey);
            Field localField = ReflectUtil.getField(field.getDeclaringClass(), localKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            List<Consumer<QueryCondition>> consumerList = ConstraintApplier.toConsumers(relation.using(), relation.pivotConstraints());
            consumerList.add(cond -> cond.eq(pivotType, Relation.getMorphAlias(clazz, entityClass)));
            RelationVia<?> pivotVia = new RelationVia<>(localField, pivotIdField, consumerList);
            RelationVia<?> via = new RelationVia<>(relatedPivotField, foreignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.MorphToMany<>(
                    field,
                    ListUtil.toList(pivotVia, via),
                    relation.using(),
                    pivotTypeField,
                    pivotIdField,
                    relatedPivotField,
                    foreignField,
                    localField,
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

            Field pivotTypeField = ReflectUtil.getField(relation.using(), pivotType);
            Field foreignPivotField = ReflectUtil.getField(relation.using(), foreignPivotKey);
            Field pivotIdField = ReflectUtil.getField(relation.using(), pivotId);
            Field foreignField = ReflectUtil.getField(RelationUtils.getGenericType(field), foreignKey);
            Field ownerField = ReflectUtil.getField(field.getDeclaringClass(), ownerKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            List<Consumer<QueryCondition>> consumerList = ConstraintApplier.toConsumers(relation.using(), relation.pivotConstraints());
            consumerList.add(cond -> cond.eq(pivotType, Relation.getMorphAlias(entityClass, clazz)));
            RelationVia<?> pivotVia = new RelationVia<>(ownerField, foreignPivotField, consumerList);
            RelationVia<?> via = new RelationVia<>(pivotIdField, foreignField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.MorphToMany<>(
                    field,
                    ListUtil.toList(pivotVia, via),
                    relation.using(),
                    pivotTypeField,
                    foreignPivotField,
                    pivotIdField,
                    foreignField,
                    ownerField,
                    true,
                    relation.withPivot()
            );
        }
    },
    SIBLING_MANY(SiblingMany.class, true) {
        @Override
        public <T extends Model<?>> io.github.biiiiiigmonster.relation.SiblingMany<T> getRelation(RelationOption<T> relationOption) {
            Class<T> clazz = relationOption.getClazz();
            Field field = ReflectUtil.getField(clazz, relationOption.getFieldName());

            SiblingMany relation = field.getAnnotation(SiblingMany.class);
            String parentKey = relation.parent().foreignKey();
            if (StringUtils.isNotBlank(relation.from())) {
                Field belongsField = ReflectUtil.getField(clazz, relation.from());
                BelongsTo belongs = belongsField.getAnnotation(BelongsTo.class);
                parentKey = StringUtils.isNotBlank(belongs.foreignKey())
                        ? belongs.foreignKey() : RelationUtils.getForeignKey((Class<? extends Model<?>>) belongsField.getType());
            }

            Field parentField = ReflectUtil.getField(field.getDeclaringClass(), parentKey);
            Class<? extends Model<?>> entityClass = RelationUtils.getGenericType(field);
            RelationVia<?> via = new RelationVia<>(parentField, parentField, ConstraintApplier.toConsumers(entityClass, relation.constraints()));
            return new io.github.biiiiiigmonster.relation.SiblingMany<>(
                    field,
                    ListUtil.toList(via),
                    parentField
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
