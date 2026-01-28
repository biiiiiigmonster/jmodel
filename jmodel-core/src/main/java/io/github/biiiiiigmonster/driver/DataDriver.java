package io.github.biiiiiigmonster.driver;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.annotation.PrimaryKey;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据驱动核心接口
 * 定义了所有底层 ORM 框架需要实现的标准操作，包括元数据查询和数据操作
 *
 * @author jmodel-core
 */
public interface DataDriver {
    /**
     * 主键字段缓存
     */
    Map<Class<? extends Model<?>>, String> PRIMARY_KEY_CACHE = new ConcurrentHashMap<>();

    /**
     * 列名缓存
     */
    Map<Field, String> COLUMN_NAME_CACHE = new ConcurrentHashMap<>();

    // ===== 元数据方法 =====

    /**
     * 获取实体类的主键字段名
     *
     * @param entityClass 实体类
     * @return 主键字段名
     */
    default String getPrimaryKey(Class<? extends Model<?>> entityClass) {
        return PRIMARY_KEY_CACHE.computeIfAbsent(entityClass, clazz -> {
            for (Field field : getAllFields(clazz)) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                if (primaryKey != null) {
                    return field.getName();
                }
            }
            return "id";
        });
    }

    /**
     * 获取字段对应的数据库列名
     *
     * @param field 字段
     * @return 数据库列名
     */
    default String getColumnName(Field field) {
        return COLUMN_NAME_CACHE.computeIfAbsent(field, Field::getName);
    }

    // ===== 数据操作方法 =====

    /**
     * 根据主键查找实体
     *
     * @param entityClass 实体类
     * @param id          主键值
     * @return 查找到的实体，如果不存在则返回 null
     */
    default <T extends Model<?>> T findById(Class<T> entityClass, Serializable id) {
        QueryCondition<T> condition = QueryCondition.create(entityClass).eq(getPrimaryKey(entityClass), id);
        List<T> results = findByCondition(condition);
        return CollectionUtils.isEmpty(results) ? null : results.get(0);
    }

    /**
     * 根据条件查询实体列表
     *
     * @param condition 查询条件
     * @return 符合条件的实体列表
     */
    <T extends Model<?>> List<T> findByCondition(QueryCondition<T> condition);

    /**
     * 插入新实体
     *
     * @param entity 要插入的实体
     * @return 插入是否成功
     */
    default int insert(Model<?> entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * 更新现有实体
     *
     * @param entity 要更新的实体
     * @return 更新是否成功
     */
    default int update(Model<?> entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据主键删除实体
     *
     * @param entityClass 实体类
     * @param id          主键值
     * @return 删除是否成功
     */
    default int deleteById(Class<? extends Model<?>> entityClass, Serializable id) {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除实体
     *
     * @param entity 要删除的实体
     * @return 删除是否成功
     */
    @SuppressWarnings("unchecked")
    default int delete(Model<?> entity) {
        return deleteById((Class<? extends Model<?>>) entity.getClass(), (Serializable) entity.primaryKeyValue());
    }

    static Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}