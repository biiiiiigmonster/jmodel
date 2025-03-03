package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HasManyThrough<TH extends Model<?>> extends HasOneOrManyThrough<TH> {

    /**
     * @param relatedField        Project.deployments
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
        Map<?, TH> throughDictionary = throughs.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));
        models.forEach(o -> {
            List<R> valList = new ArrayList<>();
            TH through = throughDictionary.get(ReflectUtil.getFieldValue(o, localField));
            if (through != null) {
                valList = dictionary.getOrDefault(ReflectUtil.getFieldValue(through, throughLocalField), new ArrayList<>());
            }
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });
    }
}
