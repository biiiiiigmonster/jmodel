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
import java.util.stream.Collectors;

public class BelongsToMany extends Relation {
    protected Field foreignPivotField;
    protected Field relatedPivotField;
    protected Field localField;
    protected Field foreignField;

    public BelongsToMany(Field relatedField, Field foreignPivotField, Field relatedPivotField, Field localField, Field foreignField) {
        super(relatedField);

        this.foreignPivotField = foreignPivotField;
        this.relatedPivotField = relatedPivotField;
        this.localField = localField;
        this.foreignField = foreignField;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = models.stream()
                .map(o -> ReflectUtil.getFieldValue(o, localField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(localKeyValueList)) {
            return new ArrayList<>();
        }

        // 多对多只支持从Repository中获取
        IService<R> relatedRepository = RelationUtils.getRelatedRepository((Class<R>) foreignField.getDeclaringClass());
        String formatSql = String.format("%s in (select %s from %s where %s in {0})",
                RelationUtils.getColumn(foreignField),
                RelationUtils.getColumn(relatedPivotField),
                StrUtil.toUnderlineCase(foreignPivotField.getDeclaringClass().getSimpleName()),
                RelationUtils.getColumn(foreignPivotField)
        );
        QueryChainWrapper<R> wrapper = relatedRepository.query().apply(formatSql, localKeyValueList);
        return relatedRepository.list(wrapper);
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> void match(List<T> models, List<R> results) {

    }
}
