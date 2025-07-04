package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户观察者示例
 * 演示如何使用@Observes注解自动注册观察者
 * 
 * @author luyunfeng
 */
@Slf4j
@Component
@Observes(User.class)
public class UserObserverExample implements ModelObserver<User> {
    
    @Override
    public void creating(User user) {
        log.info("UserObserverExample: User creating - {}", user.getName());
        // 在创建前可以修改用户数据
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail("default@example.com");
        }
    }
    
    @Override
    public void created(User user) {
        log.info("UserObserverExample: User created - {}", user.getName());
        // 创建后可以发送欢迎邮件等
    }
    
    @Override
    public void saving(User user) {
        log.info("UserObserverExample: User saving - {}", user.getName());
        // 保存前可以验证数据
    }
    
    @Override
    public void saved(User user) {
        log.info("UserObserverExample: User saved - {}", user.getName());
        // 保存后可以更新缓存等
    }
    
    @Override
    public void updating(User user) {
        log.info("UserObserverExample: User updating - {}", user.getName());
        // 更新前可以记录变更日志
    }
    
    @Override
    public void updated(User user) {
        log.info("UserObserverExample: User updated - {}", user.getName());
        // 更新后可以发送通知
    }
    
    @Override
    public void deleting(User user) {
        log.info("UserObserverExample: User deleting - {}", user.getName());
        // 删除前可以检查依赖关系
    }
    
    @Override
    public void deleted(User user) {
        log.info("UserObserverExample: User deleted - {}", user.getName());
        // 删除后可以清理相关数据
    }
    
    @Override
    public void retrieved(User user) {
        log.info("UserObserverExample: User retrieved - {}", user.getName());
        // 检索后可以更新访问时间等
    }
}