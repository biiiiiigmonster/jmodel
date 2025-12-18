package com.github.biiiiiigmonster.router.driver.mybatisplus;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.github.biiiiiigmonster.driver.EntityMetadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus 实体元数据实现
 * 通过 MyBatis-Plus 注解获取实体的元数据信息
 * 
 * @author jmodel-core
 */
@Component
public class MyBatisPlusMetadata implements EntityMetadata {
    
    /**
     * 提供者名称常量
     */
    public static final String PROVIDER_NAME = "mybatis-plus";
    
    /**
     * 主键字段缓存
     */
    private static final Map<Class<?>, String> PRIMARY_KEY_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 列名缓存
     */
    private static final Map<String, String> COLUMN_NAME_CACHE = new ConcurrentHashMap<>();
    
    @Override
    public String getPrimaryKey(Class<?> entityClass) {
        return PRIMARY_KEY_CACHE.computeIfAbsent(entityClass, clazz -> {
            // 查找带有 @TableId 注解的字段
            for (Field field : getAllFields(clazz)) {
                TableId tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    return field.getName();
                }
            }
            // 默认返回 "id"
            return "id";
        });
    }

    
    @Override
    public String getColumnName(Class<?> entityClass, String fieldName) {
        String cacheKey = entityClass.getName() + "." + fieldName;
        return COLUMN_NAME_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Field field = getField(entityClass, fieldName);
                if (field != null) {
                    // 检查 @TableField 注解
                    TableField tableField = field.getAnnotation(TableField.class);
                    if (tableField != null && !tableField.value().isEmpty()) {
                        return tableField.value();
                    }
                    // 检查 @TableId 注解
                    TableId tableId = field.getAnnotation(TableId.class);
                    if (tableId != null && !tableId.value().isEmpty()) {
                        return tableId.value();
                    }
                }
                // 默认使用驼峰转下划线
                return StrUtil.toUnderlineCase(fieldName);
            } catch (Exception e) {
                return StrUtil.toUnderlineCase(fieldName);
            }
        });
    }
    
    @Override
    public String getForeignKey(Class<?> entityClass) {
        // 使用约定：类名首字母小写 + 主键字段名首字母大写
        // 例如: User -> userId
        String simpleName = entityClass.getSimpleName();
        String primaryKey = getPrimaryKey(entityClass);
        return StrUtil.lowerFirst(simpleName) + StrUtil.upperFirst(primaryKey);
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    
    /**
     * 获取类的所有字段（包括父类）
     * 
     * @param clazz 类
     * @return 字段数组
     */
    private Field[] getAllFields(Class<?> clazz) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
    
    /**
     * 获取指定名称的字段（包括父类）
     * 
     * @param clazz 类
     * @param fieldName 字段名
     * @return 字段，如果不存在返回 null
     */
    private Field getField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * 清除缓存（主要用于测试）
     */
    public static void clearCache() {
        PRIMARY_KEY_CACHE.clear();
        COLUMN_NAME_CACHE.clear();
    }
}
