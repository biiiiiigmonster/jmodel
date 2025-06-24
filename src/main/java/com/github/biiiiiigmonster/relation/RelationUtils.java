package com.github.biiiiiigmonster.relation;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.SerializableFunction;
import com.github.biiiiiigmonster.SerializedLambda;
import com.github.biiiiiigmonster.relation.annotation.config.Related;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author luyunfeng
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class RelationUtils implements BeanPostProcessor {
    private static final Map<Class<?>, BaseMapper<?>> RELATED_REPOSITORY_MAP = new HashMap<>();

    private static final Map<Class<?>, List<Map<String, Object>>> RELATED_MAP = new HashMap<>();

    private static final Map<String, Map<Object, Method>> MAP_CACHE = new HashMap<>();

    private static final Map<Class<?>, Map<String, ColumnCache>> COLUMN_MAP = new HashMap<>();

    @Autowired
    private ApplicationContext context;

    public static <T extends Model<?>> void load(T obj, String... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), Arrays.asList(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>, R> void load(T obj, SerializableFunction<T, R>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), SerializedLambda.resolveFieldNames(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, RelationOption<T>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), Arrays.asList(relations), false);
    }

    public static <T extends Model<?>> void load(List<T> list, String... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), Arrays.asList(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), SerializedLambda.resolveFieldNames(relations)), false);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, RelationOption<T>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, Arrays.asList(relations), false);
    }

    public static <T extends Model<?>> void load(T obj, boolean loadForce, String... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), Arrays.asList(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, SerializableFunction<T, ?>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), SerializedLambda.resolveFieldNames(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(T obj, boolean loadForce, RelationOption<T>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), Arrays.asList(relations), loadForce);
    }

    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, String... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), Arrays.asList(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), SerializedLambda.resolveFieldNames(relations)), loadForce);
    }

    @SafeVarargs
    public static <T extends Model<?>> void load(List<T> list, boolean loadForce, RelationOption<T>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, Arrays.asList(relations), loadForce);
    }

    public static <T extends Model<?>> void loadForce(List<T> list, String... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), Arrays.asList(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, SerializableFunction<T, ?>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, processRelations(list.get(0).getClass(), SerializedLambda.resolveFieldNames(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(List<T> list, RelationOption<T>... relations) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        load(list, Arrays.asList(relations), true);
    }

    public static <T extends Model<?>> void loadForce(T obj, String... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), Arrays.asList(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, SerializableFunction<T, ?>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), processRelations(obj.getClass(), SerializedLambda.resolveFieldNames(relations)), true);
    }

    @SafeVarargs
    public static <T extends Model<?>> void loadForce(T obj, RelationOption<T>... relations) {
        if (obj == null) {
            return;
        }

        load(ListUtil.toList(obj), Arrays.asList(relations), true);
    }

    /**
     * @param models
     * @param list
     * @param loadForce
     * @param <T>
     */
    private static <T extends Model<?>> void load(List<T> models, List<RelationOption<?>> list, boolean loadForce) {
        if (ObjectUtil.isEmpty(models)) {
            return;
        }

        list.forEach((relationOption) -> handle(models, relationOption, loadForce));
    }

    private static <T extends Model<?>, R extends Model<?>> void handle(List<T> models, RelationOption<?> relationOption, boolean loadForce) {
        // 分离
        List<T> eager = new ArrayList<>();
        List<T> exists = new ArrayList<>();
        List<R> results = new ArrayList<>();
        if (loadForce) {
            eager = models;
        } else {
            for (T model : models) {
                Object value = ReflectUtil.getFieldValue(model, relationOption.getRelatedField());
                if (value == null) {
                    eager.add(model);
                } else {
                    if (relationOption.isNested()) {
                        exists.add(model);
                        if (relationOption.isRelatedFieldList()) {
                            results.addAll((List<R>) value);
                        } else {
                            results.add((R) value);
                        }
                    }
                }
            }
        }
        Relation relation = relationOption.getRelation();
        // 合并关联结果
        results = merge(results, relation.getEager(eager));
        // 嵌套处理
        if (relationOption.isNested()) {
            load(results, relationOption.getNestedRelations(), loadForce);
        }

        // 合并父模型数据
        eager.addAll(exists);
        // 组装匹配
        relation.match(eager, results);
    }

    // 新旧结果集合并，如果重复以新结果集为准
    private static <T extends Model<?>> List<T> merge(List<T> oldList, List<T> newList) {
        if (CollectionUtils.isEmpty(oldList) || CollectionUtils.isEmpty(newList)) {
            return newList;
        }

        newList.addAll(oldList);
        return new ArrayList<>(newList.stream().collect(Collectors.toMap(
                r -> ReflectUtil.getFieldValue(r, RelationUtils.getPrimaryKey(r.getClass())),
                r -> r,
                (o1, o2) -> o1
        )).values());
    }

    private static <T extends Model<?>, R extends Model<?>> List<RelationOption<? extends Model<?>>> processRelations(Class<T> clazz, List<String> relations) {
        Map<String, List<String>> map = relations.stream()
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toMap(
                        relation -> StrUtil.subBefore(relation, ".", false),
                        relation -> {
                            String nestedRelation = StrUtil.subAfter(relation, ".", false);
                            return nestedRelation.isEmpty() ? new ArrayList<>() : ListUtil.toList(nestedRelation);
                        },
                        (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        }
                ));

        List<RelationOption<?>> list = new ArrayList<>();
        map.forEach((fieldName, nestedRelations) -> {
            RelationOption<?> relationOption = RelationOption.of(clazz, fieldName);
            if (!CollectionUtils.isEmpty(nestedRelations)) {
                relationOption.nested(processRelations((Class<R>) getGenericType(relationOption.getRelatedField()), nestedRelations));
            }
            list.add(relationOption);
        });

        return list;
    }

    public static String relatedMethodCacheKey(Field field) {
        return String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
    }

    public static Map<Object, Method> getRelatedMethod(Field field) {
        String key = relatedMethodCacheKey(field);
        return MAP_CACHE.computeIfAbsent(key, k ->
                RELATED_MAP.get(field.getDeclaringClass()).stream()
                        .filter(map -> {
                            Related related = (Related) map.get("annotation");
                            return related.field().equals(field.getName());
                        })
                        .map(map -> {
                            Map<Object, Method> cache = new HashMap<>();
                            cache.put(map.get("bean"), (Method) map.get("method"));
                            return cache;
                        })
                        .findFirst()
                        .orElse(null)
        );
    }

    public static BaseMapper<?> getRelatedRepository(Class<?> clazz) {
        return RELATED_REPOSITORY_MAP.get(clazz);
    }

    public static boolean hasRelatedRepository(Field field) {
        return RELATED_REPOSITORY_MAP.containsKey(field.getDeclaringClass());
    }

    public static String getColumn(Field foreignField) {
        Class<?> entityClass = foreignField.getDeclaringClass();

        Map<String, ColumnCache> columnMap = COLUMN_MAP.computeIfAbsent(entityClass, LambdaUtils::getColumnMap);
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(foreignField.getName()));
        return columnCache.getColumn();
    }

    public static Class<?> getGenericType(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return field.getType();
        }

        return getTypeClass((ParameterizedType) field.getGenericType());
    }

    public static Class<?> getGenericParameterType(Method method) {
        Class<?> paramClazz = method.getParameterTypes()[0];
        if (!List.class.isAssignableFrom(paramClazz)) {
            return paramClazz;
        }

        return getTypeClass((ParameterizedType) method.getGenericParameterTypes()[0]);
    }

    public static Class<?> getGenericReturnType(Method method) {
        if (!List.class.isAssignableFrom(method.getReturnType())) {
            return method.getReturnType();
        }

        return getTypeClass((ParameterizedType) method.getGenericReturnType());
    }

    private static Class<?> getTypeClass(ParameterizedType type) {
        Type[] typeArguments = type.getActualTypeArguments();
        for (Type typeArgument : typeArguments) {
            if (typeArgument instanceof Class) {
                return (Class<?>) typeArgument;
            }
        }

        return Object.class;
    }

    /**
     * 模型默认本地键名
     *
     * @param clazz model class
     * @return String
     */
    public static String getPrimaryKey(Class<?> clazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        return tableInfo == null ? "id" : tableInfo.getKeyColumn();
    }

    /**
     * 模型默认外地键名
     *
     * @param clazz model class
     * @return String
     */
    public static String getForeignKey(Class<?> clazz) {
        return StrUtil.lowerFirst(clazz.getSimpleName()) + StrUtil.upperFirst(getPrimaryKey(clazz));
    }

    /**
     * 启动时扫描一下一些model的加载是否有指定方法，默认都是@RelatedRepository
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            related(bean, method, method.getAnnotation(Related.class));
        }

        return bean;
    }

    @PostConstruct
    public void init() {
        context.getBeansOfType(BaseMapper.class).forEach((beanName, mapper) -> {
            Class<?> mapperClass = AopUtils.isAopProxy(mapper) ? AopUtils.getTargetClass(beanName) : mapper.getClass();
            Class<?> typeClass = ReflectionKit.getSuperClassGenericType(mapperClass, BaseMapper.class, 0);
            RELATED_REPOSITORY_MAP.put(typeClass, mapper);
        });
    }

    private void related(Object bean, Method method, Related annotation) {
        if (annotation == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("bean", bean);
        map.put("method", method);
        map.put("annotation", annotation);
        Class<?> foreignClazz = getGenericReturnType(method);
        RELATED_MAP.computeIfAbsent(foreignClazz, k -> new ArrayList<>()).add(map);
    }

    /**
     * 保存关联模型
     * 支持一对一、一对多关联
     */
    @SafeVarargs
    public static <T extends Model<?>, R> void saveRelations(T model, SerializableFunction<T, R>... relations) {
        if (model == null || relations == null || relations.length == 0) {
            return;
        }

        for (SerializableFunction<T, R> relation : relations) {
            Field field = SerializedLambda.getField(relation);
            R relatedModel = relation.apply(model);
            
            if (relatedModel != null) {
                RelationType relationType = RelationType.of(field);
                if (relationType == RelationType.HAS_ONE || relationType == RelationType.HAS_MANY) {
                    saveRelatedModel(model, field, relatedModel);
                }
            }
        }
    }

    /**
     * 保存关联模型（字符串方式）
     */
    public static <T extends Model<?>> void saveRelations(T model, String... relations) {
        if (model == null || relations == null || relations.length == 0) {
            return;
        }

        for (String relation : relations) {
            Field field = ReflectUtil.getField(model.getClass(), relation);
            if (field != null) {
                Object relatedModel = ReflectUtil.getFieldValue(model, field);
                if (relatedModel != null) {
                    RelationType relationType = RelationType.of(field);
                    if (relationType == RelationType.HAS_ONE || relationType == RelationType.HAS_MANY) {
                        saveRelatedModel(model, field, relatedModel);
                    }
                }
            }
        }
    }

    /**
     * 创建并保存关联模型
     */
    public static <T extends Model<?>, R> R createRelation(T model, SerializableFunction<T, R> relation, R relatedModel) {
        if (model == null || relation == null || relatedModel == null) {
            return null;
        }

        Field field = SerializedLambda.getField(relation);
        RelationType relationType = RelationType.of(field);
        
        if (relationType == RelationType.HAS_ONE || relationType == RelationType.HAS_MANY) {
            saveRelatedModel(model, field, relatedModel);
            return relatedModel;
        }
        
        return null;
    }

    /**
     * 创建并保存关联模型（字符串方式）
     */
    public static <T extends Model<?>, R> R createRelation(T model, String relation, R relatedModel) {
        if (model == null || relation == null || relatedModel == null) {
            return null;
        }

        Field field = ReflectUtil.getField(model.getClass(), relation);
        if (field != null) {
            RelationType relationType = RelationType.of(field);
            if (relationType == RelationType.HAS_ONE || relationType == RelationType.HAS_MANY) {
                saveRelatedModel(model, field, relatedModel);
                return relatedModel;
            }
        }
        
        return null;
    }

    /**
     * 附加关联（多对多）
     */
    @SafeVarargs
    public static <T extends Model<?>, R> void attachRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        if (model == null || relation == null || models == null || models.length == 0) {
            return;
        }

        Field field = SerializedLambda.getField(relation);
        RelationType relationType = RelationType.of(field);
        
        if (relationType == RelationType.BELONGS_TO_MANY) {
            attachManyToMany(model, field, Arrays.asList(models));
        }
    }

    /**
     * 附加关联（多对多，字符串方式）
     */
    public static <T extends Model<?>, R> void attachRelations(T model, String relation, R... models) {
        if (model == null || relation == null || models == null || models.length == 0) {
            return;
        }

        Field field = ReflectUtil.getField(model.getClass(), relation);
        if (field != null) {
            RelationType relationType = RelationType.of(field);
            if (relationType == RelationType.BELONGS_TO_MANY) {
                attachManyToMany(model, field, Arrays.asList(models));
            }
        }
    }

    /**
     * 分离关联（多对多）
     */
    @SafeVarargs
    public static <T extends Model<?>, R> void detachRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        if (model == null || relation == null) {
            return;
        }

        Field field = SerializedLambda.getField(relation);
        RelationType relationType = RelationType.of(field);
        
        if (relationType == RelationType.BELONGS_TO_MANY) {
            if (models == null || models.length == 0) {
                // 分离所有关联
                detachAllManyToMany(model, field);
            } else {
                // 分离指定关联
                detachManyToMany(model, field, Arrays.asList(models));
            }
        }
    }

    /**
     * 分离关联（多对多，字符串方式）
     */
    public static <T extends Model<?>, R> void detachRelations(T model, String relation, R... models) {
        if (model == null || relation == null) {
            return;
        }

        Field field = ReflectUtil.getField(model.getClass(), relation);
        if (field != null) {
            RelationType relationType = RelationType.of(field);
            if (relationType == RelationType.BELONGS_TO_MANY) {
                if (models == null || models.length == 0) {
                    // 分离所有关联
                    detachAllManyToMany(model, field);
                } else {
                    // 分离指定关联
                    detachManyToMany(model, field, Arrays.asList(models));
                }
            }
        }
    }

    /**
     * 同步关联（多对多）
     */
    @SafeVarargs
    public static <T extends Model<?>, R> void syncRelations(T model, SerializableFunction<T, List<R>> relation, R... models) {
        if (model == null || relation == null) {
            return;
        }

        Field field = SerializedLambda.getField(relation);
        RelationType relationType = RelationType.of(field);
        
        if (relationType == RelationType.BELONGS_TO_MANY) {
            // 先分离所有关联，再附加新的关联
            detachAllManyToMany(model, field);
            if (models != null && models.length > 0) {
                attachManyToMany(model, field, Arrays.asList(models));
            }
        }
    }

    /**
     * 同步关联（多对多，字符串方式）
     */
    public static <T extends Model<?>, R> void syncRelations(T model, String relation, R... models) {
        if (model == null || relation == null) {
            return;
        }

        Field field = ReflectUtil.getField(model.getClass(), relation);
        if (field != null) {
            RelationType relationType = RelationType.of(field);
            if (relationType == RelationType.BELONGS_TO_MANY) {
                // 先分离所有关联，再附加新的关联
                detachAllManyToMany(model, field);
                if (models != null && models.length > 0) {
                    attachManyToMany(model, field, Arrays.asList(models));
                }
            }
        }
    }

    /**
     * 更新关联模型
     */
    public static <T extends Model<?>, R> void updateRelation(T model, SerializableFunction<T, R> relation, R relatedModel) {
        if (model == null || relation == null || relatedModel == null) {
            return;
        }

        Field field = SerializedLambda.getField(relation);
        RelationType relationType = RelationType.of(field);
        
        if (relationType == RelationType.HAS_ONE || relationType == RelationType.HAS_MANY) {
            updateRelatedModel(model, field, relatedModel);
        }
    }

    /**
     * 更新关联模型（字符串方式）
     */
    public static <T extends Model<?>, R> void updateRelation(T model, String relation, R relatedModel) {
        if (model == null || relation == null || relatedModel == null) {
            return;
        }

        Field field = ReflectUtil.getField(model.getClass(), relation);
        if (field != null) {
            RelationType relationType = RelationType.of(field);
            if (relationType == RelationType.HAS_ONE || relationType == RelationType.HAS_MANY) {
                updateRelatedModel(model, field, relatedModel);
            }
        }
    }

    /**
     * 保存关联模型的具体实现
     */
    private static <T extends Model<?>> void saveRelatedModel(T model, Field field, Object relatedModel) {
        RelationType relationType = RelationType.of(field);
        
        if (relationType == RelationType.HAS_ONE) {
            // 设置外键
            setForeignKey(model, field, relatedModel);
            // 保存关联模型
            if (relatedModel instanceof Model) {
                ((Model<?>) relatedModel).save();
            }
        } else if (relationType == RelationType.HAS_MANY) {
            if (relatedModel instanceof List) {
                List<?> list = (List<?>) relatedModel;
                for (Object item : list) {
                    if (item instanceof Model) {
                        // 设置外键
                        setForeignKey(model, field, item);
                        // 保存关联模型
                        ((Model<?>) item).save();
                    }
                }
            }
        }
    }

    /**
     * 更新关联模型的具体实现
     */
    private static <T extends Model<?>> void updateRelatedModel(T model, Field field, Object relatedModel) {
        if (relatedModel instanceof Model) {
            Model<?> modelInstance = (Model<?>) relatedModel;
            // 如果模型有ID，则更新；否则插入
            if (modelInstance.primaryKeyValue() != null) {
                BaseMapper<Model<?>> mapper = (BaseMapper<Model<?>>) getRelatedRepository(modelInstance.getClass());
                mapper.updateById(modelInstance);
            } else {
                saveRelatedModel(model, field, relatedModel);
            }
        }
    }

    /**
     * 设置外键
     */
    private static <T extends Model<?>> void setForeignKey(T model, Field field, Object relatedModel) {
        RelationOption<?> relationOption = RelationOption.of(model.getClass(), field.getName());
        Relation relation = relationOption.getRelation();
        
        if (relation instanceof HasOneOrMany) {
            HasOneOrMany hasOneOrMany = (HasOneOrMany) relation;
            Field foreignField = hasOneOrMany.getForeignField();
            Field localField = hasOneOrMany.getLocalField();
            
            Object localValue = ReflectUtil.getFieldValue(model, localField);
            ReflectUtil.setFieldValue(relatedModel, foreignField, localValue);
        }
    }

    /**
     * 附加多对多关联
     */
    private static <T extends Model<?>, R> void attachManyToMany(T model, Field field, List<R> models) {
        RelationOption<?> relationOption = RelationOption.of(model.getClass(), field.getName());
        Relation relation = relationOption.getRelation();
        
        if (relation instanceof BelongsToMany) {
            BelongsToMany<?> belongsToMany = (BelongsToMany<?>) relation;
            Class<?> pivotClass = belongsToMany.getPivotClass();
            Field foreignPivotField = belongsToMany.getForeignPivotField();
            Field relatedPivotField = belongsToMany.getRelatedPivotField();
            Field localField = belongsToMany.getLocalField();
            Field foreignField = belongsToMany.getForeignField();
            
            Object localValue = ReflectUtil.getFieldValue(model, localField);
            
            for (R relatedModel : models) {
                if (relatedModel instanceof Model) {
                    Object foreignValue = ReflectUtil.getFieldValue(relatedModel, foreignField);
                    
                    // 创建中间表记录
                    try {
                        Object pivot = pivotClass.getDeclaredConstructor().newInstance();
                        ReflectUtil.setFieldValue(pivot, foreignPivotField, localValue);
                        ReflectUtil.setFieldValue(pivot, relatedPivotField, foreignValue);
                        
                        BaseMapper<Object> pivotMapper = (BaseMapper<Object>) getRelatedRepository(pivotClass);
                        pivotMapper.insert(pivot);
                    } catch (Exception e) {
                        log.error("Failed to create pivot record", e);
                    }
                }
            }
        }
    }

    /**
     * 分离多对多关联
     */
    private static <T extends Model<?>, R> void detachManyToMany(T model, Field field, List<R> models) {
        RelationOption<?> relationOption = RelationOption.of(model.getClass(), field.getName());
        Relation relation = relationOption.getRelation();
        
        if (relation instanceof BelongsToMany) {
            BelongsToMany<?> belongsToMany = (BelongsToMany<?>) relation;
            Class<?> pivotClass = belongsToMany.getPivotClass();
            Field foreignPivotField = belongsToMany.getForeignPivotField();
            Field relatedPivotField = belongsToMany.getRelatedPivotField();
            Field localField = belongsToMany.getLocalField();
            Field foreignField = belongsToMany.getForeignField();
            
            Object localValue = ReflectUtil.getFieldValue(model, localField);
            List<Object> foreignValues = new ArrayList<>();
            
            for (R relatedModel : models) {
                if (relatedModel instanceof Model) {
                    Object foreignValue = ReflectUtil.getFieldValue(relatedModel, foreignField);
                    foreignValues.add(foreignValue);
                }
            }
            
            if (!foreignValues.isEmpty()) {
                // 删除中间表记录
                BaseMapper<Object> pivotMapper = (BaseMapper<Object>) getRelatedRepository(pivotClass);
                QueryWrapper<Object> wrapper = new QueryWrapper<>();
                wrapper.eq(RelationUtils.getColumn(foreignPivotField), localValue)
                       .in(RelationUtils.getColumn(relatedPivotField), foreignValues);
                pivotMapper.delete(wrapper);
            }
        }
    }

    /**
     * 分离所有多对多关联
     */
    private static <T extends Model<?>> void detachAllManyToMany(T model, Field field) {
        RelationOption<?> relationOption = RelationOption.of(model.getClass(), field.getName());
        Relation relation = relationOption.getRelation();
        
        if (relation instanceof BelongsToMany) {
            BelongsToMany<?> belongsToMany = (BelongsToMany<?>) relation;
            Class<?> pivotClass = belongsToMany.getPivotClass();
            Field foreignPivotField = belongsToMany.getForeignPivotField();
            Field localField = belongsToMany.getLocalField();
            
            Object localValue = ReflectUtil.getFieldValue(model, localField);
            
            // 删除所有中间表记录
            BaseMapper<Object> pivotMapper = (BaseMapper<Object>) getRelatedRepository(pivotClass);
            QueryWrapper<Object> wrapper = new QueryWrapper<>();
            wrapper.eq(RelationUtils.getColumn(foreignPivotField), localValue);
            pivotMapper.delete(wrapper);
        }
    }

    /**
     * 切换多对多关联
     * @param model 主模型
     * @param relation 关联方法引用
     * @param ids 要切换的ID列表
     * @param <T> 主模型类型
     * @param <R> 关联模型类型
     * @return 切换后的关联ID列表
     */
    public static <T extends Model<?>, R extends Model<?>> List<Long> toggle(T model, SerializableFunction<R, ?> relation, List<Long> ids) {
        String methodName = SerializedLambda.resolve(relation).getImplMethodName();
        String fieldName = methodToFieldName(methodName);
        return toggle(model, fieldName, ids);
    }

    /**
     * 将getter/setter方法名转换为字段名
     */
    private static String methodToFieldName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("set") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return methodName;
    }

    /**
     * 切换多对多关联（字符串方式）
     * @param model 主模型
     * @param relationName 关联名称
     * @param ids 要切换的ID列表
     * @param <T> 主模型类型
     * @return 切换后的关联ID列表
     */
    public static <T extends Model<?>> List<Long> toggle(T model, String relationName, List<Long> ids) {
        try {
            // 获取关联字段
            Field field = ReflectUtil.getField(model.getClass(), relationName);
            if (field == null) {
                throw new RuntimeException("Relation field not found: " + relationName);
            }

            // 获取当前关联的ID列表
            List<Long> currentIds = getCurrentRelationIds(model, relationName);
            
            // 计算要添加和移除的ID
            List<Long> toAdd = new ArrayList<>();
            List<Long> toRemove = new ArrayList<>();
            
            for (Long id : ids) {
                if (currentIds.contains(id)) {
                    toRemove.add(id);
                } else {
                    toAdd.add(id);
                }
            }
            
            // 执行添加和移除操作
            if (!toAdd.isEmpty()) {
                // 通过ID创建模型对象并附加
                List<Model<?>> modelsToAdd = createModelsByIds(model, field, toAdd);
                attachManyToMany(model, field, modelsToAdd);
            }
            if (!toRemove.isEmpty()) {
                // 通过ID创建模型对象并分离
                List<Model<?>> modelsToRemove = createModelsByIds(model, field, toRemove);
                detachManyToMany(model, field, modelsToRemove);
            }
            
            // 返回最终的关联ID列表
            return getCurrentRelationIds(model, relationName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to toggle relation: " + relationName, e);
        }
    }

    /**
     * 获取当前关联的ID列表
     * @param model 主模型
     * @param relationName 关联名称
     * @param <T> 主模型类型
     * @return 当前关联的ID列表
     */
    private static <T extends Model<?>> List<Long> getCurrentRelationIds(T model, String relationName) {
        try {
            // 重新查一次数据库，拿到最新的对象
            Object pk = model.primaryKeyValue();
            if (pk == null) {
                return new ArrayList<>();
            }
            T freshModel = (T) model.find((java.io.Serializable) pk);
            freshModel.load(relationName);
            Field field = ReflectUtil.getField(freshModel.getClass(), relationName);
            Object relationResult = ReflectUtil.getFieldValue(freshModel, field);
            if (relationResult instanceof List) {
                List<?> relatedModels = (List<?>) relationResult;
                return relatedModels.stream()
                    .map(related -> {
                        if (related instanceof Model) {
                            return (Long) ((Model<?>) related).primaryKeyValue();
                        }
                        return null;
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to get current relation IDs: " + relationName, e);
            throw new RuntimeException("Failed to get current relation IDs: " + relationName, e);
        }
    }

    /**
     * 通过ID列表创建模型对象
     * @param model 主模型
     * @param field 关联字段
     * @param ids ID列表
     * @param <T> 主模型类型
     * @return 模型对象列表
     */
    private static <T extends Model<?>> List<Model<?>> createModelsByIds(T model, Field field, List<Long> ids) {
        try {
            Class<?> relatedClass = getGenericType(field);
            List<Model<?>> models = new ArrayList<>();
            
            for (Long id : ids) {
                // 创建模型实例并设置ID
                Model<?> relatedModel = (Model<?>) relatedClass.getDeclaredConstructor().newInstance();
                Field idField = ReflectUtil.getField(relatedClass, "id");
                if (idField != null) {
                    ReflectUtil.setFieldValue(relatedModel, idField, id);
                }
                models.add(relatedModel);
            }
            
            return models;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create models by IDs", e);
        }
    }
}
