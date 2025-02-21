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

public class BelongsToMany extends Relation {
    protected Field foreignPivotField;// UserRole.user_id
    protected Field relatedPivotField;// UserRole.role_id
    protected Field localField;// User.id
    protected Field foreignField;// Role.id

    /**
     * @param relatedField User.roles
     * @param foreignPivotField UserRole.user_id
     * @param relatedPivotField UserRole.role_id
     * @param localField User.id
     * @param foreignField Role.id
     */
    public BelongsToMany(Field relatedField, Field foreignPivotField, Field relatedPivotField, Field localField, Field foreignField) {
        super(relatedField);

        this.foreignPivotField = foreignPivotField;
        this.relatedPivotField = relatedPivotField;
        this.localField = localField;
        this.foreignField = foreignField;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> List<Model<?>> getEager(List<T> models) {
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
        IService<?> relatedRepository = RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryChainWrapper<?> wrapper = relatedRepository.query().in(RelationUtils.getColumn(foreignField), relatedPivotKeyValueList);
        List<Model<?>> results = (List<Model<?>>) wrapper.list();
        Map<?, Model<?>> dictionary = results.stream()
                .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));
        Map<?, List<Pivot<?>>> pivotDictionary = pivots.stream()
                .collect(Collectors.groupingBy(r -> ReflectUtil.getFieldValue(r, foreignPivotField)));
        models.forEach(o -> {
            List<Model<?>> valList = pivotDictionary.getOrDefault(ReflectUtil.getFieldValue(o, localField), new ArrayList<>())
                    .stream()
                    .map(p -> dictionary.get(ReflectUtil.getFieldValue(p, relatedPivotField)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ReflectUtil.setFieldValue(o, relatedField, valList);
        });

        return results;
    }

    @Override
    public <T extends Model<?>> void match(List<T> models, List<Model<?>> results) {}
}
