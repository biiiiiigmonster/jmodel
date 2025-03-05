package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        BaseMapper<TH> throughRepository = (BaseMapper<TH>) RelationUtils.getRelatedRepository(this.throughClass);
        QueryWrapper<TH> throughWrapper = new QueryWrapper<>();
        throughWrapper.in(RelationUtils.getColumn(foreignField), localKeyValueList);
        List<TH> throughs = throughRepository.selectList(throughWrapper);
        List<?> throughKeyValueList = relatedKeyValueList(throughs, throughLocalField);
        if (ObjectUtil.isEmpty(throughKeyValueList)) {
            return new ArrayList<>();
        }

        // 远程一对多只支持从Repository中获取
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(throughForeignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(throughForeignField), throughKeyValueList);
        List<R> results = relatedRepository.selectList(wrapper);
        // 预匹配
        throughMatch(models, throughs, results);

        return results;
    }

    public abstract <T extends Model<?>, R extends Model<?>> void throughMatch(List<T> models, List<TH> throughs, List<R> results);

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
    }
}
