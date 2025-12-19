package com.github.biiiiiigmonster.driver.mybatisplus;

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
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus 数据驱动实现
 * 将 DataDriver 接口的操作委托给 MyBatis-Plus 的 BaseMapper
 * 
 * @author jmodel
 */
@Component
public class MyBatisPlusDriver implements DataDriver<Model<?>>, ApplicationContextAware {

    /**
     * 驱动名称常量
     */
    public static final String DRIVER_NAME = "mybatis-plus";

    /**
     * Spring 应用上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * Mapper 缓存，避免重复查找
     */
    private static final Map<Class<?>, BaseMapper<?>> MAPPER_CACHE = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Model<?> findById(Class<Model<?>> entityClass, Serializable id) {
        try {
            BaseMapper<Model<?>> mapper = getMapper(entityClass);
            return mapper.selectById(id);
        } catch (DriverOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new DriverOperationException("findById", DRIVER_NAME, e);
        }
    }

    @Override
    public List<Model<?>> findByCondition(Class<Model<?>> entityClass, QueryCondition condition) {
        try {
            BaseMapper<Model<?>> mapper = getMapper(entityClass);
            QueryWrapper<Model<?>> wrapper = buildQueryWrapper(condition);
            return mapper.selectList(wrapper);
        } catch (DriverOperationException | QueryConditionException e) {
            throw e;
        } catch (Exception e) {
            throw new DriverOperationException("findByCondition", DRIVER_NAME, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int insert(Model<?> entity) {
        try {
            BaseMapper<Model<?>> mapper = getMapper((Class<Model<?>>) entity.getClass());
            return mapper.insert(entity);
        } catch (DriverOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new DriverOperationException("insert", DRIVER_NAME, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int update(Model<?> entity) {
        try {
            BaseMapper<Model<?>> mapper = getMapper((Class<Model<?>>) entity.getClass());
            return mapper.updateById(entity);
        } catch (DriverOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new DriverOperationException("update", DRIVER_NAME, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean deleteById(Class<Model<?>> entityClass, Serializable id) {
        try {
            BaseMapper<Model<?>> mapper = getMapper(entityClass);
            return mapper.deleteById(id) > 0;
        } catch (DriverOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new DriverOperationException("deleteById", DRIVER_NAME, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean delete(Model<?> entity) {
        try {
            BaseMapper<Model<?>> mapper = getMapper((Class<Model<?>>) entity.getClass());
            Serializable id = (Serializable) entity.primaryKeyValue();
            return mapper.deleteById(id) > 0;
        } catch (DriverOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new DriverOperationException("delete", DRIVER_NAME, e);
        }
    }

    @Override
    public String getDriverName() {
        return DRIVER_NAME;
    }


    /**
     * 获取实体类对应的 Mapper
     *
     * @param entityClass 实体类
     * @return 对应的 BaseMapper
     */
    @SuppressWarnings("unchecked")
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
                throw new DriverOperationException("getMapper", DRIVER_NAME,
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
     * @param mapper Mapper 实例
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
    @SuppressWarnings("unchecked")
    private QueryWrapper<Model<?>> buildQueryWrapper(QueryCondition condition) {
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

    /**
     * 清除 Mapper 缓存（主要用于测试）
     */
    public static void clearMapperCache() {
        MAPPER_CACHE.clear();
    }
}