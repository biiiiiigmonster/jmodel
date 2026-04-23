package io.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes"})
public class HasOneThrough<T extends Model<?>, TH extends Model<?>> extends HasOneOrManyThrough<T, TH> {

    /**
     * @param relatedField        Mechanic.carOwner
     * @param throughClass        Car.class
     * @param foreignField        Car.mechanic_id
     * @param throughForeignField Owner.car_id
     * @param localField          Mechanic.id
     * @param throughLocalField   Car.id
     */
    public HasOneThrough(Field relatedField, List<RelationVia> viaList, Class<TH> throughClass, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField, viaList, throughClass, foreignField, throughForeignField, localField, throughLocalField);
    }

    @Override
    public <R extends Model<?>> List<R> match(List<T> models, List<R> results) {
        List<TH> throughs = viaList.get(0).getResults();
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, throughForeignField), r -> r));
        Map<?, TH> throughDictionary = throughs.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r));
        models.forEach(o -> {
            R value = null;
            TH through = throughDictionary.get(ReflectUtil.getFieldValue(o, localField));
            if (through != null) {
                value = dictionary.get(ReflectUtil.getFieldValue(through, throughLocalField));
            }
            ReflectUtil.setFieldValue(o, relatedField, value);
        });
        return results;
    }
}
