package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<?> localKeyValueList = models.stream()
                .map(o -> ReflectUtil.getFieldValue(o, localField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        return RelationUtils.hasRelatedRepository((Class<R>) foreignField.getDeclaringClass())
                ? byRelatedRepository(localKeyValueList)
                : byRelatedMethod(localKeyValueList, RelationUtils.getRelatedMethod(String.format("%s.%s", foreignField.getDeclaringClass().getName(), foreignField.getName()), foreignField));
    }

    private <R extends Model<?>> List<R> byRelatedRepository(List<?> localKeyValueList) {
        IService<R> relatedRepository = RelationUtils.getRelatedRepository((Class<R>) foreignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query().in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.list(wrapper);
    }
}
