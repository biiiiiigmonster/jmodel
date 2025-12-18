package com.github.biiiiiigmonster.driver;

import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.driver.annotation.UseDriver;
import com.github.biiiiiigmonster.driver.exception.DriverNotRegisteredException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 驱动注册和管理器
 * 负责管理数据驱动的注册、查找和默认驱动设置
 * 
 * @author jmodel-core
 */
public class DriverRegistry {
    
    /**
     * 已注册的驱动映射表
     */
    private static final Map<String, DataDriver<?>> DRIVER_MAP = new ConcurrentHashMap<>();
    
    /**
     * 已注册的元数据提供者映射表
     */
    private static final Map<String, EntityMetadata> METADATA_MAP = new ConcurrentHashMap<>();
    
    /**
     * 模型类到驱动名称的映射缓存
     */
    private static final Map<Class<?>, String> MODEL_DRIVER_MAP = new ConcurrentHashMap<>();
    
    /**
     * 默认驱动名称
     */
    private static volatile String defaultDriverName;
    
    /**
     * 私有构造函数，防止实例化
     */
    private DriverRegistry() {
    }

    
    /**
     * 注册数据驱动
     * 如果是第一个注册的驱动，将自动设置为默认驱动
     * 
     * @param driverName 驱动名称
     * @param driver 驱动实例
     */
    public static void registerDriver(String driverName, DataDriver<?> driver) {
        if (driverName == null || driverName.trim().isEmpty()) {
            throw new IllegalArgumentException("驱动名称不能为空");
        }
        if (driver == null) {
            throw new IllegalArgumentException("驱动实例不能为空");
        }
        DRIVER_MAP.put(driverName, driver);
        if (defaultDriverName == null) {
            defaultDriverName = driverName;
        }
    }
    
