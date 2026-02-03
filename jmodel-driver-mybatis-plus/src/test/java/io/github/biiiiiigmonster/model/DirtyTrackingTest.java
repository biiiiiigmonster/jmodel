package io.github.biiiiiigmonster.model;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration tests for Model dirty-tracking functionality.
 * <p>
 * Phase 3: 集成验证
 * - P3-01: 验证通过 ORM 查询的实体能正确追踪
 * - P3-02: 验证直接 mapper 查询的实体能正确追踪
 * - P3-03: 验证 new 创建的实体能正确追踪
 *
 * @author luyunfeng
 */
public class DirtyTrackingTest extends BaseTest {

    // ==================== P3-01: ORM 查询实体追踪测试 ====================
    // Note: 当前框架使用 mapper 查询等效于 ORM 查询

    /**
     * 测试从数据库查询的实体，通过 setter 修改后能正确追踪变更
     * <p>
     * 重要：需要先建立快照（通过 syncOriginal/isDirty/getOriginal），然后 setter 的修改才会被追踪。
     * 这是因为 ORM 框架通过 setter 填充实体，我们需要在填充完成后才开始追踪。
     */
    @Test
    public void testMapperQuery_setterModification_tracksChanges() {
        // Given: 从数据库查询用户
        User user = userMapper.selectById(1L);
        assertNotNull("User should exist", user);
        String originalName = user.getName();

        // 建立快照（此时实体是"干净的"）
        assertFalse("Should not be dirty after load", user.isDirty());

        // When: 通过 setter 修改字段
        user.setName("NewName");

        // Then: dirty-tracking 应该正确追踪变更
        assertTrue("Should be dirty after setter call", user.isDirty());
        assertTrue("Name field should be dirty", user.isDirty("name"));
        assertFalse("Email field should not be dirty", user.isDirty("email"));

        // 验证 getDirty() 返回正确的脏字段
        Map<String, Object> dirty = user.getDirty();
        assertEquals("Should have 1 dirty field", 1, dirty.size());
        assertEquals("Dirty name should be NewName", "NewName", dirty.get("name"));

        // 验证 getOriginal() 返回原始值
        assertEquals("Original name should match", originalName, user.getOriginal("name"));
    }

    /**
     * 测试从数据库查询的实体，修改后保存，wasChanged() 应返回正确结果
     */
    @Test
    public void testMapperQuery_saveAndWasChanged() {
        // Given: 从数据库查询用户
        User user = userMapper.selectById(1L);
        String originalName = user.getName();

        // 建立快照
        user.syncOriginal();

        // When: 修改并保存
        user.setName("SavedName");
        user.save();

        // Then: isDirty() 应该返回 false（已保存）
        assertFalse("Should not be dirty after save", user.isDirty());

        // wasChanged() 应该返回 true
        assertTrue("wasChanged should return true", user.wasChanged());
        assertTrue("wasChanged(name) should return true", user.wasChanged("name"));
        assertFalse("wasChanged(email) should return false", user.wasChanged("email"));

        // getChanges() 应该返回本次保存的变更
        Map<String, Object> changes = user.getChanges();
        assertEquals("Should have 1 change", 1, changes.size());
        assertEquals("Change should be SavedName", "SavedName", changes.get("name"));
    }

    /**
     * 测试修改后恢复原值，isDirty 应该返回 false
     */
    @Test
    public void testMapperQuery_revertToOriginal_notDirty() {
        // Given: 从数据库查询用户
        User user = userMapper.selectById(1L);
        String originalName = user.getName();

        // 建立快照
        user.syncOriginal();

        // When: 修改后再恢复为原值
        user.setName("TempName");
        assertTrue("Should be dirty after modification", user.isDirty("name"));

        user.setName(originalName);

        // Then: 应该不再是 dirty
        assertFalse("Should not be dirty after reverting", user.isDirty("name"));
        assertFalse("Should not be dirty overall", user.isDirty());
    }

    // ==================== P3-02: 直接 mapper 查询实体追踪测试 ====================

    /**
     * 测试直接使用 mapper 查询（selectById）的实体能正确追踪
     */
    @Test
    public void testDirectMapperQuery_tracksChanges() {
        // Given: 直接 mapper 查询
        User user = userMapper.selectById(2L);
        assertNotNull("User should exist", user);

        // 建立快照
        user.syncOriginal();

        // When: 修改多个字段
        user.setName("DirectMapperName");
        user.setEmail("direct@test.com");

        // Then: 所有修改的字段都应该被追踪
        assertTrue("Should be dirty", user.isDirty());
        assertTrue("Name should be dirty", user.isDirty("name"));
        assertTrue("Email should be dirty", user.isDirty("email"));
        assertFalse("Id should not be dirty", user.isDirty("id"));

        Map<String, Object> dirty = user.getDirty();
        assertEquals("Should have 2 dirty fields", 2, dirty.size());
        assertEquals("DirectMapperName", dirty.get("name"));
        assertEquals("direct@test.com", dirty.get("email"));
    }

