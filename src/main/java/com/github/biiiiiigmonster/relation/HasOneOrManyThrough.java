package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class HasOneOrManyThrough extends Relation {
    protected Field foreignField;
    protected Field throughForeignField;
    protected Field localField;
    protected Field throughLocalField;

    public HasOneOrManyThrough(Field relatedField, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField);

        this.foreignField = foreignField;
        this.throughForeignField = throughForeignField;
        this.localField = localField;
        this.throughLocalField = throughLocalField;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> List<Model<?>> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        IService<?> throughRepository = RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryChainWrapper<?> throughWrapper = throughRepository.query()
                .in(RelationUtils.getColumn(foreignField), localKeyValueList);
        List<Model<?>> throughs = (List<Model<?>>) throughWrapper.list();
        List<?> throughKeyValueList = relatedKeyValueList(throughs, throughLocalField);
        if (ObjectUtil.isEmpty(throughKeyValueList)) {
            return new ArrayList<>();
        }

        // 远程一对多只支持从Repository中获取
        IService<?> relatedRepository = RelationUtils.getRelatedRepository(throughForeignField.getDeclaringClass());
        QueryChainWrapper<?> wrapper = relatedRepository.query().in(RelationUtils.getColumn(throughForeignField), throughKeyValueList);
        List<Model<?>> results = (List<Model<?>>) wrapper.list();
        // 预匹配
        throughMatch(models, throughs, results);

        return results;
    }

    public abstract <T extends Model<?>> void throughMatch(List<T> models, List<Model<?>> throughs, List<Model<?>> results);
}
