package com.github.biiiiiigmonster.driver;

import com.github.biiiiiigmonster.Model;

import java.io.Serializable;
import java.util.List;

/**
 * 数据驱动核心接口
 * 定义了所有底层 ORM 框架需要实现的标准操作，包括元数据查询和数据操作
 *
 * @param <T> 继承自 Model 的实体类型
 * @author jmodel-core
 */
public interface DataDriver<T extends Model<?>> {

    // ===== 元数据方法 =====

    /**
     * 获取实体类的主键字段名
     *
     * @param entityClass 实体类
     * @return 主键字段名
     */
    String getPrimaryKey(Class<?> entityClass);

    /**
     * 获取字段对应的数据库列名
     *
     * @param entityClass 实体类
     * @param fieldName   字段名
     * @return 数据库列名
     */
    String getColumnName(Class<?> entityClass, String fieldName);

    // ===== 数据操作方法 =====

    /**
     * 根据主键查找实体
     *
     * @param entityClass 实体类
     * @param id          主键值
     * @return 查找到的实体，如果不存在则返回 null
     */
    T findById(Class<T> entityClass, Serializable id);

    /**
     * 根据条件查询实体列表
     *
     * @param entityClass 实体类
     * @param condition   查询条件
     * @return 符合条件的实体列表
     */
    List<T> findByCondition(Class<T> entityClass, QueryCondition condition);

    /**
     * 插入新实体
     *
     * @param entity 要插入的实体
     * @return 插入是否成功
     */
    int insert(T entity);

    /**
     * 更新现有实体
     *
     * @param entity 要更新的实体
     * @return 更新是否成功
     */
    int update(T entity);

    /**
     * 根据主键删除实体
     *
     * @param entityClass 实体类
     * @param id          主键值
     * @return 删除是否成功
     */
    int deleteById(Class<T> entityClass, Serializable id);

    /**
     * 删除实体
     *
     * @param entity 要删除的实体
     * @return 删除是否成功
     */
    int delete(T entity);
}