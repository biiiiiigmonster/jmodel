package com.github.biiiiiigmonster.relation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

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
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), localKeyValueList)
                .eq(RelationUtils.getColumn(morphType), getMorphAlias());
        return relatedRepository.selectList(wrapper);
    }

    protected Object[] additionalRelatedMethodArgs() {
        return new Object[]{getMorphAlias()};
    }

    protected String getMorphAlias() {
        return Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }
}
