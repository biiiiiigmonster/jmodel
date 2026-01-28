package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BelongsTo<T extends Model<?>> extends Relation<T> {
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
    public <R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> ownerKeyValueList = relatedKeyValueList(models, foreignField);
        return getResult(ownerKeyValueList, ownerField);
    }

    @Override
    public <R extends Model<?>> void match(List<T> models, List<R> results) {
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, ownerField), r -> r));

        models.forEach(o -> {
            R value = dictionary.get(ReflectUtil.getFieldValue(o, foreignField));
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }

    public <R extends Model<?>> void associate(R relatedModel) {
        Object localValue = ReflectUtil.getFieldValue(relatedModel, ownerField);
        ReflectUtil.setFieldValue(model, foreignField, localValue);
        model.save();
    }
}
