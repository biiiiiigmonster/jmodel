package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.DataDriver;
import io.github.biiiiiigmonster.driver.DriverRegistry;
import io.github.biiiiiigmonster.driver.QueryCondition;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public class MorphToMany<T extends Model<?>, MP extends MorphPivot<?>> extends BelongsToMany<T, MP> {
    protected Field morphPivotType;
    protected boolean inverse;

    /**
     * @param relatedField      Post.tags                   Tag.posts
     * @param morphPivotClass   Morph pivot class
     * @param morphPivotType    Taggables.taggable_type     Taggables.taggable_type
     * @param foreignPivotField Taggables.taggable_id       Taggables.tag_id
     * @param relatedPivotField Taggables.tag_id            Taggables.taggable_id
     * @param foreignField      Tag.id                      Post.id
     * @param localField        Post.id                     Tag.id
     * @param inverse           inverse
     * @param withPivot         with pivot
     */
    public MorphToMany(Field relatedField, Class<MP> morphPivotClass, Field morphPivotType, Field foreignPivotField, Field relatedPivotField, Field foreignField, Field localField, boolean inverse, boolean withPivot) {
        super(relatedField, morphPivotClass, foreignPivotField, relatedPivotField, foreignField, localField, withPivot);

        this.morphPivotType = morphPivotType;
        this.inverse = inverse;
    }

    protected Consumer<QueryCondition<MP>> pivotConditionEnhancer(List<?> keys) {
        Consumer<QueryCondition<MP>> superCond = super.pivotConditionEnhancer(keys);
        String morphTypeColumn = RelationUtils.getColumn(morphPivotType);
        return superCond.andThen(cond -> cond.eq(morphTypeColumn, getMorphAlias()));
    }

    protected String getMorphAlias() {
        return inverse
                ? Relation.getMorphAlias(foreignField.getDeclaringClass(), localField.getDeclaringClass())
                : Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }

    protected void pivotSave(MP pivot, Object localValue, Object foreignValue) {
        ReflectUtil.setFieldValue(pivot, morphPivotType, getMorphAlias());
        super.pivotSave(pivot, localValue, foreignValue);
    }

    @Override
    protected void pivotDeleteByCondition(Object localValue, List<Object> foreignValues) {
        DataDriver driver = DriverRegistry.getDriver(pivotClass);
        String foreignPivotColumn = RelationUtils.getColumn(foreignPivotField);
        String morphTypeColumn = RelationUtils.getColumn(morphPivotType);

        QueryCondition<MP> condition = QueryCondition.create(pivotClass)
                .eq(foreignPivotColumn, localValue)
                .eq(morphTypeColumn, getMorphAlias());

        if (foreignValues != null && !foreignValues.isEmpty()) {
            String relatedPivotColumn = RelationUtils.getColumn(relatedPivotField);
            condition.in(relatedPivotColumn, foreignValues);
        }

        // 查询要删除的记录
        List<MP> toDelete = driver.findByCondition(condition);
        // 逐个删除
        for (MP pivot : toDelete) {
            pivot.delete();
        }
    }
}
