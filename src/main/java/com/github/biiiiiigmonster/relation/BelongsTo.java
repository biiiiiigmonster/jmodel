package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BelongsTo extends Relation {
    protected Field foreignField;
    protected Field ownerField;

    /**
     * @param relatedField Phone.user
     * @param foreignField Phone.user_id
     * @param ownerField   User.id
     */
    public BelongsTo(Field relatedField, Field foreignField, Field ownerField) {
        super(relatedField);

        this.foreignField = foreignField;
        this.ownerField = ownerField;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> ownerKeyValueList = relatedKeyValueList(models, foreignField);
        return getResult(ownerKeyValueList, ownerField, this::byRelatedRepository);
    }

    private <R extends Model<?>> List<R> byRelatedRepository(List<?> keys) {
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(ownerField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(ownerField), keys);
        return relatedRepository.selectList(wrapper);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, ownerField), r -> r));

        models.forEach(o -> {
            R value = dictionary.get(ReflectUtil.getFieldValue(o, foreignField));
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
