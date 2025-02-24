package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class MorphTo extends BelongsTo {
    protected Field morphType;

    /**
     * @param relatedField Image.imageable
     * @param morphType    Image.imageable_type
     * @param foreignField Image.imageable_id
     * @param ownerField   Post.id
     */
    public MorphTo(Field relatedField, Field morphType, Field foreignField, Field ownerField) {
        super(relatedField, foreignField, ownerField);

        this.morphType = morphType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        return super.getEager(filterMorph(models));
    }

    protected <T extends Model<?>> List<T> filterMorph(List<T> models) {
        return models.stream()
                .filter(model -> ReflectUtil.getFieldValue(model, morphType) == Relation.getMorphAlias(model.getClass()))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
        super.match(filterMorph(models), results);
    }
}
