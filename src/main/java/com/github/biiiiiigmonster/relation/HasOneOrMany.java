package com.github.biiiiiigmonster.relation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;

public abstract class HasOneOrMany extends Relation {
    protected Field foreignField;
    protected Field localField;

    public HasOneOrMany(Field relatedField, Field foreignField, Field localField) {
        super(relatedField);

        this.foreignField = foreignField;
        this.localField = localField;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        return getResult(localKeyValueList, foreignField, this::byRelatedRepository);
    }

    @SuppressWarnings("unchecked")
    protected <R extends Model<?>> List<R> byRelatedRepository(List<?> localKeyValueList) {
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.selectList(wrapper);
    }
}
