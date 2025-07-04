package com.github.biiiiiigmonster.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 事件调度器，负责事件的注册、触发和管理
 * 参考Laravel的事件系统设计
 * 
 * @author luyunfeng
 */
@Slf4j
@Component
public class EventDispatcher {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * 事件监听器映射表
     */
    private final Map<Class<?>, List<EventListener>> listeners = new ConcurrentHashMap<>();
    
    /**
     * 全局监听器
     */
    private final List<EventListener> globalListeners = new ArrayList<>();
    
    /**
     * 静音的事件类型
     */
    private final Set<Class<?>> mutedEvents = ConcurrentHashMap.newKeySet();
    
    /**
     * 注册事件监听器
     * 
     * @param eventClass 事件类型
     * @param listener 监听器
     */
    public <T> void listen(Class<T> eventClass, EventListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
        log.debug("Registered listener for event: {}", eventClass.getSimpleName());
    }
    
    /**
     * 注册全局监听器
     * 
     * @param listener 监听器
     */
    public void listenGlobal(EventListener<Object> listener) {
        globalListeners.add(listener);
        log.debug("Registered global listener");
    }
    
    /**
     * 触发事件
     * 
     * @param event 事件对象
     */
    public <T> void dispatch(T event) {
        if (isEventMuted(event.getClass())) {
            log.debug("Event {} is muted, skipping dispatch", event.getClass().getSimpleName());
            return;
        }
        
        // 发布Spring事件
        eventPublisher.publishEvent(event);
        
        // 调用注册的监听器
        callListeners(event);
        
        log.debug("Dispatched event: {}", event.getClass().getSimpleName());
    }
    
    /**
     * 触发事件并等待所有监听器完成
     * 
     * @param event 事件对象
     */
    public <T> void dispatchAndWait(T event) {
        if (isEventMuted(event.getClass())) {
            log.debug("Event {} is muted, skipping dispatch", event.getClass().getSimpleName());
            return;
        }
        
        // 发布Spring事件
        eventPublisher.publishEvent(event);
        
        // 调用注册的监听器并等待完成
        callListenersAndWait(event);
        
        log.debug("Dispatched and waited for event: {}", event.getClass().getSimpleName());
    }
    
    /**
     * 静音指定类型的事件
     * 
     * @param eventClass 事件类型
     */
    public void mute(Class<?> eventClass) {
        mutedEvents.add(eventClass);
        log.debug("Muted event: {}", eventClass.getSimpleName());
    }
    
    /**
     * 取消静音指定类型的事件
     * 
     * @param eventClass 事件类型
     */
    public void unmute(Class<?> eventClass) {
        mutedEvents.remove(eventClass);
        log.debug("Unmuted event: {}", eventClass.getSimpleName());
    }
    
    /**
     * 检查事件是否被静音
     * 
     * @param eventClass 事件类型
     * @return 是否被静音
     */
    public boolean isEventMuted(Class<?> eventClass) {
        return mutedEvents.contains(eventClass);
    }
    
    /**
     * 清除所有静音的事件
     */
    public void clearMutedEvents() {
        mutedEvents.clear();
        log.debug("Cleared all muted events");
    }
    
    /**
     * 移除指定事件的监听器
     * 
     * @param eventClass 事件类型
     * @param listener 监听器
     */
    public <T> void forget(Class<T> eventClass, EventListener<T> listener) {
        List<EventListener> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            log.debug("Removed listener for event: {}", eventClass.getSimpleName());
        }
    }
    
    /**
     * 移除指定事件的所有监听器
     * 
     * @param eventClass 事件类型
     */
    public void forget(Class<?> eventClass) {
        listeners.remove(eventClass);
        log.debug("Removed all listeners for event: {}", eventClass.getSimpleName());
    }
    
    /**
     * 获取指定事件的监听器数量
     * 
     * @param eventClass 事件类型
     * @return 监听器数量
     */
    public int getListenerCount(Class<?> eventClass) {
        List<EventListener> eventListeners = listeners.get(eventClass);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * 调用监听器
     * 
     * @param event 事件对象
     */
    @SuppressWarnings("unchecked")
    private <T> void callListeners(T event) {
        // 调用全局监听器
        for (EventListener listener : globalListeners) {
            try {
                listener.handle(event);
            } catch (Exception e) {
                log.error("Error in global listener for event: {}", event.getClass().getSimpleName(), e);
            }
        }
        
        // 调用特定事件的监听器
        List<EventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    listener.handle(event);
                } catch (Exception e) {
                    log.error("Error in listener for event: {}", event.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 调用监听器并等待完成
     * 
     * @param event 事件对象
     */
    @SuppressWarnings("unchecked")
    private <T> void callListenersAndWait(T event) {
        // 调用全局监听器
        for (EventListener listener : globalListeners) {
            try {
                listener.handle(event);
            } catch (Exception e) {
                log.error("Error in global listener for event: {}", event.getClass().getSimpleName(), e);
            }
        }
        
        // 调用特定事件的监听器
        List<EventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    listener.handle(event);
                } catch (Exception e) {
                    log.error("Error in listener for event: {}", event.getClass().getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 事件监听器接口
     * 
     * @param <T> 事件类型
     */
    @FunctionalInterface
    public interface EventListener<T> {
        /**
         * 处理事件
         * 
         * @param event 事件对象
         */
        void handle(T event);
    }
}