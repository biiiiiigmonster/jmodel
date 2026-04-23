package io.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.QueryCondition;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import io.github.biiiiiigmonster.relation.annotation.config.MorphId;
import io.github.biiiiiigmonster.relation.annotation.config.MorphName;
import io.github.biiiiiigmonster.relation.annotation.config.MorphType;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Relation<T extends Model<?>> {
    protected Field relatedField;
    protected List<RelationVia> viaList;
    protected T model;

    protected List<Consumer<QueryCondition>> constraints = new ArrayList<>();

    private static final Map<String, String> MORPH_ALIAS_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Morph> MORPH_MAP = new ConcurrentHashMap<>();

    public Relation(Field relatedField, List<RelationVia> viaList) {
        this.relatedField = relatedField;
        this.viaList = viaList;
    }

    public <R extends Model<?>> List<R> getEager(List<T> models) {
        if (CollectionUtils.isEmpty(viaList)) {
            return new ArrayList<>();
        }

        List<?> results = ListUtil.toList(models);
        for (int i = 0; i < viaList.size(); i++) {
            RelationVia via = viaList.get(i);
            if (i == viaList.size() - 1) {
                via.addConstraints(constraints);
            }

            results = via.getResult(results);
            if (CollectionUtils.isEmpty(results)) {
                return new ArrayList<>();
            }
        }

        return (List<R>) results;
    }

    public abstract <R extends Model<?>> List<R> match(List<T> models, List<R> results);

    /**
     * 追加约束（可为来自注解的也可为运行时的）
     */
    public Relation<T> addConstraint(Consumer<QueryCondition> constraint) {
        if (constraint != null) {
            this.constraints.add(constraint);
        }
        return this;
    }

    /**
     * 批量追加约束
     */
    public Relation<T> addConstraints(List<Consumer<QueryCondition>> list) {
        if (list != null && !list.isEmpty()) {
            this.constraints.addAll(list);
        }
        return this;
    }

    public static <T extends Model<?>> List<?> relatedKeyValueList(List<T> models, Field field) {
        return models.stream().map(o -> ReflectUtil.getFieldValue(o, field)).filter(ObjectUtil::isNotEmpty).distinct().collect(Collectors.toList());
    }

    public static String getMorphAlias(Class<?> clazz, Class<?> within) {
        String key = clazz.getName() + within.getName();
        return MORPH_ALIAS_MAP.computeIfAbsent(key, k ->
                Arrays.stream(clazz.getAnnotationsByType(MorphAlias.class))
                        .filter(m -> m.in().length == 0 || Arrays.stream(m.in()).collect(Collectors.toSet()).contains(within))
                        // MorphAlias allowed annotation more times, get the max in() length when multi matched.
                        .max(Comparator.comparingInt(m -> m.in().length))
                        .map(m -> StringUtils.isBlank(m.value()) ? clazz.getSimpleName() : m.value())
                        .orElse(clazz.getName())
        );
    }

    public static Morph getMorph(Class<?> clazz) {
        return MORPH_MAP.computeIfAbsent(clazz, k -> {
            String name = StrUtil.lowerFirst(k.getSimpleName());
            MorphName morphName = k.getAnnotation(MorphName.class);
            if (morphName != null && StringUtils.isNotBlank(morphName.value())) {
                name = morphName.value();
            }

            String type = String.format("%sType", name);
            String id = String.format("%sId", name);

            for (Field field : k.getDeclaredFields()) {
                MorphType morphType = field.getAnnotation(MorphType.class);
                if (morphType != null) {
                    type = field.getName();
                }
                MorphId morphId = field.getAnnotation(MorphId.class);
                if (morphId != null) {
                    id = field.getName();
                }
            }

            return new Morph(type, id);
        });
    }

    public Relation<T> setModel(T model) {
        this.model = model;
        return this;
    }
}
