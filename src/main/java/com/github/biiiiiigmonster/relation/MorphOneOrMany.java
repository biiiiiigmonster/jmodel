package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class MorphOneOrMany extends HasOneOrMany {
    protected Field morphType;

    public MorphOneOrMany(Field relatedField, Field morphType, Field foreignField, Field localField) {
        super(relatedField, foreignField, localField);

        this.morphType = morphType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        // 多态只支持从Repository中获取
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), localKeyValueList)
                .eq(RelationUtils.getColumn(morphType), getMorphAlias());
        return relatedRepository.selectList(wrapper);
    }

    protected String getMorphAlias() {
        return Relation.getMorphAlias(localField.getDeclaringClass(), foreignField.getDeclaringClass());
    }
}
