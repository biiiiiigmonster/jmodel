package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.QueryCondition;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.MorphTo;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class HasOneOrMany<T extends Model<?>> extends Relation<T> {
    @Getter
    protected Field foreignField;
    @Getter
    protected Field localField;
    protected boolean chaperone;

    public HasOneOrMany(Field relatedField, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField);

        this.foreignField = foreignField;
        this.localField = localField;
        this.chaperone = chaperone;
    }

    @Override
    public <R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (CollectionUtils.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        return getResult(getForeignClass(), foreignConditionEnhancer(localKeyValueList));
    }

    protected <R extends Model<?>> Class<R> getForeignClass() {
        return (Class<R>) foreignField.getDeclaringClass();
    }

    protected <R extends Model<?>> Consumer<QueryCondition<R>> foreignConditionEnhancer(List<?> keys) {
        String columnName = RelationUtils.getColumn(foreignField);
        return condition -> condition.in(columnName, keys);
    }

    protected Field chaperoneField() {
        for (Field field : ReflectUtil.getFields(foreignField.getDeclaringClass())) {
            if (field.getType() == localField.getDeclaringClass()) {
                if (field.getAnnotation(BelongsTo.class) != null || field.getAnnotation(MorphTo.class) != null) {
                    return field;
                }
            }
        }

        return null;
    }

    public <R extends Model<?>> void associate(List<R> relatedModels) {
        relatedModels.forEach(this::associateRelatedModel);
    }

    protected <R extends Model<?>> void associateRelatedModel(R relatedModel) {
        Object localValue = ReflectUtil.getFieldValue(model, localField);
        ReflectUtil.setFieldValue(relatedModel, foreignField, localValue);
        relatedModel.save();
    }
}
