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
