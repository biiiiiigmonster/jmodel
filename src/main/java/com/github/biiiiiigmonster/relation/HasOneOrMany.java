package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class HasOneOrMany extends Relation {
    protected Field foreignField;
    protected Field localField;
    protected boolean chaperone;

    public HasOneOrMany(Field relatedField, Field foreignField, Field localField, boolean chaperone) {
        super(relatedField);

        this.foreignField = foreignField;
        this.localField = localField;
        this.chaperone = chaperone;
    }

    @Override
    public <T extends Model<?>, R extends Model<?>> List<R> getEager(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        return getResult(localKeyValueList, foreignField, this::byRelatedRepository);
    }

    protected <R extends Model<?>> List<R> byRelatedRepository(List<?> localKeyValueList) {
        BaseMapper<R> relatedRepository = (BaseMapper<R>) RelationUtils.getRelatedRepository(foreignField.getDeclaringClass());
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(foreignField), localKeyValueList);
        return relatedRepository.selectList(wrapper);
    }

    protected <T extends Model<?>, R extends Model<?>> void inverseMatch(R result, T model) {
        if (!chaperone || result == null) {
            return;
        }

        for (Field field : ReflectUtil.getFields(result.getClass())) {
            if (field.getType() == model.getClass()) {
                if (field.getAnnotation(BelongsTo.class) != null || field.getAnnotation(MorphTo.class) != null) {
                    ReflectUtil.setFieldValue(result, field, model);
                }
            }
        }
    }
}
