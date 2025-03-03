package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BelongsToMany extends Relation {
    protected Field foreignPivotField;
    protected Field relatedPivotField;
    protected Field foreignField;
    protected Field localField;

    /**
     * @param relatedField      User.roles
     * @param foreignPivotField UserRole.user_id
     * @param relatedPivotField UserRole.role_id
     * @param localField        User.id
     * @param foreignField      Role.id
     */
    public BelongsToMany(Field relatedField, Field foreignPivotField, Field relatedPivotField, Field foreignField, Field localField) {
        super(relatedField);

        this.foreignPivotField = foreignPivotField;
        this.relatedPivotField = relatedPivotField;
        this.foreignField = foreignField;
        this.localField = localField;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        IService<?> pivotRepository = RelationUtils.getRelatedRepository(foreignPivotField.getDeclaringClass());
        QueryChainWrapper<?> pivotWrapper = pivotRepository.query()
                .in(RelationUtils.getColumn(foreignPivotField), localKeyValueList);
        List<Pivot<?>> pivots = (List<Pivot<?>>) pivotWrapper.list();
        List<?> relatedPivotKeyValueList = relatedKeyValueList(pivots, relatedPivotField);
        if (ObjectUtil.isEmpty(relatedPivotKeyValueList)) {
            return new ArrayList<>();
        }

        // 多对多只支持从Repository中获取
        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), relatedPivotKeyValueList);
        List<R> results = relatedRepository.list(wrapper);
        Map<?, R> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));
        Map<?, List<Pivot<?>>> pivotDictionary = pivots.stream()
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
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {
    }
}