    /**
     * 测试 getDirty(fields...) 过滤功能
     */
    @Test
    public void testDirectMapperQuery_getDirtyWithFilter() {
        // Given: 查询并修改多个字段
        User user = userMapper.selectById(1L);

        // 建立快照
        user.syncOriginal();

        user.setName("FilterTestName");
        user.setEmail("filter@test.com");

        // When: 只获取 name 字段的脏数据
        Map<String, Object> filteredDirty = user.getDirty("name");

        // Then: 应该只包含 name 字段
        assertEquals("Should have 1 filtered field", 1, filteredDirty.size());
        assertEquals("FilterTestName", filteredDirty.get("name"));
        assertNull("Email should not be included", filteredDirty.get("email"));
    }

    // ==================== P3-03: new 创建实体追踪测试 ====================

    /**
     * 测试 new 创建的实体能正确追踪
     */
    @Test
    public void testNewEntity_tracksAllSetterCalls() {
        // Given: 创建新实体
        User user = new User();

        // 初始状态应该不是 dirty（没有调用过 setter 建立快照）
        assertFalse("New entity should not be dirty initially", user.isDirty());

        // When: 通过 setter 设置字段
        user.setName("NewUser");
        user.setEmail("new@example.com");

        // Then: 所有设置的字段都应该是 dirty
        assertTrue("Should be dirty after setters", user.isDirty());
        assertTrue("Name should be dirty", user.isDirty("name"));
        assertTrue("Email should be dirty", user.isDirty("email"));

        // 因为是新实体，原始值应该是 null
        assertNull("Original name should be null", user.getOriginal("name"));
        assertNull("Original email should be null", user.getOriginal("email"));
    }

    /**
     * 测试 new 创建的实体保存后的状态
     * <p>
     * 对于新实体，需要先建立快照才能追踪 setter 的变更。
     */
    @Test
    public void testNewEntity_saveAndCheckChanges() {
        // Given: 创建新实体
        User user = new User();

        // 先建立快照（此时所有字段为 null）
        user.syncOriginal();

        // 设置字段（此时会被追踪）
        user.setName("BrandNewUser");
        user.setEmail("brandnew@example.com");

        // When: 保存
        user.save();

        // Then: isDirty 应该为 false，wasChanged 应该为 true
        assertFalse("Should not be dirty after save", user.isDirty());
        assertTrue("wasChanged should return true", user.wasChanged());
        assertTrue("wasChanged(name) should return true", user.wasChanged("name"));
        assertTrue("wasChanged(email) should return true", user.wasChanged("email"));

        // getChanges 应该包含所有设置的字段
        Map<String, Object> changes = user.getChanges();
        assertEquals("BrandNewUser", changes.get("name"));
        assertEquals("brandnew@example.com", changes.get("email"));
    }

    // ==================== 快照对比（兜底检测）测试 ====================

    /**
     * 测试直接字段赋值能在 save 前通过快照对比检测到
     * 注意：这需要在直接赋值前先建立快照
     */
    @Test
    public void testDirectFieldAssignment_detectedBySnapshotComparison() {
        // Given: 从数据库查询用户
        User user = userMapper.selectById(1L);
        String originalName = user.getName();

        // 先建立快照
        user.syncOriginal();

        // When: 直接字段赋值（绕过 setter）
        ReflectUtil.setFieldValue(user, "name", "DirectAssignedName");

        // Then: 快照对比应该能检测到变更
        // 注意：isDirty() 调用时会执行 detectUntrackedChanges()
        assertTrue("Should detect direct assignment", user.isDirty());
        assertTrue("Name should be dirty", user.isDirty("name"));

        Map<String, Object> dirty = user.getDirty();
        assertEquals("DirectAssignedName", dirty.get("name"));
    }

    /**
     * 测试反射赋值能在 save 前通过快照对比检测到
     */
    @Test
    public void testReflectionAssignment_detectedBySnapshotComparison() {
        // Given: 从数据库查询用户
        User user = userMapper.selectById(1L);

        // 先建立快照
        user.syncOriginal();

        // When: 反射赋值
        ReflectUtil.setFieldValue(user, "email", "reflection@test.com");

        // Then: 快照对比应该能检测到变更
        assertTrue("Should detect reflection assignment", user.isDirty());
        assertTrue("Email should be dirty", user.isDirty("email"));
    }

    // ==================== 类型安全 API 测试 ====================

    /**
     * 测试 isDirty(lambda) 类型安全版本
     */
    @Test
    public void testTypeSafeIsDirty() {
        // Given: 查询并修改
        User user = userMapper.selectById(1L);

        // 建立快照
        user.syncOriginal();

        user.setName("TypeSafeTest");

        // Then: 类型安全版本应该正确工作
        assertTrue("isDirty(User::getName) should return true", user.isDirty(User::getName));
        assertFalse("isDirty(User::getEmail) should return false", user.isDirty(User::getEmail));
    }

