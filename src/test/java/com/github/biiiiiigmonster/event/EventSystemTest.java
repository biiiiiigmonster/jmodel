package com.github.biiiiiigmonster.event;

import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件系统测试类
 * 演示完整的事件功能
 * 
 * @author luyunfeng
 */
@SpringBootTest
@ActiveProfiles("test")
public class EventSystemTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EventDispatcher eventDispatcher;
    
    @Autowired
    private ModelEventManager modelEventManager;
    
    @Test
    public void testEventSystem() {
        // 注册事件监听器
        eventDispatcher.listen(ModelCreatedEvent.class, event -> {
            User user = (User) event.getModel();
            System.out.println("User created: " + user.getName());
        });
        
        eventDispatcher.listen(ModelSavedEvent.class, event -> {
            User user = (User) event.getModel();
            System.out.println("User saved: " + user.getName());
        });
        
        eventDispatcher.listen(ModelDeletedEvent.class, event -> {
            User user = (User) event.getModel();
            System.out.println("User deleted: " + user.getName());
        });
        
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        
        // 保存用户（会触发创建事件）
        boolean saved = user.save();
        assertTrue(saved);
        
        // 更新用户（会触发更新事件）
        user.setName("Updated User");
        boolean updated = user.save();
        assertTrue(updated);
        
        // 删除用户（会触发删除事件）
        boolean deleted = user.delete();
        assertTrue(deleted);
    }
    
    @Test
    public void testModelObserver() {
        // 创建用户观察者
        UserObserver userObserver = new UserObserver();
        modelEventManager.observe(User.class, userObserver);
        
        // 创建用户
        User user = new User();
        user.setName("Observer Test User");
        user.setEmail("observer@example.com");
        
        // 保存用户
        boolean saved = user.save();
        assertTrue(saved);
        
        // 验证观察者被调用
        assertTrue(userObserver.isCreatedCalled());
        assertTrue(userObserver.isSavedCalled());
        
        // 删除用户
        boolean deleted = user.delete();
        assertTrue(deleted);
        
        // 验证删除观察者被调用
        assertTrue(userObserver.isDeletedCalled());
    }
    
    @Test
    public void testEventMuting() {
        // 静音User模型的事件
        User.muteEvents(User.class);
        assertTrue(User.isEventsMuted(User.class));
        
        // 创建用户观察者
        UserObserver userObserver = new UserObserver();
        modelEventManager.observe(User.class, userObserver);
        
        // 创建用户（事件被静音，观察者不会被调用）
        User user = new User();
        user.setName("Muted User");
        user.setEmail("muted@example.com");
        
        boolean saved = user.save();
        assertTrue(saved);
        
        // 验证观察者没有被调用
        assertFalse(userObserver.isCreatedCalled());
        assertFalse(userObserver.isSavedCalled());
        
        // 取消静音
        User.unmuteEvents(User.class);
        assertFalse(User.isEventsMuted(User.class));
        
        // 创建另一个用户（事件不再被静音）
        User user2 = new User();
        user2.setName("Unmuted User");
        user2.setEmail("unmuted@example.com");
        
        boolean saved2 = user2.save();
        assertTrue(saved2);
        
        // 验证观察者被调用
        assertTrue(userObserver.isCreatedCalled());
        assertTrue(userObserver.isSavedCalled());
    }
    
    /**
     * 用户观察者实现
     */
    private static class UserObserver implements ModelObserver<User> {
        private boolean createdCalled = false;
        private boolean savedCalled = false;
        private boolean deletedCalled = false;
        
        @Override
        public void created(User user) {
            createdCalled = true;
            System.out.println("UserObserver: User created - " + user.getName());
        }
        
        @Override
        public void saved(User user) {
            savedCalled = true;
            System.out.println("UserObserver: User saved - " + user.getName());
        }
        
        @Override
        public void deleted(User user) {
            deletedCalled = true;
            System.out.println("UserObserver: User deleted - " + user.getName());
        }
        
        public boolean isCreatedCalled() {
            return createdCalled;
        }
        
        public boolean isSavedCalled() {
            return savedCalled;
        }
        
        public boolean isDeletedCalled() {
            return deletedCalled;
        }
    }
}
