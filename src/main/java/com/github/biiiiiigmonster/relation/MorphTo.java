package com.github.biiiiiigmonster.relation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.biiiiiigmonster.Model;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MorphTo extends BelongsTo {
    protected Field morphType;
    protected String ownerKey;

    /**
     * @param relatedField Image.imageable
     * @param morphType Image.imageable_type
     * @param foreignField Image.imageable_id
     * @param ownerKey id
     */
    public MorphTo(Field relatedField, Field morphType, Field foreignField, String ownerKey) {
        super(relatedField, foreignField, null);

        this.morphType = morphType;
        this.ownerKey = ownerKey;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> List<Model<?>> getEager(List<T> models) {
        Map<String, List<T>> morphMap = models.stream().collect(Collectors.groupingBy(o -> (String) ReflectUtil.getFieldValue(o, morphType)));
        List<Model<?>> results = new ArrayList<>();
        morphMap.forEach((k, morphModels) -> {
            Class<Model<?>> morphClass = (Class<Model<?>>) getMorphClass(k);
            List<?> ownerKeyValueList = relatedKeyValueList(morphModels, foreignField);
            if (ObjectUtil.isEmpty(ownerKeyValueList)) {
                return;
            }

            IService<?> relatedRepository = RelationUtils.getRelatedRepository(morphClass);
            QueryChainWrapper<?> wrapper = relatedRepository.query().in(RelationUtils.getColumn(morphOwnerField(morphClass)), ownerKeyValueList);
            results.addAll((List<Model<?>>) wrapper.list());
        });

        return results;
    }

    private Field morphOwnerField(Class<?> clazz) {
        String key = StringUtils.isEmpty(ownerKey) ? RelationUtils.getPrimaryKey(clazz) : ownerKey;
        return ReflectUtil.getField(clazz, key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void match(List<T> models, List<Model<?>> results) {
        if (ObjectUtil.isEmpty(results)) {
            return;
        }

        Map<String, List<T>> morphMap = models.stream().collect(Collectors.groupingBy(o -> (String) ReflectUtil.getFieldValue(o, morphType)));
        morphMap.forEach((k, morphModels) -> {
            Class<Model<?>> morphClass = (Class<Model<?>>) getMorphClass(k);
            Map<?, Model<?>> dictionary = results.stream()
                    .filter(r -> ReflectUtil.getFieldValue(r, morphType) == Relation.getMorphAlias(morphClass))
                    .collect(Collectors.toMap(r -> ReflectUtil.getFieldValue(r, foreignField), r -> r, (o1, o2) -> o1));

            morphModels.forEach(o -> {
                Model<?> value = dictionary.get(ReflectUtil.getFieldValue(o, morphOwnerField(morphClass)));
                ReflectUtil.setFieldValue(o, relatedField, value);
            });
        });
    }
}
