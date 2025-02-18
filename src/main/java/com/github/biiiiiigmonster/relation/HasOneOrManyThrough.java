package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class HasOneOrManyThrough<TR extends Model<?>> extends Relation {
    protected Field foreignField; // Car.mechanic_id
    protected Field throughForeignField; // Owner.car_id
    protected Field localField; // Mechanic.id
    protected Field throughLocalField; // Car.id
    protected Class<TR> throughClass;

    public HasOneOrManyThrough(Field relatedField, Class<TR> throughClass, Field foreignField, Field throughForeignField, Field localField, Field throughLocalField) {
        super(relatedField);

        this.throughClass = throughClass;
        this.foreignField = foreignField;
        this.throughForeignField = throughForeignField;
        this.localField = localField;
        this.throughLocalField = throughLocalField;
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

        IService<TR> throughRepository = (IService<TR>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryChainWrapper<TR> throughWrapper = throughRepository.query()
                .in(RelationUtils.getColumn(foreignField), localKeyValueList);
        List<TR> throughs = throughRepository.list(throughWrapper);
        List<?> throughKeyValueList = throughs.stream()
                .map(o -> ReflectUtil.getFieldValue(o, throughLocalField))
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(throughKeyValueList)) {
            return new ArrayList<>();
        }

        // 远程一对多只支持从Repository中获取
        IService<R> relatedRepository = (IService<R>) RelationUtils.getRelatedRepository(throughForeignField.getDeclaringClass());
        QueryChainWrapper<R> wrapper = relatedRepository.query().in(RelationUtils.getColumn(throughForeignField), throughKeyValueList);
        List<R> results = relatedRepository.list(wrapper);
        // 预匹配
        throughMatch(models, throughs, results);

        return results;
    }

    public abstract<T extends Model<?>, R extends Model<?>> void throughMatch(List<T> models, List<TR> throughs, List<R> results);
}
