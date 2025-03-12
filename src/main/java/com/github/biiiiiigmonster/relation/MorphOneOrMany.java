package com.github.biiiiiigmonster.relation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;

public abstract class MorphOneOrMany extends HasOneOrMany {
    protected Field morphType;

    public MorphOneOrMany(Field relatedField, Field morphType, Field foreignField, Field localField) {
        super(relatedField, foreignField, localField);

        this.morphType = morphType;
    }

    protected <R extends Model<?>> List<R> byRelatedRepository(List<?> localKeyValueList) {
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.eq(RelationUtils.getColumn(morphType), getMorphAlias())
                .in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.selectList(wrapper);
    }

    protected String getMorphAlias() {
        return Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }
}