    /**
     * 测试 getOriginal(lambda) 类型安全版本
     */
    @Test
    public void testTypeSafeGetOriginal() {
        // Given: 查询并修改
        User user = userMapper.selectById(1L);
        String originalName = user.getName();

        // 建立快照（通过 getOriginal 建立）
        assertEquals("getOriginal should return current value as original", originalName, user.getOriginal("name"));

        user.setName("TypeSafeOriginalTest");

        // Then: 类型安全版本应该返回原始值
        String originalFromLambda = user.getOriginal(User::getName);
        assertEquals("getOriginal(User::getName) should return original", originalName, originalFromLambda);
    }

    // ==================== syncOriginal 测试 ====================

    /**
     * 测试 syncOriginal() 重置所有字段
     */
    @Test
    public void testSyncOriginal_resetsAllChanges() {
        // Given: 查询并修改
        User user = userMapper.selectById(1L);

        // 建立快照
        user.syncOriginal();

        user.setName("SyncTest");
        user.setEmail("sync@test.com");
        assertTrue("Should be dirty", user.isDirty());

        // When: 调用 syncOriginal()
        user.syncOriginal();

        // Then: 不再是 dirty
        assertFalse("Should not be dirty after syncOriginal", user.isDirty());
        // 原始值应该是当前值
        assertEquals("SyncTest", user.getOriginal("name"));
        assertEquals("sync@test.com", user.getOriginal("email"));
    }

    /**
     * 测试 syncOriginal(fields...) 只重置指定字段
     */
    @Test
    public void testSyncOriginalFields_resetsSpecificFields() {
        // Given: 查询并修改
        User user = userMapper.selectById(1L);

        // 建立快照
        user.syncOriginal();

        user.setName("SyncFieldTest");
        user.setEmail("syncfield@test.com");
        assertTrue("Name should be dirty", user.isDirty("name"));
        assertTrue("Email should be dirty", user.isDirty("email"));

        // When: 只同步 name 字段
        user.syncOriginal("name");

        // Then: name 不再 dirty，但 email 仍然 dirty
        assertFalse("Name should not be dirty after sync", user.isDirty("name"));
        assertTrue("Email should still be dirty", user.isDirty("email"));
    }

    // ==================== 关系字段不追踪测试 ====================

    /**
     * 测试关系字段不被追踪
     */
    @Test
    public void testRelationField_notTracked() {
        // Given: 查询用户
        User user = userMapper.selectById(1L);

        // 建立快照
        user.syncOriginal();

        // When: 尝试检查关系字段
        // 关系字段（如 phone, profile）不应该出现在 dirty tracking 中

        // 即使直接设置关系字段的值，也不应该被追踪
        // 因为关系字段有独立的事件机制

        // Then: getDirty 不应该包含关系字段
        user.setName("RelationTest");
        Map<String, Object> dirty = user.getDirty();

        assertFalse("phone should not be tracked", dirty.containsKey("phone"));
        assertFalse("profile should not be tracked", dirty.containsKey("profile"));
        assertFalse("posts should not be tracked", dirty.containsKey("posts"));
    }

    // ==================== 边界情况测试 ====================

    /**
     * 测试设置 null 值
     */
    @Test
    public void testSetNullValue() {
        // Given: 查询用户
        User user = userMapper.selectById(1L);
        String originalName = user.getName();
        assertNotNull("Original name should not be null", originalName);

        // 建立快照
        user.syncOriginal();

        // When: 设置为 null
        user.setName(null);

        // Then: 应该被追踪为变更
        assertTrue("Should be dirty after setting null", user.isDirty("name"));
        assertNull("Current value should be null", user.getDirty().get("name"));
        assertEquals("Original should be preserved", originalName, user.getOriginal("name"));
    }

    /**
     * 测试连续多次修改同一字段
     */
    @Test
    public void testMultipleModifications() {
        // Given: 查询用户
        User user = userMapper.selectById(1L);
        String originalName = user.getName();

        // 建立快照
        user.syncOriginal();

        // When: 多次修改
        user.setName("First");
        user.setName("Second");
        user.setName("Third");

        // Then: 只记录最终状态与原始值的差异
        assertTrue("Should be dirty", user.isDirty("name"));
        assertEquals("Dirty value should be Third", "Third", user.getDirty().get("name"));
        assertEquals("Original should still be original", originalName, user.getOriginal("name"));
    }

    /**
     * 测试无修改直接保存
     */
    @Test
    public void testSaveWithoutModification() {
        // Given: 查询用户但不修改
        User user = userMapper.selectById(1L);

        // When: 直接保存
        user.save();

        // Then: wasChanged 应该为 false（没有实际变更）
        assertFalse("wasChanged should be false", user.wasChanged());
        assertTrue("getChanges should be empty", user.getChanges().isEmpty());
    }
}
