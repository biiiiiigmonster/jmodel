package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 事件监听器注册器，用于自动注册Spring容器中的事件监听器
 * 参考Laravel的事件监听器自动注册机制
 * 
 * @author luyunfeng
 */
@Slf4j
@Component
public class EventListenerRegistrar implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Autowired
    private EventDispatcher eventDispatcher;
    
    @Autowired
    private ModelEventManager modelEventManager;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct
    public void registerEventListeners() {
        log.info("Starting to register event listeners...");
        
        // 注册Spring事件监听器
        registerSpringEventListeners();
        
        // 注册模型观察者
        registerModelObservers();
        
        log.info("Event listeners registration completed");
    }
    
    /**
     * 注册Spring事件监听器
     */
    private void registerSpringEventListeners() {
        Map<String, Object> listeners = applicationContext.getBeansOfType(Object.class);
        
        for (Map.Entry<String, Object> entry : listeners.entrySet()) {
            Object bean = entry.getValue();
            String beanName = entry.getKey();
            
            // 检查是否有@EventListener注解的方法
            registerEventListenerMethods(bean, beanName);
        }
    }
    
    /**
     * 注册事件监听器方法
     * 
     * @param bean Bean实例
     * @param beanName Bean名称
     */
    private void registerEventListenerMethods(Object bean, String beanName) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        
        for (Method method : methods) {
            com.github.biiiiiigmonster.event.EventListener annotation = method.getAnnotation(com.github.biiiiiigmonster.event.EventListener.class);
            if (annotation != null) {
                registerEventListenerMethod(bean, method, annotation);
                log.debug("Registered event listener method: {}.{}", beanName, method.getName());
            }
        }
    }
    
    /**
     * 注册单个事件监听器方法
     * 
     * @param bean Bean实例
     * @param method 方法
     * @param annotation 注解
     */
    private void registerEventListenerMethod(Object bean, Method method, com.github.biiiiiigmonster.event.EventListener annotation) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            log.warn("Event listener method must have exactly one parameter: {}.{}", 
                    bean.getClass().getSimpleName(), method.getName());
            return;
        }
        
        Class<?> eventType = parameterTypes[0];
        
        // 创建监听器
        EventDispatcher.EventListener<Object> listener = event -> {
            try {
                method.setAccessible(true);
                method.invoke(bean, event);
            } catch (Exception e) {
                log.error("Error invoking event listener method: {}.{}", 
                        bean.getClass().getSimpleName(), method.getName(), e);
            }
        };
        
        // 注册监听器
        @SuppressWarnings("unchecked")
        Class<Object> eventClass = (Class<Object>) eventType;
        eventDispatcher.listen(eventClass, listener);
    }
    
    /**
     * 注册模型观察者
     */
    private void registerModelObservers() {
        Map<String, Object> observers = applicationContext.getBeansOfType(Object.class);
        
        for (Map.Entry<String, Object> entry : observers.entrySet()) {
            Object bean = entry.getValue();
            String beanName = entry.getKey();
            
            // 检查是否实现了ModelObserver接口
            if (bean instanceof ModelObserver) {
                registerModelObserver(bean, beanName);
            }
        }
    }
    
    /**
     * 注册模型观察者
     * 
     * @param bean Bean实例
     * @param beanName Bean名称
     */
    @SuppressWarnings("unchecked")
    private void registerModelObserver(Object bean, String beanName) {
        // 检查是否有@Observes注解
        Observes observes = bean.getClass().getAnnotation(Observes.class);
        if (observes != null) {
            Class<?> modelClass = observes.value();
            if (Model.class.isAssignableFrom(modelClass)) {
                @SuppressWarnings("unchecked")
                Class<Model<?>> typedModelClass = (Class<Model<?>>) modelClass;
                @SuppressWarnings("unchecked")
                ModelObserver<Model<?>> typedObserver = (ModelObserver<Model<?>>) bean;
                modelEventManager.observe(typedModelClass, typedObserver);
                log.debug("Registered model observer: {} for model: {}", beanName, modelClass.getSimpleName());
            } else {
                log.warn("Invalid model class in @Observes annotation: {}", modelClass.getName());
            }
        } else {
            log.debug("Model observer without @Observes annotation: {}", beanName);
        }
    }
}
