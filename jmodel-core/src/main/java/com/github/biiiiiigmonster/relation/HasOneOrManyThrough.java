package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.DriverRegistry;
import com.github.biiiiiigmonster.driver.QueryCondition;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class HasOneOrManyThrough<T extends Model<?>, TH extends Model<?>> extends Relation<T> {
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
    public <R extends Model<?>> List<R> getEager(List<T> models) {
        List<TH> throughs = getThroughResult(models);
        List<R> results = getThroughForeignResult(throughs);

        // 预匹配
        throughMatch(models, throughs, results);

        return results;
    }

    protected List<TH> getThroughResult(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (CollectionUtils.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        String columnName = RelationUtils.getColumn(foreignField);
        return getResult(throughClass, cond -> cond.in(columnName, localKeyValueList));
    }

    protected <R extends Model<?>> List<R> getThroughForeignResult(List<TH> throughs) {
        List<?> throughKeyValueList = relatedKeyValueList(throughs, throughLocalField);
        return getResult(throughKeyValueList, throughForeignField);
    }

    public abstract <R extends Model<?>> void throughMatch(List<T> models, List<TH> throughs, List<R> results);

    @Override
    public <R extends Model<?>> void match(List<T> models, List<R> results) {
    }
}
