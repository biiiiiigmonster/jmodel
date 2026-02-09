package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Dirty Tracking API 单元测试 (Phase 4 - P4-01)
 */
public class DirtyTrackingApiTest extends BaseTest {

    @Test
    public void testIsDirty_beforeSyncOriginal_returnsFalse() {
        User user = findById(User.class, 1L);
        assertNotNull(user);
        assertFalse("syncOriginal 前 isDirty 应为 false", user.isDirty());
    }

    @Test
    public void testIsDirty_afterSyncOriginal_andSetterCall_returnsTrue() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("New Name");
        assertTrue("syncOriginal 后 setter 调用，isDirty 应为 true", user.isDirty());
    }

    @Test
    public void testSetterCall_beforeSyncOriginal_noTracking() {
        User user = findById(User.class, 1L);
        user.setName("No Tracking");
        assertFalse("syncOriginal 前 setter 不应触发追踪", user.isDirty());
        assertTrue("未追踪时 getDirty 应为空", user.getDirty().isEmpty());
        assertNull("未追踪时 getOriginal(field) 应返回 null", user.getOriginal("name"));
        assertTrue("未追踪时 getOriginal() 应返回空 Map", user.getOriginal().isEmpty());
    }

    @Test
    public void testIsDirty_afterSyncOriginal_noModification_returnsFalse() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        assertFalse("syncOriginal 后未修改，isDirty 应为 false", user.isDirty());
    }

    @Test
    public void testIsDirty_multipleFields_anyMatch() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Changed");
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));
        assertFalse("isDirty(\"email\") 应为 false", user.isDirty("email"));
        assertTrue("isDirty(\"name\", \"email\") 任一匹配应为 true", user.isDirty("name", "email"));
        assertFalse("isDirty(\"id\", \"email\") 均未变更应为 false", user.isDirty("id", "email"));
    }

    @Test
    public void testIsDirty_lambdaVersion() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Lambda Test");
        assertTrue("isDirty(User::getName) 应为 true", user.isDirty(User::getName));
        assertFalse("isDirty(User::getEmail) 应为 false", user.isDirty(User::getEmail));
    }

    @Test
    public void testGetDirty_returnsAllChangedFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("New Name");
        user.setEmail("new@email.com");
        Map<String, Object> dirty = user.getDirty();
        assertEquals("应有 2 个脏字段", 2, dirty.size());
        assertEquals("New Name", dirty.get("name"));
        assertEquals("new@email.com", dirty.get("email"));
    }

    @Test
    public void testGetDirty_withFieldFilter_returnsFilteredFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("New Name");
        user.setEmail("new@email.com");
        Map<String, Object> nameOnly = user.getDirty("name");
        assertEquals("过滤后应只有 1 个字段", 1, nameOnly.size());
        assertEquals("New Name", nameOnly.get("name"));
        Map<String, Object> idOnly = user.getDirty("id");
        assertTrue("id 未修改，过滤结果应为空", idOnly.isEmpty());
    }

    @Test
    public void testGetDirty_noChanges_returnsEmptyMap() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        Map<String, Object> dirty = user.getDirty();
        assertTrue("无变更时 getDirty 应为空 Map", dirty.isEmpty());
    }

    @Test
    public void testGetOriginal_returnsSyncedValue() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        String originalEmail = user.getEmail();
        user.syncOriginal();
        user.setName("Changed");
        user.setEmail("changed@test.com");
        assertEquals("getOriginal(\"name\") 应返回同步时的值", originalName, user.getOriginal("name"));
        assertEquals("getOriginal(\"email\") 应返回同步时的值", originalEmail, user.getOriginal("email"));
    }

    @Test
    public void testGetOriginal_withDefault_returnsDefaultWhenNull() {
        User untrackedUser = findById(User.class, 1L);
        assertEquals("未追踪时应返回默认值", "fallback", untrackedUser.getOriginal("name", "fallback"));
        User trackedUser = findById(User.class, 1L);
        trackedUser.syncOriginal();
        Object originalName = trackedUser.getOriginal("name", "default_name");
        assertNotEquals("追踪状态下有值时不应返回默认值", "default_name", originalName);
    }

    @Test
    public void testGetOriginal_allFields_returnsUnmodifiableMap() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        Map<String, Object> original = user.getOriginal();
        assertFalse("原始值 Map 不应为空", original.isEmpty());
        try {
            original.put("name", "should_fail");
            fail("getOriginal() 返回的 Map 应为不可修改");
        } catch (UnsupportedOperationException e) {
            // 预期异常
        }
    }

    @Test
    public void testGetOriginal_untrackedState_returnsEmptyMap() {
        User user = findById(User.class, 1L);
        Map<String, Object> original = user.getOriginal();
        assertTrue("未追踪时 getOriginal() 应返回空 Map", original.isEmpty());
    }

    @Test
    public void testGetOriginal_untrackedState_returnsNull() {
        User user = findById(User.class, 1L);
        assertNull("未追踪时 getOriginal(field) 应返回 null", user.getOriginal("name"));
    }

    @Test
    public void testGetOriginal_lambdaVersion() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        user.setName("Lambda Original Test");
        String original = user.getOriginal(User::getName);
        assertEquals("Lambda getOriginal 应返回原始名称", originalName, original);
    }

    @Test
    public void testWasChanged_afterSave_returnsTrue() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Updated");
        user.save();
        assertTrue("save 后 wasChanged() 应为 true", user.wasChanged());
        assertTrue("save 后 wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertFalse("save 后 wasChanged(\"email\") 应为 false（未修改）", user.wasChanged("email"));
    }

    @Test
    public void testWasChanged_beforeSave_returnsFalse() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Not Yet Saved");
        assertFalse("save 前 wasChanged() 应为 false", user.wasChanged());
    }

    @Test
    public void testGetChanges_afterSave_returnsChangedFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Changed Name");
        user.setEmail("changed@test.com");
        user.save();
        Map<String, Object> changes = user.getChanges();
        assertEquals("getChanges 应有 2 个字段", 2, changes.size());
        assertEquals("Changed Name", changes.get("name"));
        assertEquals("changed@test.com", changes.get("email"));
    }

    @Test
    public void testGetChanges_returnsUnmodifiableMap() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Test");
        user.save();
        Map<String, Object> changes = user.getChanges();
        try {
            changes.put("hack", "value");
            fail("getChanges() 返回的 Map 应为不可修改");
        } catch (UnsupportedOperationException e) {
            // 预期异常
        }
    }

    @Test
    public void testGetChanges_beforeSave_returnsEmptyMap() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Not Saved");
        Map<String, Object> changes = user.getChanges();
        assertTrue("save 前 getChanges 应为空", changes.isEmpty());
    }

    @Test
    public void testSyncOriginal_enablesTracking() {
        User user = findById(User.class, 1L);
        assertFalse("初始状态 isDirty 应为 false", user.isDirty());
        user.syncOriginal();
        user.setName("Tracked");
        assertTrue("syncOriginal 后修改应被追踪", user.isDirty());
    }

    @Test
    public void testSyncOriginal_clearsChanges() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Dirty");
        assertTrue("修改后应为 dirty", user.isDirty());
        user.syncOriginal();
        assertFalse("重新 syncOriginal 后应清空变更", user.isDirty());
    }

    @Test
    public void testSyncOriginal_updatesOriginalValues() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        assertEquals("syncOriginal 后原始值应为当前值", originalName, user.getOriginal("name"));
        user.setName("New Value");
        user.syncOriginal();
        assertEquals("重新 syncOriginal 后原始值应更新为新的当前值", "New Value", user.getOriginal("name"));
    }

    @Test
    public void testSyncOriginal_partialFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Changed Name");
        user.setEmail("changed@email.com");
        assertTrue("name 应为 dirty", user.isDirty("name"));
        assertTrue("email 应为 dirty", user.isDirty("email"));
        user.syncOriginal("name");
        assertFalse("name 同步后不应为 dirty", user.isDirty("name"));
        assertTrue("email 未同步仍应为 dirty", user.isDirty("email"));
        assertEquals("name 原始值应更新", "Changed Name", user.getOriginal("name"));
    }

    @Test
    public void testSyncOriginal_partialFields_onUntrackedEntity() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal("name");
        user.setName("After Partial Sync");
        assertTrue("部分同步后应进入追踪状态", user.isDirty("name"));
        assertEquals("原始值应为全量同步时的值", originalName, user.getOriginal("name"));
    }

    @Test
    public void testRelationField_notTracked() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        Map<String, Object> original = user.getOriginal();
        assertFalse("关系字段 phone 不应在原始快照中", original.containsKey("phone"));
        assertFalse("关系字段 profile 不应在原始快照中", original.containsKey("profile"));
        assertFalse("关系字段 posts 不应在原始快照中", original.containsKey("posts"));
        assertFalse("关系字段 roles 不应在原始快照中", original.containsKey("roles"));
        assertFalse("关系字段 image 不应在原始快照中", original.containsKey("image"));
        assertFalse("关系字段 profileAddress 不应在原始快照中", original.containsKey("profileAddress"));
        assertFalse("关系字段 commentLikes 不应在原始快照中", original.containsKey("commentLikes"));
        assertTrue("持久化字段 id 应在原始快照中", original.containsKey("id"));
        assertTrue("持久化字段 name 应在原始快照中", original.containsKey("name"));
        assertTrue("持久化字段 email 应在原始快照中", original.containsKey("email"));
    }

    @Test
    public void testRevertToOriginal_notMarkedAsDirty() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        user.setName("Temporary");
        assertTrue("修改后应为 dirty", user.isDirty("name"));
        user.setName(originalName);
        assertFalse("恢复原值后不应为 dirty", user.isDirty("name"));
        assertFalse("恢复原值后 isDirty() 也应为 false", user.isDirty());
    }

    @Test
    public void testMultipleModifications_allTracked() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        user.setName("First");
        user.setName("Second");
        user.setName("Third");
        assertTrue("多次修改后 isDirty 应为 true", user.isDirty("name"));
        assertEquals("getDirty 应返回最终值", "Third", user.getDirty().get("name"));
        assertEquals("getOriginal 应返回初始原始值", originalName, user.getOriginal("name"));
    }

    @Test
    public void testMultipleModifications_revertToOriginal() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        user.setName("First");
        user.setName("Second");
        user.setName(originalName);
        assertFalse("多次修改后恢复原值不应为 dirty", user.isDirty("name"));
    }

    @Test
    public void testFullWorkflow_load_modify_save_checkChanges() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        String originalEmail = user.getEmail();
        assertNotNull(user);
        user.syncOriginal();
        assertFalse("刚启用追踪，isDirty 应为 false", user.isDirty());
        user.setName("Workflow Name");
        user.setEmail("workflow@test.com");
        assertTrue("isDirty() 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));
        assertTrue("isDirty(\"email\") 应为 true", user.isDirty("email"));
        assertEquals("getOriginal(\"name\") 应为原始值", originalName, user.getOriginal("name"));
        assertEquals("getOriginal(\"email\") 应为原始值", originalEmail, user.getOriginal("email"));
        Map<String, Object> dirty = user.getDirty();
        assertEquals("Workflow Name", dirty.get("name"));
        assertEquals("workflow@test.com", dirty.get("email"));
        user.save();
        assertFalse("save 后 isDirty 应为 false", user.isDirty());
        assertTrue("save 后 wasChanged 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertTrue("wasChanged(\"email\") 应为 true", user.wasChanged("email"));
        Map<String, Object> changes = user.getChanges();
        assertEquals("Workflow Name", changes.get("name"));
        assertEquals("workflow@test.com", changes.get("email"));
    }
}
