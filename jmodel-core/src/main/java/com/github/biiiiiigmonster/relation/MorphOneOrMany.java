package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
import com.github.biiiiiigmonster.driver.QueryCondition;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class MorphOneOrMany extends HasOneOrMany {
    protected Field morphType;

    public MorphOneOrMany(Field relatedField, Field morphType, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField, foreignField, localField, chaperone);

        this.morphType = morphType;
    }

    protected <R extends Model<?>> List<R> byRelatedRepository(List<?> localKeyValueList) {
        Class<R> relatedClass = (Class<R>) foreignField.getDeclaringClass();
        DataDriver<R> driver = DriverRegistry.getDriver(relatedClass);
        String foreignColumn = RelationUtils.getColumn(foreignField);
        String morphTypeColumn = RelationUtils.getColumn(morphType);
        QueryCondition condition = QueryCondition.create()
                .in(foreignColumn, localKeyValueList)
                .eq(morphTypeColumn, getMorphAlias());
        return driver.findByCondition(relatedClass, condition);
    }

    protected String getMorphAlias() {
        return Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }

    protected <R extends Model<?>> void associateRelatedModel(R relatedModel) {
        ReflectUtil.setFieldValue(relatedModel, morphType, getMorphAlias());
        super.associateRelatedModel(relatedModel);
    }
}
