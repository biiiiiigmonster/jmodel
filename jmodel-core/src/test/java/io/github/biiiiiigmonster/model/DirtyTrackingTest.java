package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Dirty Tracking 集成测试。
 * <p>
 * 覆盖 Phase 3 验证目标（不含 P3-02 QueryWrapper 特有测试，已留在 driver 模块）：
 * <ul>
 *   <li>P3-01: 验证通过 ORM 查询（findById）的实体能正确追踪</li>
 *   <li>P3-03: 验证 new 创建的实体能正确追踪</li>
 * </ul>
 */
public class DirtyTrackingTest extends BaseTest {

    // ==================== P3-01: findById 查询的实体追踪 ====================

    @Test
    public void testSelectById_syncOriginal_setterModify_isDirty() {
        User user = findById(User.class, 1L);
        assertNotNull(user);

        assertFalse("syncOriginal 前 isDirty 应为 false", user.isDirty());

        user.syncOriginal();
        assertFalse("syncOriginal 后未修改，isDirty 应为 false", user.isDirty());

        String originalName = user.getName();
        user.setName("New Name");

        assertTrue("修改后 isDirty() 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));
        assertFalse("isDirty(\"email\") 应为 false", user.isDirty("email"));
    }

    @Test
    public void testSelectById_syncOriginal_getDirty() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        user.setName("Changed Name");
        user.setEmail("changed@example.com");

        Map<String, Object> dirty = user.getDirty();
        assertEquals("脏字段数量应为 2", 2, dirty.size());
        assertEquals("Changed Name", dirty.get("name"));
        assertEquals("changed@example.com", dirty.get("email"));
    }

    @Test
    public void testSelectById_getDirtyWithFilter() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        user.setName("Changed Name");
        user.setEmail("changed@example.com");

        Map<String, Object> dirtyNameOnly = user.getDirty("name");
        assertEquals("过滤后应只有 1 个脏字段", 1, dirtyNameOnly.size());
        assertEquals("Changed Name", dirtyNameOnly.get("name"));

