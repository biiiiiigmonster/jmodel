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

public abstract class MorphOneOrMany extends HasOneOrMany {
    protected Field morphType;

    public MorphOneOrMany(Field relatedField, Field morphType, Field foreignField, Field localField) {
        super(relatedField, foreignField, localField);

        this.morphType = morphType;
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

        // 多态只支持从Repository中获取
        IService<R> relatedRepository = RelationUtils.getRelatedRepository((Class<R>) foreignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query()
                .eq(RelationUtils.getColumn(morphType), Relation.getMorphAlias(localField.getDeclaringClass()))
                .in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.list(wrapper);
    }
}