    /**
     * 注册实体元数据提供者
     * 
     * @param providerName 提供者名称
     * @param metadata 元数据提供者实例
     */
    public static void registerMetadata(String providerName, EntityMetadata metadata) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("元数据提供者名称不能为空");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("元数据提供者实例不能为空");
        }
        METADATA_MAP.put(providerName, metadata);
    }
    
    /**
     * 获取模型对应的驱动
     * 
     * @param modelClass 模型类
     * @param <T> 模型类型
     * @return 对应的数据驱动
     * @throws DriverNotRegisteredException 如果驱动未注册
     */
    @SuppressWarnings("unchecked")
    public static <T extends Model<?>> DataDriver<T> getDriver(Class<T> modelClass) {
        String driverName = getDriverName(modelClass);
        DataDriver<?> driver = DRIVER_MAP.get(driverName);
        if (driver == null) {
            throw new DriverNotRegisteredException(driverName, modelClass);
        }
        return (DataDriver<T>) driver;
    }
    
    /**
     * 根据驱动名称获取驱动实例
     * 
     * @param driverName 驱动名称
     * @param <T> 模型类型
     * @return 对应的数据驱动
     * @throws DriverNotRegisteredException 如果驱动未注册
     */
    @SuppressWarnings("unchecked")
    public static <T extends Model<?>> DataDriver<T> getDriverByName(String driverName) {
        DataDriver<?> driver = DRIVER_MAP.get(driverName);
        if (driver == null) {
            throw new DriverNotRegisteredException(driverName);
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
        String driverName = getDriverName(modelClass);
        EntityMetadata metadata = METADATA_MAP.get(driverName);
        if (metadata == null) {
            throw new DriverNotRegisteredException(driverName, modelClass);
        }
        return metadata;
    }
    
    /**
     * 根据提供者名称获取元数据提供者
     * 
     * @param providerName 提供者名称
     * @return 对应的元数据提供者
     * @throws DriverNotRegisteredException 如果元数据提供者未注册
     */
    public static EntityMetadata getMetadataByName(String providerName) {
        EntityMetadata metadata = METADATA_MAP.get(providerName);
        if (metadata == null) {
            throw new DriverNotRegisteredException(providerName);
        }
        return metadata;
    }
    
    /**
     * 获取模型的驱动名称
     * 优先从 UseDriver 注解获取，否则使用默认驱动
     * 
     * @param modelClass 模型类
     * @return 驱动名称
     * @throws IllegalStateException 如果没有默认驱动且模型未指定驱动
     */
    public static String getDriverName(Class<?> modelClass) {
        return MODEL_DRIVER_MAP.computeIfAbsent(modelClass, clazz -> {
            UseDriver annotation = clazz.getAnnotation(UseDriver.class);
            if (annotation != null) {
                return annotation.value();
            }
            if (defaultDriverName == null) {
                throw new IllegalStateException("未设置默认驱动，且模型类 " + modelClass.getName() + " 未指定驱动");
            }
            return defaultDriverName;
        });
    }
    
    /**
     * 设置默认驱动
     * 
     * @param driverName 驱动名称
     * @throws IllegalArgumentException 如果驱动未注册
     */
    public static void setDefaultDriver(String driverName) {
        if (driverName == null || driverName.trim().isEmpty()) {
            throw new IllegalArgumentException("驱动名称不能为空");
        }
        if (!DRIVER_MAP.containsKey(driverName)) {
            throw new IllegalArgumentException("驱动未注册: " + driverName);
        }
        defaultDriverName = driverName;
    }

    
    /**
     * 获取默认驱动名称
     * 
     * @return 默认驱动名称，如果未设置则返回 null
     */
    public static String getDefaultDriverName() {
        return defaultDriverName;
    }
    
    /**
     * 检查驱动是否已注册
     * 
     * @param driverName 驱动名称
     * @return 如果已注册返回 true，否则返回 false
     */
    public static boolean isDriverRegistered(String driverName) {
        return DRIVER_MAP.containsKey(driverName);
    }
    
    /**
     * 检查元数据提供者是否已注册
     * 
     * @param providerName 提供者名称
     * @return 如果已注册返回 true，否则返回 false
     */
    public static boolean isMetadataRegistered(String providerName) {
        return METADATA_MAP.containsKey(providerName);
    }
    
    /**
     * 获取所有已注册的驱动名称
     * 
     * @return 驱动名称集合
     */
    public static Set<String> getRegisteredDriverNames() {
        return DRIVER_MAP.keySet();
    }
    
    /**
     * 获取所有已注册的元数据提供者名称
     * 
     * @return 元数据提供者名称集合
     */
    public static Set<String> getRegisteredMetadataNames() {
        return METADATA_MAP.keySet();
    }
    
    /**
     * 注销驱动
     * 
     * @param driverName 驱动名称
     * @return 如果成功注销返回 true，如果驱动不存在返回 false
     */
    public static boolean unregisterDriver(String driverName) {
        DataDriver<?> removed = DRIVER_MAP.remove(driverName);
        if (removed != null) {
            // 如果注销的是默认驱动，清除默认驱动设置
            if (driverName.equals(defaultDriverName)) {
                defaultDriverName = null;
            }
            // 清除使用该驱动的模型缓存
            MODEL_DRIVER_MAP.entrySet().removeIf(entry -> driverName.equals(entry.getValue()));
            return true;
        }
        return false;
    }
    
    /**
     * 注销元数据提供者
     * 
     * @param providerName 提供者名称
     * @return 如果成功注销返回 true，如果提供者不存在返回 false
     */
    public static boolean unregisterMetadata(String providerName) {
        return METADATA_MAP.remove(providerName) != null;
    }
    
    /**
     * 清除所有注册信息（主要用于测试）
     */
    public static void clear() {
        DRIVER_MAP.clear();
        METADATA_MAP.clear();
        MODEL_DRIVER_MAP.clear();
        defaultDriverName = null;
    }
    
    /**
     * 清除模型驱动映射缓存（主要用于测试）
     */
    public static void clearModelDriverCache() {
        MODEL_DRIVER_MAP.clear();
    }
}
