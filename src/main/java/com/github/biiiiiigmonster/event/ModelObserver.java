package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.Model;

/**
 * 模型观察者接口，用于监听模型的生命周期事件
 * 参考Laravel的Model Observer设计
 * 
 * @param <T> 模型类型
 * @author luyunfeng
 */
public interface ModelObserver<T extends Model<?>> {
    
    /**
     * 模型创建前事件
     * 
     * @param model 模型实例
     */
    default void creating(T model) {
        // 默认空实现
    }
    
    /**
     * 模型创建后事件
     * 
     * @param model 模型实例
     */
    default void created(T model) {
        // 默认空实现
    }
    
    /**
     * 模型保存前事件
     * 
     * @param model 模型实例
     */
    default void saving(T model) {
        // 默认空实现
    }
    
    /**
     * 模型保存后事件
     * 
     * @param model 模型实例
     */
    default void saved(T model) {
        // 默认空实现
    }
    
    /**
     * 模型更新前事件
     * 
     * @param model 模型实例
     */
    default void updating(T model) {
        // 默认空实现
    }
    
    /**
     * 模型更新后事件
     * 
     * @param model 模型实例
     */
    default void updated(T model) {
        // 默认空实现
    }
    
    /**
     * 模型删除前事件
     * 
     * @param model 模型实例
     */
    default void deleting(T model) {
        // 默认空实现
    }
    
    /**
     * 模型删除后事件
     * 
     * @param model 模型实例
     */
    default void deleted(T model) {
        // 默认空实现
    }
    
    /**
     * 模型检索后事件
     * 
     * @param model 模型实例
     */
    default void retrieved(T model) {
        // 默认空实现
    }
    
    /**
     * 模型恢复前事件（软删除恢复）
     * 
     * @param model 模型实例
     */
    default void restoring(T model) {
        // 默认空实现
    }
    
    /**
     * 模型恢复后事件（软删除恢复）
     * 
     * @param model 模型实例
     */
    default void restored(T model) {
        // 默认空实现
    }
    
    /**
     * 模型强制删除前事件
     * 
     * @param model 模型实例
     */
    default void forceDeleting(T model) {
        // 默认空实现
    }
    
    /**
     * 模型强制删除后事件
     * 
     * @param model 模型实例
     */
    default void forceDeleted(T model) {
        // 默认空实现
    }
}