package com.github.biiiiiigmonster.driver;

import com.github.biiiiiigmonster.Model;

import java.io.Serializable;
import java.util.List;

/**
 * 数据驱动核心接口
 * 定义了所有底层 ORM 框架需要实现的标准操作
 * 
 * @param <T> 继承自 Model 的实体类型
 * @author jmodel-core
 */
public interface DataDriver<T extends Model<?>> {
    
    /**
     * 根据主键查找实体
     * 
     * @param entityClass 实体类
     * @param id 主键值
     * @return 查找到的实体，如果不存在则返回 null
     */
    T findById(Class<T> entityClass, Serializable id);
    
    /**
     * 根据条件查询实体列表
     * 
     * @param entityClass 实体类
     * @param condition 查询条件
     * @return 符合条件的实体列表
     */
    List<T> findByCondition(Class<T> entityClass, QueryCondition condition);
    
    /**
     * 插入新实体
     * 
     * @param entity 要插入的实体
     * @return 插入是否成功
     */
    boolean insert(T entity);
    
    /**
     * 更新现有实体
     * 
     * @param entity 要更新的实体
     * @return 更新是否成功
     */
    boolean update(T entity);
    
    /**
     * 根据主键删除实体
     * 
     * @param entityClass 实体类
     * @param id 主键值
     * @return 删除是否成功
     */
    boolean deleteById(Class<T> entityClass, Serializable id);
    
    /**
     * 删除实体
     * 
     * @param entity 要删除的实体
     * @return 删除是否成功
     */
    boolean delete(T entity);
    
    /**
     * 获取驱动标识符
     * 
     * @return 驱动名称
     */
    String getDriverName();
}