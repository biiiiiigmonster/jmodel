package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
import com.github.biiiiiigmonster.driver.QueryCondition;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class HasOneOrMany extends Relation {
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
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        return getResult(localKeyValueList, foreignField);
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
