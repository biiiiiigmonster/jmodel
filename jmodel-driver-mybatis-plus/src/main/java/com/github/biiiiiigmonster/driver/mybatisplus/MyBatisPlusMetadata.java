package com.github.biiiiiigmonster.driver.mybatisplus;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.github.biiiiiigmonster.driver.EntityMetadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus 实体元数据实现
 * 通过 MyBatis-Plus 注解获取实体的元数据信息
 * 
 * @author jmodel
 */
@Component
public class MyBatisPlusMetadata implements EntityMetadata {
    
    public static final String PROVIDER_NAME = "mybatis-plus";
    
    private static final Map<Class<?>, String> PRIMARY_KEY_CACHE = new ConcurrentHashMap<>();
    
    private static final Map<String, String> COLUMN_NAME_CACHE = new ConcurrentHashMap<>();
    
    @Override
    public String getPrimaryKey(Class<?> entityClass) {
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
    public String getColumnName(Class<?> entityClass, String fieldName) {
        String cacheKey = entityClass.getName() + "." + fieldName;
        return COLUMN_NAME_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Field field = getField(entityClass, fieldName);
                if (field != null) {
                    TableField tableField = field.getAnnotation(TableField.class);
                    if (tableField != null && !tableField.value().isEmpty()) {
                        return tableField.value();
                    }
                    TableId tableId = field.getAnnotation(TableId.class);
                    if (tableId != null && !tableId.value().isEmpty()) {
                        return tableId.value();
                    }
                }
                return StrUtil.toUnderlineCase(fieldName);
            } catch (Exception e) {
                return StrUtil.toUnderlineCase(fieldName);
            }
        });
    }
    
    @Override
    public String getForeignKey(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        String primaryKey = getPrimaryKey(entityClass);
        return StrUtil.lowerFirst(simpleName) + StrUtil.upperFirst(primaryKey);
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
    
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
    
    public static void clearCache() {
        PRIMARY_KEY_CACHE.clear();
        COLUMN_NAME_CACHE.clear();
    }
}
