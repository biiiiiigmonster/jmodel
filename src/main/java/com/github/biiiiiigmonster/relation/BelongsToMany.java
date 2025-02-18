package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BelongsToMany<P extends Pivot<?>> extends Relation {
    protected Field foreignPivotField;// UserRole.user_id
    protected Field relatedPivotField;// UserRole.role_id
    protected Field localField;// User.id
    protected Field foreignField;// Role.id
    protected Class<P> pivotClass;

    public BelongsToMany(Field relatedField, Class<P> pivotClass, Field foreignPivotField, Field relatedPivotField, Field localField, Field foreignField) {
        super(relatedField);

        this.pivotClass = pivotClass;
        this.foreignPivotField = foreignPivotField;
        this.relatedPivotField = relatedPivotField;
        this.localField = localField;
        this.foreignField = foreignField;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = models.stream()
                .map(o -> ReflectUtil.getFieldValue(o, localField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        IService<P> pivotRepository = (IService<P>) RelationUtils.getRelatedRepository(foreignPivotField.getDeclaringClass());
        QueryChainWrapper<P> pivotWrapper = pivotRepository.query()
                .in(RelationUtils.getColumn(foreignPivotField), localKeyValueList);
        List<P> pivots = pivotRepository.list(pivotWrapper);
        List<?> relatedPivotKeyValueList = pivots.stream()
                .map(o -> ReflectUtil.getFieldValue(o, relatedPivotField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(relatedPivotKeyValueList)) {
            return new ArrayList<>();
        }

        // 多对多只支持从Repository中获取
        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query().in(RelationUtils.getColumn(foreignField), relatedPivotKeyValueList);
        List<R> results = relatedRepository.list(wrapper);
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));
        Map<?, List<P>> pivotDictionary = pivots.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignPivotField)));
        models.forEach(o -> {
            List<R> valList = pivotDictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>())
                    .stream()
                    .map(p -> dictionary.get(ReflectUtil.getFieldValue(p, relatedPivotField)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });

        return results;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {}
}
