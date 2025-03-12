package com.github.biiiiiigmonster.relation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

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
        List<R> results = getResult(throughKeyValueList, throughForeignField, this::byRelatedRepository);

        // 预匹配
        throughMatch(models, throughs, results);

        return results;
    }

    @SuppressWarnings("unchecked")
    protected List<TH> byThroughRelatedRepository(List<?> keys) {
        BaseMapper<TH> throughRepository = (BaseMapper<TH>) RelationUtils.getRelatedRepository(throughClass);
        QueryWrapper<TH> throughWrapper = new QueryWrapper<>();
        throughWrapper.in(RelationUtils.getColumn(foreignField), keys);
        return throughRepository.selectList(throughWrapper);
    }

    @SuppressWarnings("unchecked")
    protected <R extends Model<?>> List<R> byRelatedRepository(List<?> keys) {
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(throughForeignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(throughForeignField), keys);
        return relatedRepository.selectList(wrapper);
    }

    public abstract <T extends Model<?>, R extends Model<?>> void throughMatch(List<T> models, List<TH> throughs, List<R> results);

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
    }
}
