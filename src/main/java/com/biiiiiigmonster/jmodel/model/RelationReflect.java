package com.biiiiiigmonster.jmodel.model;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/31 19:34
 */
@Getter
@Slf4j
public class RelationReflect<T, R> {
    private final Class<T> clazz;
    private final String fieldName;
    private Field relatedField;
    private Field localField;
    private Field foreignField;
    private Boolean relatedFieldIsList;
    private Map<Object, Method> repositoryMethod;

    public RelationReflect(Class<T> clazz, String fieldName) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        parse();
    }

    private void parse() {
        relatedField = ReflectUtil.getField(clazz, fieldName);
        relatedField.setAccessible(true);
        relatedFieldIsList = List.class.isAssignableFrom(relatedField.getType());
        Relation relation = relatedField.getAnnotation(Relation.class);
        localField = ReflectUtil.getField(relatedField.getDeclaringClass(), relation.localKey());
        localField.setAccessible(true);
        foreignField = ReflectUtil.getField(RelationUtils.getGenericType(relatedField), relation.foreignKey());
        foreignField.setAccessible(true);

        String cacheKey = String.format("%s.%s", relatedField.getDeclaringClass().getName(), relatedField.getName());
        repositoryMethod = RelationUtils.getRepositoryMethodList(cacheKey, foreignField);
        Assert.assertNotNull("未找到仓库", repositoryMethod);
    }

    public <TL> List<R> fetchForeignResult(List<T> list) {
        List<TL> localKeyList = list.stream()
                .map(o -> (TL) ReflectUtil.getFieldValue(o, localField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(localKeyList)) {
            return new ArrayList<>();
        }

        Object bean = repositoryMethod.keySet().iterator().next();
        Method method = repositoryMethod.values().iterator().next();
        if (List.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return ReflectUtil.invoke(bean, method, localKeyList);
        } else {
            log.warn("{}存在N + 1查询隐患，建议{}实现List参数的仓库方法", bean.getClass().getName(), method.getName());
            return localKeyList.stream()
                    .map(param -> ReflectUtil.invoke(bean, method, param))
                    .filter(Objects::nonNull)
                    .flatMap(r -> {
                        if (List.class.isAssignableFrom(method.getReturnType())) {
                            return ((List<R>) r).stream();
                        } else {
                            return Stream.of((R) r);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public <TL> void setRelation(List<T> models, List<R> foreign) {
        Map<TL, List<R>> map = foreign.stream()
                .collect(Collectors.groupingBy(r -> (TL) ReflectUtil.getFieldValue(r, foreignField)));

        models.forEach(o -> {
            List<R> valList = map.get((TL) ReflectUtil.getFieldValue(o, localField));
            if (relatedFieldIsList) {
                ReflectUtil.setFieldValue(o, relatedField, valList == null ? new ArrayList<>() : valList);
            } else if (ObjectUtil.isNotEmpty(valList)) {
                ReflectUtil.setFieldValue(o, relatedField, valList.get(0));
            }
        });
    }
}
