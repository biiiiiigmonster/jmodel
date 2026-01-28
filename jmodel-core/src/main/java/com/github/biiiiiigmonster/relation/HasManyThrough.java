package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HasManyThrough<T extends Model<?>, TH extends Model<?>> extends HasOneOrManyThrough<T, TH> {

    /**
     * @param relatedField        Project.deployments
     * @param throughClass        Environment.class
     * @param foreignField        Environment.project_id
     * @param throughForeignField Deployment.environment_id
     * @param localField          Project.id
     * @param throughLocalField   Environment.id
     */
    public HasManyThrough(Field relatedField, Class<TH> throughClass, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField, throughClass, foreignField, throughForeignField, localField, throughLocalField);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void throughMatch(List<T> models, List<TH> throughs, List<R> results) {
        Map<?, List<R>> dictionary = results.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, throughForeignField)));
        Map<?, List<TH>> throughDictionary = throughs.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignField)));
        models.forEach(o -> {
            List<R> valList = throughDictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>())
                    .stream()
                    .flatMap(th -> dictionary.get(ReflectUtil.getFieldValue(th, throughLocalField)).stream())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }
}
