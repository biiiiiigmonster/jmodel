package com.github.biiiiiigmonster.model;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * 测试Model类的脏字段跟踪功能
 */
public class DirtyTrackingTest extends BaseTest {

    @Test
    public void testNewModelIsDirty() {
        // 创建新模型
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");

        // 新模型应该是脏的（因为还没有保存）
        assertFalse("新模型不应该是干净的", user.isClean());
        assertTrue("新模型应该是脏的", user.isDirty());
        assertFalse("新模型应该不存在于数据库中", user.exists());
        assertTrue("新模型应该是新创建的", user.wasRecentlyCreated());
    }

    @Test
    public void testModelFromDatabaseIsClean() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 从数据库加载的模型应该是干净的
        assertTrue("从数据库加载的模型应该是干净的", user.isClean());
        assertFalse("从数据库加载的模型不应该是脏的", user.isDirty());
        assertTrue("从数据库加载的模型应该存在于数据库中", user.exists());
        assertTrue("从数据库加载的模型不应该是新创建的", user.wasRecentlyCreated());
    }

    @Test
    public void testModelBecomesDirtyAfterModification() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 修改字段
        user.setName("Modified Name");

        // 模型应该变成脏的
        assertFalse("修改后模型不应该是干净的", user.isClean());
        assertTrue("修改后模型应该是脏的", user.isDirty());
        assertTrue("name字段应该是脏的", user.isDirty("name"));
        assertFalse("email字段不应该是脏的", user.isDirty("email"));

        // 检查脏字段值
        assertEquals("脏字段值应该是修改后的值", "Modified Name", user.getDirty("name"));
        assertEquals("脏字段值应该是修改后的值", "Modified Name", user.getDirty(User::getName));
    }

    @Test
    public void testGetOriginalValues() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        String originalName = user.getName();
        String originalEmail = user.getEmail();

        // 修改字段
        user.setName("Modified Name");
        user.setEmail("modified@example.com");

        // 检查原始值
        assertEquals("原始name值应该保持不变", originalName, user.getOriginal("name"));
        assertEquals("原始email值应该保持不变", originalEmail, user.getOriginal("email"));
        assertEquals("原始name值应该保持不变", originalName, user.getOriginal(User::getName));
        assertEquals("原始email值应该保持不变", originalEmail, user.getOriginal(User::getEmail));
    }

    @Test
    public void testSyncOriginal() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 修改字段
        user.setName("Modified Name");
        assertTrue("修改后模型应该是脏的", user.isDirty());

        // 同步原始值
        user.syncOriginal();

        // 模型应该变成干净的
        assertTrue("同步后模型应该是干净的", user.isClean());
        assertFalse("同步后模型不应该是脏的", user.isDirty());
    }

    @Test
    public void testSyncSpecificFields() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 修改多个字段
        user.setName("Modified Name");
        user.setEmail("modified@example.com");

        // 只同步name字段
        user.syncOriginal("name");

        // name字段应该是干净的，email字段应该是脏的
        assertFalse("name字段不应该是脏的", user.isDirty("name"));
        assertTrue("email字段应该是脏的", user.isDirty("email"));
        assertTrue("模型整体应该是脏的", user.isDirty());
    }

    @Test
    public void testSyncSpecificFieldsWithLambda() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 修改多个字段
        user.setName("Modified Name");
        user.setEmail("modified@example.com");

        // 只同步name字段
        user.syncOriginal(User::getName);

        // name字段应该是干净的，email字段应该是脏的
        assertFalse("name字段不应该是脏的", user.isDirty(User::getName));
        assertTrue("email字段应该是脏的", user.isDirty(User::getEmail));
        assertTrue("模型整体应该是脏的", user.isDirty());
    }

    @Test
    public void testSaveOnlyDirtyFields() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        String originalName = user.getName();
        String originalEmail = user.getEmail();

        // 只修改name字段
        user.setName("Modified Name Only");

        // 保存模型
        boolean saved = user.save();
        assertTrue("保存应该成功", saved);

        // 验证只有name字段被更新
        User updatedUser = userMapper.selectById(user.getId());
        assertEquals("name字段应该被更新", "Modified Name Only", updatedUser.getName());
        assertEquals("email字段应该保持不变", originalEmail, updatedUser.getEmail());

        // 保存后模型应该是干净的
        assertTrue("保存后模型应该是干净的", user.isClean());
    }

    @Test
    public void testSaveNewModel() {
        // 创建新模型
        User user = new User();
        user.setName("New User");
        user.setEmail("newuser@example.com");

        // 新模型应该是脏的（因为还没有保存）
        assertTrue("新模型应该是脏的", user.isDirty());

        // 保存模型
        boolean saved = user.save();
        assertTrue("保存应该成功", saved);

        // 保存后模型应该是干净的
        assertTrue("保存后模型应该是干净的", user.isClean());
        assertTrue("保存后模型应该存在于数据库中", user.exists());
        assertTrue("保存后模型应该是新创建的", user.wasRecentlyCreated());
    }

    @Test
    public void testSaveWithoutChanges() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 不修改任何字段
        assertTrue("未修改的模型应该是干净的", user.isClean());

        // 保存模型
        boolean saved = user.save();
        assertTrue("保存应该成功", saved);

        // 模型应该仍然是干净的
        assertTrue("保存后模型应该仍然是干净的", user.isClean());
    }

    @Test
    public void testGetDirtyMap() {
        // 从数据库加载模型
        User user = userMapper.selectById(1L);
        assertNotNull("用户应该存在", user);
        
        // 手动标记为已存在（模拟从数据库加载）
        user.markAsExisting();

        // 修改多个字段
        user.setName("Modified Name");
        user.setEmail("modified@example.com");

        // 获取脏字段映射
        Map<String, Object> dirtyMap = user.getDirty();

        // 验证脏字段映射
        assertEquals("脏字段数量应该是2", 2, dirtyMap.size());
        assertTrue("脏字段映射应该包含name", dirtyMap.containsKey("name"));
        assertTrue("脏字段映射应该包含email", dirtyMap.containsKey("email"));
        assertEquals("name字段值应该是修改后的值", "Modified Name", dirtyMap.get("name"));
        assertEquals("email字段值应该是修改后的值", "modified@example.com", dirtyMap.get("email"));
    }
}
