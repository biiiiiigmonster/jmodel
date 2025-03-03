package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasOneThrough<TH extends Model<?>> extends HasOneOrManyThrough<TH> {

    /**
     * @param relatedField        Mechanic.carOwner
     * @param foreignField        Car.mechanic_id
     * @param throughForeignField Owner.car_id
     * @param localField          Mechanic.id
     * @param throughLocalField   Car.id
     */
    public HasOneThrough(Field relatedField, Class<TH> throughClass, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField, throughClass, foreignField, throughForeignField, localField, throughLocalField);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void throughMatch(List<T> models, List<TH> throughs, List<R> results) {
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, throughForeignField), r -> r, (o1, o2) -> o1));
        Map<?, TH> throughDictionary = throughs.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));
        models.forEach(o -> {
            R value = null;
            TH through = throughDictionary.get(ReflectUtil.getFieldValue(o, localField));
            if (through != null) {
                value = dictionary.get(ReflectUtil.getFieldValue(through, throughLocalField));
            }
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
    }
}
