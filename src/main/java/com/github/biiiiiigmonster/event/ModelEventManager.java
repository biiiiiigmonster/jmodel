package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型事件管理器，负责管理模型观察者和事件触发
 * 参考Laravel的Model Event Manager设计
 * 
 * @author luyunfeng
 */
@Slf4j
@Component
public class ModelEventManager {
    
    @Autowired
    private EventDispatcher eventDispatcher;
    
    /**
     * 模型观察者映射表
     */
    private final Map<Class<?>, List<ModelObserver<?>>> observers = new ConcurrentHashMap<>();
    
    /**
     * 静音的模型类型
     */
    private final Set<Class<?>> mutedModels = ConcurrentHashMap.newKeySet();
    
    /**
     * 注册模型观察者
     * 
     * @param modelClass 模型类型
     * @param observer 观察者
     */
    public <T extends Model<?>> void observe(Class<T> modelClass, ModelObserver<T> observer) {
        observers.computeIfAbsent(modelClass, k -> new ArrayList<>()).add(observer);
        log.debug("Registered observer for model: {}", modelClass.getSimpleName());
    }
    
    /**
     * 移除模型观察者
     * 
     * @param modelClass 模型类型
     * @param observer 观察者
     */
    public <T extends Model<?>> void forgetObserver(Class<T> modelClass, ModelObserver<T> observer) {
        List<ModelObserver<?>> modelObservers = observers.get(modelClass);
        if (modelObservers != null) {
            modelObservers.remove(observer);
            log.debug("Removed observer for model: {}", modelClass.getSimpleName());
        }
    }
    
    /**
     * 移除模型的所有观察者
     * 
     * @param modelClass 模型类型
     */
    public void forgetObservers(Class<?> modelClass) {
        observers.remove(modelClass);
        log.debug("Removed all observers for model: {}", modelClass.getSimpleName());
    }
    
    /**
     * 静音指定模型的事件
     * 
     * @param modelClass 模型类型
     */
    public void mute(Class<?> modelClass) {
        mutedModels.add(modelClass);
        log.debug("Muted events for model: {}", modelClass.getSimpleName());
    }
    
    /**
     * 取消静音指定模型的事件
     * 
     * @param modelClass 模型类型
     */
    public void unmute(Class<?> modelClass) {
        mutedModels.remove(modelClass);
        log.debug("Unmuted events for model: {}", modelClass.getSimpleName());
    }
    
    /**
     * 检查模型是否被静音
     * 
     * @param modelClass 模型类型
     * @return 是否被静音
     */
    public boolean isModelMuted(Class<?> modelClass) {
        return mutedModels.contains(modelClass);
    }
    
    /**
     * 清除所有静音的模型
     */
    public void clearMutedModels() {
        mutedModels.clear();
        log.debug("Cleared all muted models");
    }
    
    /**
     * 触发模型创建前事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireCreating(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelCreatingEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).creating(model);
                } catch (Exception e) {
                    log.error("Error in creating observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型创建后事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireCreated(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelCreatedEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).created(model);
                } catch (Exception e) {
                    log.error("Error in created observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型保存前事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireSaving(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelSavingEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).saving(model);
                } catch (Exception e) {
                    log.error("Error in saving observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型保存后事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireSaved(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelSavedEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).saved(model);
                } catch (Exception e) {
                    log.error("Error in saved observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型更新前事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireUpdating(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelUpdatingEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).updating(model);
                } catch (Exception e) {
                    log.error("Error in updating observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型更新后事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireUpdated(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelUpdatedEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).updated(model);
                } catch (Exception e) {
                    log.error("Error in updated observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型删除前事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireDeleting(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelDeletingEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).deleting(model);
                } catch (Exception e) {
                    log.error("Error in deleting observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型删除后事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireDeleted(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelDeletedEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).deleted(model);
                } catch (Exception e) {
                    log.error("Error in deleted observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 触发模型检索后事件
     * 
     * @param model 模型实例
     */
    @SuppressWarnings("unchecked")
    public <T extends Model<?>> void fireRetrieved(T model) {
        if (isModelMuted(model.getClass())) {
            return;
        }
        
        // 触发Spring事件
        eventDispatcher.dispatch(new ModelRetrievedEvent<>(this, model));
        
        // 调用观察者
        List<ModelObserver<?>> modelObservers = observers.get(model.getClass());
        if (modelObservers != null) {
            for (ModelObserver<?> observer : modelObservers) {
                try {
                    ((ModelObserver<T>) observer).retrieved(model);
                } catch (Exception e) {
                    log.error("Error in retrieved observer for model: {}", model.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 获取指定模型的观察者数量
     * 
     * @param modelClass 模型类型
     * @return 观察者数量
     */
    public int getObserverCount(Class<?> modelClass) {
        List<ModelObserver<?>> modelObservers = observers.get(modelClass);
        return modelObservers != null ? modelObservers.size() : 0;
    }
}