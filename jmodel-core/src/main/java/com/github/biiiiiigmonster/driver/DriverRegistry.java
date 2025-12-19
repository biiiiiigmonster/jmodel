package com.github.biiiiiigmonster.driver;

import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.config.CoreProperties;
import com.github.biiiiiigmonster.driver.annotation.ModelDriver;
import com.github.biiiiiigmonster.driver.exception.DriverNotRegisteredException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 驱动注册和管理器
 * 负责管理数据驱动的注册、查找和默认驱动设置
 *
 * @author jmodel-core
 */
@Component
@SuppressWarnings("unchecked")
public class DriverRegistry {

    /**
     * 已注册的驱动映射表（按Class注册）
     */
    private static final Map<Class<? extends DataDriver<?>>, DataDriver<?>> DRIVER_MAP = new ConcurrentHashMap<>();

    /**
     * 模型类到驱动类的映射缓存
     */
    private static final Map<Class<?>, Class<? extends DataDriver<?>>> MODEL_DRIVER_MAP = new ConcurrentHashMap<>();

    /**
     * 默认驱动类
     */
    private static volatile Class<? extends DataDriver<?>> defaultDriverClass;

    @Resource
    private CoreProperties coreProperties;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 初始化默认驱动配置
     */
    @PostConstruct
    public void init() {
        if (coreProperties.getDriver() != null
                && coreProperties.getDriver().getDefaultDriver() != null) {
            defaultDriverClass = coreProperties.getDriver().getDefaultDriver();
        }

        // 自动扫描并注册所有DataDriver实现
        Map<String, DataDriver> drivers = applicationContext.getBeansOfType(DataDriver.class);
        for (DataDriver<?> driver : drivers.values()) {
            Class<? extends DataDriver<?>> driverClass = (Class<? extends DataDriver<?>>) driver.getClass();
            DRIVER_MAP.put(driverClass, driver);
        }
    }

    /**
     * 获取模型对应的驱动
     *
     * @param modelClass 模型类
     * @param <T>        模型类型
     * @return 对应的数据驱动
     * @throws DriverNotRegisteredException 如果驱动未注册
     */
    public static <T extends Model<?>> DataDriver<T> getDriver(Class<T> modelClass) {
        Class<? extends DataDriver<?>> driverClass = getDriverClass(modelClass);
        DataDriver<?> driver = DRIVER_MAP.get(driverClass);
        if (driver == null) {
            throw new DriverNotRegisteredException(driverClass.getName(), modelClass);
        }
        return (DataDriver<T>) driver;
    }

    /**
     * 获取模型的驱动类
     * 优先从 ModelDriver 注解获取，否则使用默认驱动
     *
     * @param modelClass 模型类
     * @return 驱动类
     * @throws DriverNotRegisteredException 如果没有默认驱动且模型未指定驱动
     */
    public static Class<? extends DataDriver<?>> getDriverClass(Class<?> modelClass) {
        return MODEL_DRIVER_MAP.computeIfAbsent(modelClass, clazz -> {
            ModelDriver annotation = clazz.getAnnotation(ModelDriver.class);
            if (annotation != null) {
                return annotation.value();
            }
            if (defaultDriverClass == null) {
                throw new DriverNotRegisteredException("default", modelClass);
            }
            return defaultDriverClass;
        });
    }

    /**
     * 检查驱动是否已注册
     *
     * @param driverClass 驱动类
     * @return 如果已注册返回 true，否则返回 false
     */
    public static boolean isDriverRegistered(Class<? extends DataDriver<?>> driverClass) {
        return DRIVER_MAP.containsKey(driverClass);
    }
}
