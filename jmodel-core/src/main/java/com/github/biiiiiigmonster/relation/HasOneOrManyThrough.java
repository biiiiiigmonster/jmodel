package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
import com.github.biiiiiigmonster.driver.QueryCondition;

import java.lang.reflect.Field;
import java.util.List;

public abstract class HasOneOrManyThrough<TH extends Model<?>> extends Relation {
    protected Class<TH> throughClass;
    protected Field foreignField;
    protected Field throughForeignField;
    protected Field localField;
    protected Field throughLocalField;

    public HasOneOrManyThrough(Field relatedField, Class<TH> throughClass, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField);

        this.throughClass = throughClass;
        this.foreignField = foreignField;
        this.throughForeignField = throughForeignField;
        this.localField = localField;
        this.throughLocalField = throughLocalField;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        List<TH> throughs = getResult(localKeyValueList, foreignField, this::byThroughRelatedRepository);

        List<?> throughKeyValueList = relatedKeyValueList(throughs, throughLocalField);
        List<R> results = getResult(throughKeyValueList, throughForeignField);

        // 预匹配
        throughMatch(models, throughs, results);

        return results;
    }

    protected List<TH> byThroughRelatedRepository(List<?> keys) {
        DataDriver<TH> driver = DriverRegistry.getDriver(throughClass);
        String columnName = RelationUtils.getColumn(foreignField);
        QueryCondition condition = QueryCondition.byFieldValues(columnName, keys);
        return driver.findByCondition(throughClass, condition);
    }

    public abstract <T extends Model<?>, R extends Model<?>> void throughMatch(List<T> models, List<TH> throughs, List<R> results);

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
    }
}
