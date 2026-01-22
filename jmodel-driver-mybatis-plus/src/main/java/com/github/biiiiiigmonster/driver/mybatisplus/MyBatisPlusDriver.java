package com.github.biiiiiigmonster.driver.mybatisplus;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.DataDriver;
import com.github.biiiiiigmonster.driver.QueryCondition;
import com.github.biiiiiigmonster.driver.QueryCondition.Criterion;
import com.github.biiiiiigmonster.driver.QueryCondition.CriterionType;
import com.github.biiiiiigmonster.driver.exception.DriverOperationException;
import com.github.biiiiiigmonster.driver.exception.QueryConditionException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus 数据驱动实现
 * 将 DataDriver 接口的操作委托给 MyBatis-Plus 的 BaseMapper
 * 同时提供实体元数据信息
 *
 * @author jmodel
 */
@Component
@SuppressWarnings("unchecked")
public class MyBatisPlusDriver implements DataDriver<Model<?>> {

    /**
     * Spring 应用上下文
     */
    @Resource
    private ApplicationContext applicationContext;

    /**
     * Mapper 缓存，避免重复查找
     */
    private static final Map<Class<?>, BaseMapper<?>> MAPPER_CACHE = new ConcurrentHashMap<>();

    /**
     * 主键字段缓存
     */
    private static final Map<Class<Model<?>>, String> PRIMARY_KEY_CACHE = new ConcurrentHashMap<>();

    /**
     * 列名缓存
     */
    private static final Map<Field, String> COLUMN_NAME_CACHE = new ConcurrentHashMap<>();

    // ===== 元数据方法实现 =====

    @Override
    public String getPrimaryKey(Class<Model<?>> entityClass) {
        return PRIMARY_KEY_CACHE.computeIfAbsent(entityClass, clazz -> {
            for (Field field : getAllFields(clazz)) {
                TableId tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    return field.getName();
                }
            }
            return "id";
        });
    }

    @Override
    public String getColumnName(Field field) {
        return COLUMN_NAME_CACHE.computeIfAbsent(field, key -> {
            TableField tableField = key.getAnnotation(TableField.class);
            if (tableField != null && !tableField.value().isEmpty()) {
                return tableField.value();
            }
            TableId tableId = key.getAnnotation(TableId.class);
            if (tableId != null && !tableId.value().isEmpty()) {
                return tableId.value();
            }
            return StrUtil.toUnderlineCase(key.getName());
        });
    }
    // ===== 数据操作方法实现 =====

    @Override
    public List<Model<?>> findByCondition(QueryCondition<Model<?>> condition) {
        BaseMapper<Model<?>> mapper = getMapper(condition.getEntityClass());
        QueryWrapper<Model<?>> wrapper = buildQueryWrapper(condition);
        return mapper.selectList(wrapper);
    }

    @Override
    public int insert(Model<?> entity) {
        BaseMapper<Model<?>> mapper = getMapper((Class<Model<?>>) entity.getClass());
        return mapper.insert(entity);
    }

    @Override
    public int update(Model<?> entity) {
        BaseMapper<Model<?>> mapper = getMapper((Class<Model<?>>) entity.getClass());
        return mapper.updateById(entity);
    }

    @Override
    public int deleteById(Class<Model<?>> entityClass, Serializable id) {
        BaseMapper<Model<?>> mapper = getMapper(entityClass);
        return mapper.deleteById(id);
    }

    /**
     * 获取实体类对应的 Mapper
     *
     * @param entityClass 实体类
     * @return 对应的 BaseMapper
     */
    private BaseMapper<Model<?>> getMapper(Class<Model<?>> entityClass) {
        return (BaseMapper<Model<?>>) MAPPER_CACHE.computeIfAbsent(entityClass, clazz -> {
            // 根据实体类名推断 Mapper 名称
            String mapperBeanName = getMapperBeanName(clazz);
            try {
                return applicationContext.getBean(mapperBeanName, BaseMapper.class);
            } catch (BeansException e) {
                // 尝试通过类型查找
                Map<String, BaseMapper> mappers = applicationContext.getBeansOfType(BaseMapper.class);
                for (BaseMapper<?> mapper : mappers.values()) {
                    // 检查 Mapper 的泛型类型是否匹配
                    if (isMapperForEntity(mapper, clazz)) {
                        return mapper;
                    }
                }
                throw new DriverOperationException("getMapper", getClass(),
                        new IllegalStateException("未找到实体类 " + clazz.getName() + " 对应的 Mapper", e));
            }
        });
    }

    /**
     * 根据实体类名生成 Mapper Bean 名称
     * 例如: User -> userMapper
     *
     * @param entityClass 实体类
     * @return Mapper Bean 名称
     */
    private String getMapperBeanName(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1) + "Mapper";
    }

    /**
     * 检查 Mapper 是否对应指定的实体类
     *
     * @param mapper      Mapper 实例
     * @param entityClass 实体类
     * @return 如果匹配返回 true
     */
    private boolean isMapperForEntity(BaseMapper<?> mapper, Class<?> entityClass) {
        // 通过 Mapper 接口的泛型参数判断
        Class<?>[] interfaces = mapper.getClass().getInterfaces();
        for (Class<?> iface : interfaces) {
            if (BaseMapper.class.isAssignableFrom(iface)) {
                java.lang.reflect.Type[] genericInterfaces = iface.getGenericInterfaces();
                for (java.lang.reflect.Type genericInterface : genericInterfaces) {
                    if (genericInterface instanceof java.lang.reflect.ParameterizedType) {
                        java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) genericInterface;
                        java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0].equals(entityClass)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * 将 QueryCondition 转换为 MyBatis-Plus 的 QueryWrapper
     *
     * @param condition 查询条件
     * @return QueryWrapper 实例
     */
    private QueryWrapper<Model<?>> buildQueryWrapper(QueryCondition<Model<?>> condition) {
        QueryWrapper<Model<?>> wrapper = new QueryWrapper<>();

        if (condition == null || condition.getCriteria().isEmpty()) {
            return wrapper;
        }

        for (Criterion criterion : condition.getCriteria()) {
            String field = criterion.getField();
            Object value = criterion.getValue();
            CriterionType type = criterion.getType();

            switch (type) {
                case EQ:
                    wrapper.eq(field, value);
                    break;
                case IN:
                    if (value instanceof List) {
                        wrapper.in(field, (List<?>) value);
                    }
                    break;
                case GT:
                    wrapper.gt(field, value);
                    break;
                case LT:
                    wrapper.lt(field, value);
                    break;
                case LIKE:
                    wrapper.like(field, value);
                    break;
                case IS_NULL:
                    wrapper.isNull(field);
                    break;
                case IS_NOT_NULL:
                    wrapper.isNotNull(field);
                    break;
                default:
                    throw new QueryConditionException(type.name(), "不支持的条件类型", null);
            }
        }

        return wrapper;
    }

    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}