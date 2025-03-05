package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        return RelationUtils.hasRelatedRepository(foreignField.getDeclaringClass())
                ? byRelatedRepository(localKeyValueList)
                : byRelatedMethod(localKeyValueList, RelationUtils.getRelatedMethod(String.format("%s.%s", foreignField.getDeclaringClass().getName(), foreignField.getName()), foreignField));
    }

    @SuppressWarnings("unchecked")
    private <R extends Model<?>> List<R> byRelatedRepository(List<?> localKeyValueList) {
        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.list(wrapper);
    }
}
