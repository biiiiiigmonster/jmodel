package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BelongsTo extends Relation {
    protected Field foreignField;
    protected Field ownerField;

    public BelongsTo(Field relatedField, Field foreignField, Field ownerField) {
        super(relatedField);

        this.foreignField = foreignField;
        this.ownerField = ownerField;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> ownerKeyValueList = models.stream()
                .map(o -> ReflectUtil.getFieldValue(o, ownerField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(ownerKeyValueList)) {
            return new ArrayList<>();
        }

        return RelationUtils.hasRelatedRepository((Class<R>) foreignField.getDeclaringClass())
                ? byRelatedRepository(ownerKeyValueList)
                : byRelatedMethod(ownerKeyValueList, RelationUtils.getRelatedMethod(String.format("%s.%s", foreignField.getDeclaringClass().getName(), foreignField.getName()), foreignField));
    }

    private <R extends Model<?>> List<R> byRelatedRepository(List<?> ownerKeyValueList) {
        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query().in(RelationUtils.getColumn(foreignField), ownerKeyValueList);
        return relatedRepository.list(wrapper);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        if (ObjectUtil.isEmpty(results)) {
            return;
        }

        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));

        models.forEach(o -> {
            R value = dictionary.getOrDefault(ReflectUtil.getFieldValue(o, ownerField), null);
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
