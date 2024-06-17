package com.biiiiiigmonster.octopus.model;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
public class RelationReflect<T extends Model<?>, R extends Model<?>> {
    private final Class<T> clazz;
    private final String fieldName;
    private Field relatedField;
    private Field localField;
    private Field foreignField;
    private Boolean relatedFieldIsList;

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
    }

    public <TL> List<R> fetchForeignResult(List<T> eager) {
        List<TL> localKeyValueList = eager.stream()
                .map(o -> (TL) ReflectUtil.getFieldValue(o, localField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        return RelationUtils.hasRelatedRepository((Class<R>) foreignField.getDeclaringClass())
                ? byRelatedRepository(localKeyValueList) : byRelatedMethod(localKeyValueList);
    }

    private <TL> List<R> byRelatedRepository(List<TL> localKeyValueList) {
        IService<R> relatedRepository = RelationUtils.getRelatedRepository((Class<R>) foreignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query().in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.list(wrapper);
    }

    private <TL> List<R> byRelatedMethod(List<TL> localKeyValueList) {
        String cacheKey = String.format("%s.%s", foreignField.getDeclaringClass().getName(), foreignField.getName());
        Map<Object, Method> relatedMethod = RelationUtils.getRelatedMethod(cacheKey, foreignField);
//        Assert.assertNotNull("未找到仓库" + cacheKey, relatedMethod);

        Object bean = relatedMethod.keySet().iterator().next();
        Method method = relatedMethod.values().iterator().next();
        if (List.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return ReflectUtil.invoke(bean, method, localKeyValueList);
        } else {
            log.warn("{}存在N + 1查询隐患，建议{}实现List参数的仓库方法", bean.getClass().getName(), method.getName());
            return localKeyValueList.stream()
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

    public <TL> void match(List<T> models, List<R> results) {
        Map<TL, List<R>> dictionary = results.stream()
                .collect(Collectors.groupingBy(r -> (TL) ReflectUtil.getFieldValue(r, foreignField)));

        models.forEach(o -> {
            List<R> valList = dictionary.getOrDefault((TL) ReflectUtil.getFieldValue(o, localField), new ArrayList<>());
            if (relatedFieldIsList) {
                ReflectUtil.setFieldValue(o, relatedField, valList);
            } else {
                if (ObjectUtil.isNotEmpty(valList)) {
                    ReflectUtil.setFieldValue(o, relatedField, valList.get(0));
                }
            }
        });
    }
}