        Map<String, Object> dirtyIdOnly = user.getDirty("id");
        assertTrue("id 未修改，过滤结果应为空", dirtyIdOnly.isEmpty());
    }

    @Test
    public void testSelectById_getOriginal() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        String originalEmail = user.getEmail();

        user.syncOriginal();

        user.setName("Changed Name");
        user.setEmail("changed@example.com");

        assertEquals("getOriginal(\"name\") 应返回原始名称", originalName, user.getOriginal("name"));
        assertEquals("getOriginal(\"email\") 应返回原始邮箱", originalEmail, user.getOriginal("email"));

        Map<String, Object> allOriginals = user.getOriginal();
        assertFalse("所有原始值不应为空", allOriginals.isEmpty());
        assertEquals(originalName, allOriginals.get("name"));
        assertEquals(originalEmail, allOriginals.get("email"));
    }

    @Test
    public void testSelectById_getOriginalWithDefault() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        Object originalName = user.getOriginal("name", "default_name");
        assertNotEquals("default_name", originalName);

        User untrackedUser = findById(User.class, 2L);
        Object result = untrackedUser.getOriginal("name", "fallback");
        assertEquals("未追踪状态应返回默认值", "fallback", result);
    }

    @Test
    public void testSelectById_revertToOriginal_notDirty() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();

        user.setName("Temporary Name");
        assertTrue("修改后应为 dirty", user.isDirty("name"));

        user.setName(originalName);
        assertFalse("恢复原值后不应为 dirty", user.isDirty("name"));
        assertFalse("恢复原值后 isDirty() 也应为 false", user.isDirty());
    }

    @Test
    public void testSelectById_save_wasChanged() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        user.setName("Updated Name");
        user.save();

        assertFalse("save 后 isDirty() 应为 false", user.isDirty());
        assertTrue("wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertFalse("wasChanged(\"email\") 应为 false", user.wasChanged("email"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("changes 应包含 1 个字段", 1, changes.size());
        assertEquals("Updated Name", changes.get("name"));
    }

    @Test
    public void testSelectById_save_thenModifyAgain() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        user.setName("First Update");
        user.save();

        assertFalse("save 后 isDirty 应为 false", user.isDirty());

        user.setName("Second Update");
        assertTrue("第二次修改后 isDirty 应为 true", user.isDirty("name"));
        assertEquals("原始值应为 save 后的值", "First Update", user.getOriginal("name"));
    }

    @Test
    public void testSelectById_isDirtyLambda() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        user.setName("Lambda Test");

        assertTrue("isDirty(User::getName) 应为 true", user.isDirty(User::getName));
        assertFalse("isDirty(User::getEmail) 应为 false", user.isDirty(User::getEmail));
    }

    @Test
    public void testSelectById_getOriginalLambda() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();

        user.setName("Lambda Original Test");

        String original = user.getOriginal(User::getName);
        assertEquals("Lambda getOriginal 应返回原始名称", originalName, original);
    }

    @Test
    public void testSelectById_relationFieldNotTracked() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        Map<String, Object> original = user.getOriginal();
        assertFalse("关系字段 phone 不应在原始快照中", original.containsKey("phone"));
        assertFalse("关系字段 profile 不应在原始快照中", original.containsKey("profile"));
        assertFalse("关系字段 posts 不应在原始快照中", original.containsKey("posts"));
        assertFalse("关系字段 roles 不应在原始快照中", original.containsKey("roles"));
    }

    @Test
    public void testSelectById_noSyncOriginal_noTracking() {
        User user = findById(User.class, 1L);

        user.setName("No Tracking");

        assertFalse("未 syncOriginal 时 isDirty 应为 false", user.isDirty());
        assertTrue("未 syncOriginal 时 getDirty 应为空", user.getDirty().isEmpty());
        assertNull("未 syncOriginal 时 getOriginal(field) 应返回 null", user.getOriginal("name"));
        assertTrue("未 syncOriginal 时 getOriginal() 应返回空 Map", user.getOriginal().isEmpty());
    }

    // ==================== P3-03: new 创建实体的追踪 ====================

    @Test
    public void testNewEntity_beforeSave_notTracking() {
        User user = new User();
        user.setName("New User");
        user.setEmail("new@test.com");

        assertFalse("new 实体 save 前 isDirty 应为 false（未追踪）", user.isDirty());
        assertTrue("new 实体 save 前 getDirty 应为空", user.getDirty().isEmpty());
    }

    @Test
    public void testNewEntity_save_wasChanged() {
        User user = new User();
        user.setName("Brand New User");
        user.setEmail("brandnew@test.com");

        user.save();

        assertFalse("新实体首次 save 后 isDirty 应为 false", user.isDirty());
    }

    @Test
    public void testNewEntity_save_thenModify_isDirty() {
        User user = new User();
        user.setName("Auto Tracking User");
        user.setEmail("auto@test.com");

        user.save();

        assertFalse("save 后未修改时 isDirty 应为 false", user.isDirty());

        user.setName("Modified After Save");
        assertTrue("save 后修改 isDirty 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));

        assertEquals("getOriginal 应返回 save 时的值", "Auto Tracking User", user.getOriginal("name"));
    }

    @Test
    public void testNewEntity_doubleSave_wasChanged() {
        User user = new User();
        user.setName("Double Save User");
        user.setEmail("double@test.com");

        user.save();

        user.setName("Double Save Updated");
        user.save();

        assertTrue("第二次 save 后 wasChanged 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("Double Save Updated", changes.get("name"));
    }

    @Test
    public void testNewEntity_manualSyncOriginal_isDirty() {
        User user = new User();
        user.setName("Manual Sync");
        user.setEmail("sync@test.com");

        user.syncOriginal();

        user.setName("Modified After Sync");
        assertTrue("syncOriginal 后修改 isDirty 应为 true", user.isDirty());
        assertEquals("getOriginal 应返回 sync 时的值", "Manual Sync", user.getOriginal("name"));
    }

    // ==================== 补充测试 ====================

    @Test
    public void testSyncOriginalPartialFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();

        user.setName("Partial Sync Name");
        user.setEmail("partial@test.com");

        assertTrue("name 应为 dirty", user.isDirty("name"));
        assertTrue("email 应为 dirty", user.isDirty("email"));

        user.syncOriginal("name");

        assertFalse("name 同步后不应为 dirty", user.isDirty("name"));
        assertTrue("email 未同步仍应为 dirty", user.isDirty("email"));
    }

    @Test
    public void testMultipleModifications_finalState() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();

        user.setName("First");
        user.setName("Second");
        user.setName("Third");

        assertTrue("多次修改后 isDirty 应为 true", user.isDirty("name"));
        Map<String, Object> dirty = user.getDirty();
        assertEquals("getDirty 应返回最终值", "Third", dirty.get("name"));
        assertEquals("getOriginal 应返回初始原始值", originalName, user.getOriginal("name"));
    }
}
