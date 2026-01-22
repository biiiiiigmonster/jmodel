package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.QueryCondition;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public abstract class MorphOneOrMany extends HasOneOrMany {
    protected Field morphType;

    public MorphOneOrMany(Field relatedField, Field morphType, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField, foreignField, localField, chaperone);

        this.morphType = morphType;
    }

    protected <R extends Model<?>> Consumer<QueryCondition<R>> foreignConditionEnhancer(List<?> keys) {
        Consumer<QueryCondition<R>> superCond = super.foreignConditionEnhancer(keys);
        String morphTypeColumn = RelationUtils.getColumn(morphType);
        return superCond.andThen(cond -> cond.eq(morphTypeColumn, getMorphAlias()));
    }

    protected String getMorphAlias() {
        return Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }

    protected <R extends Model<?>> void associateRelatedModel(R relatedModel) {
        ReflectUtil.setFieldValue(relatedModel, morphType, getMorphAlias());
        super.associateRelatedModel(relatedModel);
    }
}
