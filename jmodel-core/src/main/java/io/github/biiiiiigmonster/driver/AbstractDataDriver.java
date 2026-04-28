package io.github.biiiiiigmonster.driver;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.annotation.PrimaryKey;
import io.github.biiiiiigmonster.config.CoreProperties;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDataDriver implements DataDriver {
    @Resource
    private CoreProperties coreProperties;

    /**
     * 主键字段缓存
     */
    Map<Class<? extends Model<?>>, Field> PRIMARY_KEY_CACHE = new ConcurrentHashMap<>();

    /**
     * 列名缓存
     */
    Map<Field, String> COLUMN_NAME_CACHE = new ConcurrentHashMap<>();

    public Field getPrimaryField(Class<? extends Model<?>> entityClass) {
        return PRIMARY_KEY_CACHE.computeIfAbsent(entityClass, this::primaryField);
    }

    protected Field primaryField(Class<? extends Model<?>> entityClass) {
        for (Field field : getAllFields(entityClass)) {
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                return field;
            }
        }
        return ReflectUtil.getField(entityClass, coreProperties.getModel().getDefaultPrimaryKey());
    }

    public String getColumnName(Field field) {
        return COLUMN_NAME_CACHE.computeIfAbsent(field, this::columnName);
    }

    protected String columnName(Field field) {
        return StrUtil.toUnderlineCase(field.getName());
    }

    public <T extends Model<?>> T findById(Class<T> entityClass, Serializable id) {
        QueryCondition<T> condition = QueryCondition.create(entityClass).eq(getPrimaryField(entityClass), id);
        List<T> results = findByCondition(condition);
        return CollectionUtils.isEmpty(results) ? null : results.get(0);
    }

    public int insert(Model<?> entity) {
        throw new UnsupportedOperationException();
    }

    public int update(Model<?> entity) {
        throw new UnsupportedOperationException();
    }

    public int deleteById(Class<? extends Model<?>> entityClass, Serializable id) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public int delete(Model<?> entity) {
        return deleteById((Class<? extends Model<?>>) entity.getClass(), (Serializable) entity.primaryKeyValue());
    }

    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}
