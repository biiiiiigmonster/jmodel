package com.github.biiiiiigmonster.driver;

import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.config.CoreProperties;
import com.github.biiiiiigmonster.driver.annotation.UseDriver;
import com.github.biiiiiigmonster.driver.exception.DriverNotRegisteredException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
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
     * 已注册的元数据提供者映射表（按Class注册）
     */
    private static final Map<Class<? extends DataDriver<?>>, EntityMetadata> METADATA_MAP = new ConcurrentHashMap<>();

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

        // 自动扫描并注册所有EntityMetadata实现
        Map<String, EntityMetadata> metadataProviders = applicationContext.getBeansOfType(EntityMetadata.class);
        for (EntityMetadata metadata : metadataProviders.values()) {
            Class<? extends DataDriver<?>> driverClass = metadata.getDriverClass();
            if (driverClass != null) {
                METADATA_MAP.put(driverClass, metadata);
            }
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
     * 根据驱动类获取驱动实例
     *
     * @param driverClass 驱动类
     * @param <T>         模型类型
     * @return 对应的数据驱动
     * @throws DriverNotRegisteredException 如果驱动未注册
     */
    public static <T extends Model<?>> DataDriver<T> getDriverByClass(Class<? extends DataDriver<?>> driverClass) {
        DataDriver<?> driver = DRIVER_MAP.get(driverClass);
        if (driver == null) {
            throw new DriverNotRegisteredException(driverClass.getName());
        }
        return (DataDriver<T>) driver;
    }

    /**
     * 获取模型对应的元数据提供者
     *
     * @param modelClass 模型类
     * @return 对应的元数据提供者
     * @throws DriverNotRegisteredException 如果元数据提供者未注册
     */
    public static EntityMetadata getMetadata(Class<?> modelClass) {
        Class<? extends DataDriver<?>> driverClass = getDriverClass(modelClass);
        EntityMetadata metadata = METADATA_MAP.get(driverClass);
        if (metadata == null) {
            throw new DriverNotRegisteredException(driverClass.getName(), modelClass);
        }
        return metadata;
    }

    /**
     * 根据驱动类获取元数据提供者
     *
     * @param driverClass 驱动类
     * @return 对应的元数据提供者
     * @throws DriverNotRegisteredException 如果元数据提供者未注册
     */
    public static EntityMetadata getMetadataByClass(Class<? extends DataDriver<?>> driverClass) {
        EntityMetadata metadata = METADATA_MAP.get(driverClass);
        if (metadata == null) {
            throw new DriverNotRegisteredException(driverClass.getName());
        }
        return metadata;
    }

    /**
     * 获取模型的驱动类
     * 优先从 UseDriver 注解获取，否则使用默认驱动
     *
     * @param modelClass 模型类
     * @return 驱动类
     * @throws DriverNotRegisteredException 如果没有默认驱动且模型未指定驱动
     */
    public static Class<? extends DataDriver<?>> getDriverClass(Class<?> modelClass) {
        return MODEL_DRIVER_MAP.computeIfAbsent(modelClass, clazz -> {
            UseDriver annotation = clazz.getAnnotation(UseDriver.class);
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

    /**
     * 检查元数据提供者是否已注册
     *
     * @param driverClass 驱动类
     * @return 如果已注册返回 true，否则返回 false
     */
    public static boolean isMetadataRegistered(Class<? extends DataDriver<?>> driverClass) {
        return METADATA_MAP.containsKey(driverClass);
    }

    /**
     * 获取所有已注册的驱动类
     *
     * @return 驱动类集合
     */
    public static Set<Class<? extends DataDriver<?>>> getRegisteredDriverClasses() {
        return DRIVER_MAP.keySet();
    }

    /**
     * 获取所有已注册的元数据提供者对应的驱动类
     *
     * @return 驱动类集合
     */
    public static Set<Class<? extends DataDriver<?>>> getRegisteredMetadataClasses() {
        return METADATA_MAP.keySet();
    }

    /**
     * 注销驱动
     *
     * @param driverClass 驱动类
     * @return 如果成功注销返回 true，如果驱动不存在返回 false
     */
    public static boolean unregisterDriver(Class<? extends DataDriver<?>> driverClass) {
        DataDriver<?> removed = DRIVER_MAP.remove(driverClass);
        if (removed != null) {
            // 如果注销的是默认驱动，清除默认驱动设置
            if (driverClass.equals(defaultDriverClass)) {
                defaultDriverClass = null;
            }
            // 清除使用该驱动的模型缓存
            MODEL_DRIVER_MAP.entrySet().removeIf(entry -> driverClass.equals(entry.getValue()));
            return true;
        }
        return false;
    }

    /**
     * 注销元数据提供者
     *
     * @param driverClass 驱动类
     * @return 如果成功注销返回 true，如果提供者不存在返回 false
     */
    public static boolean unregisterMetadata(Class<? extends DataDriver<?>> driverClass) {
        return METADATA_MAP.remove(driverClass) != null;
    }
}
