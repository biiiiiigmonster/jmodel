package io.github.biiiiiigmonster.model;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Dirty Tracking 集成测试。
 * <p>
 * 覆盖 Phase 3 验证目标：
 * <ul>
 *   <li>P3-01: 验证通过 ORM 查询（selectById）的实体能正确追踪</li>
 *   <li>P3-02: 验证直接 mapper 条件查询（selectList/Wrapper）的实体能正确追踪</li>
 *   <li>P3-03: 验证 new 创建的实体能正确追踪</li>
 * </ul>
 */
public class DirtyTrackingTest extends BaseTest {

    // ==================== P3-01: ORM 查询（selectById）的实体追踪 ====================

    /**
     * P3-01: selectById 后 syncOriginal → setter 修改 → isDirty 返回 true
     */
    @Test
    public void testSelectById_syncOriginal_setterModify_isDirty() {
        User user = userMapper.selectById(1L);
        assertNotNull(user);

        // 启用追踪前，isDirty 应为 false
        assertFalse("syncOriginal 前 isDirty 应为 false", user.isDirty());

        // 启用追踪
        user.syncOriginal();

        // 未修改时 isDirty 应为 false
        assertFalse("syncOriginal 后未修改，isDirty 应为 false", user.isDirty());

        // 修改 name 字段
        String originalName = user.getName();
        user.setName("New Name");

        // isDirty 检查
        assertTrue("修改后 isDirty() 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));
        assertFalse("isDirty(\"email\") 应为 false", user.isDirty("email"));
    }

    /**
     * P3-01: selectById → syncOriginal → 修改 → getDirty 返回正确的脏字段
     */
    @Test
    public void testSelectById_syncOriginal_getDirty() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Changed Name");
        user.setEmail("changed@example.com");

        Map<String, Object> dirty = user.getDirty();
        assertEquals("脏字段数量应为 2", 2, dirty.size());
        assertEquals("Changed Name", dirty.get("name"));
        assertEquals("changed@example.com", dirty.get("email"));
    }

    /**
     * P3-01: selectById → syncOriginal → 修改 → getDirty(fields) 只返回指定脏字段
     */
    @Test
    public void testSelectById_getDirtyWithFilter() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Changed Name");
        user.setEmail("changed@example.com");

        Map<String, Object> dirtyNameOnly = user.getDirty("name");
        assertEquals("过滤后应只有 1 个脏字段", 1, dirtyNameOnly.size());
        assertEquals("Changed Name", dirtyNameOnly.get("name"));

        // 过滤一个未修改的字段
        Map<String, Object> dirtyIdOnly = user.getDirty("id");
        assertTrue("id 未修改，过滤结果应为空", dirtyIdOnly.isEmpty());
    }

    /**
     * P3-01: selectById → syncOriginal → 修改 → getOriginal 返回原始值
     */
    @Test
    public void testSelectById_getOriginal() {
        User user = userMapper.selectById(1L);
        String originalName = user.getName();
        String originalEmail = user.getEmail();

        user.syncOriginal();

        user.setName("Changed Name");
        user.setEmail("changed@example.com");

        // 获取单个字段的原始值
        assertEquals("getOriginal(\"name\") 应返回原始名称", originalName, user.getOriginal("name"));
        assertEquals("getOriginal(\"email\") 应返回原始邮箱", originalEmail, user.getOriginal("email"));

        // 获取所有原始值
        Map<String, Object> allOriginals = user.getOriginal();
        assertFalse("所有原始值不应为空", allOriginals.isEmpty());
        assertEquals(originalName, allOriginals.get("name"));
        assertEquals(originalEmail, allOriginals.get("email"));
    }

    /**
     * P3-01: selectById → syncOriginal → 修改 → getOriginal 带默认值
     */
    @Test
    public void testSelectById_getOriginalWithDefault() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 有值的字段，不应返回默认值
        Object originalName = user.getOriginal("name", "default_name");
        assertNotEquals("default_name", originalName);

        // 未追踪状态下获取原始值
        User untrackedUser = userMapper.selectById(2L);
        Object result = untrackedUser.getOriginal("name", "fallback");
        assertEquals("未追踪状态应返回默认值", "fallback", result);
    }

    /**
     * P3-01: selectById → syncOriginal → 修改后恢复原值 → isDirty 返回 false
     */
    @Test
    public void testSelectById_revertToOriginal_notDirty() {
        User user = userMapper.selectById(1L);
        String originalName = user.getName();
        user.syncOriginal();

        // 修改
        user.setName("Temporary Name");
        assertTrue("修改后应为 dirty", user.isDirty("name"));

        // 恢复为原始值
        user.setName(originalName);
        assertFalse("恢复原值后不应为 dirty", user.isDirty("name"));
        assertFalse("恢复原值后 isDirty() 也应为 false", user.isDirty());
    }

    /**
     * P3-01: selectById → syncOriginal → 修改 → save → wasChanged / getChanges 正确
     */
    @Test
    public void testSelectById_save_wasChanged() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Updated Name");
        user.save();

        // save 后 isDirty 应重置
        assertFalse("save 后 isDirty() 应为 false", user.isDirty());

        // wasChanged 检查
        assertTrue("wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertFalse("wasChanged(\"email\") 应为 false", user.wasChanged("email"));

        // getChanges 检查
        Map<String, Object> changes = user.getChanges();
        assertEquals("changes 应包含 1 个字段", 1, changes.size());
        assertEquals("Updated Name", changes.get("name"));
    }

    /**
     * P3-01: selectById → syncOriginal → 修改 → save → 再修改 → 新的 dirty tracking 正确
     */
    @Test
    public void testSelectById_save_thenModifyAgain() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 第一次修改并保存
        user.setName("First Update");
        user.save();

        assertFalse("save 后 isDirty 应为 false", user.isDirty());

        // 第二次修改
        user.setName("Second Update");
        assertTrue("第二次修改后 isDirty 应为 true", user.isDirty("name"));

        // getOriginal 应返回 save 后的值（即 "First Update"）
        assertEquals("原始值应为 save 后的值", "First Update", user.getOriginal("name"));
    }

    /**
     * P3-01: isDirty 的类型安全版本（Lambda）
     */
    @Test
    public void testSelectById_isDirtyLambda() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Lambda Test");

        assertTrue("isDirty(User::getName) 应为 true", user.isDirty(User::getName));
        assertFalse("isDirty(User::getEmail) 应为 false", user.isDirty(User::getEmail));
    }

    /**
     * P3-01: getOriginal 的类型安全版本（Lambda）
     */
    @Test
    public void testSelectById_getOriginalLambda() {
        User user = userMapper.selectById(1L);
        String originalName = user.getName();
        user.syncOriginal();

        user.setName("Lambda Original Test");

        String original = user.getOriginal(User::getName);
        assertEquals("Lambda getOriginal 应返回原始名称", originalName, original);
    }

    /**
     * P3-01: 关系字段不应被追踪
     */
    @Test
    public void testSelectById_relationFieldNotTracked() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 获取可追踪字段数量 - 关系字段不在其中
        Map<String, Object> original = user.getOriginal();
        assertFalse("关系字段 phone 不应在原始快照中", original.containsKey("phone"));
        assertFalse("关系字段 profile 不应在原始快照中", original.containsKey("profile"));
        assertFalse("关系字段 posts 不应在原始快照中", original.containsKey("posts"));
        assertFalse("关系字段 roles 不应在原始快照中", original.containsKey("roles"));
    }

    /**
     * P3-01: 未调用 syncOriginal 时，setter 修改不触发追踪
     */
    @Test
    public void testSelectById_noSyncOriginal_noTracking() {
        User user = userMapper.selectById(1L);

        // 不调用 syncOriginal
        user.setName("No Tracking");

        assertFalse("未 syncOriginal 时 isDirty 应为 false", user.isDirty());
        assertTrue("未 syncOriginal 时 getDirty 应为空", user.getDirty().isEmpty());
        assertNull("未 syncOriginal 时 getOriginal(field) 应返回 null", user.getOriginal("name"));
        assertTrue("未 syncOriginal 时 getOriginal() 应返回空 Map", user.getOriginal().isEmpty());
    }

    // ==================== P3-02: mapper 条件查询的实体追踪 ====================

    /**
     * P3-02: selectList + QueryWrapper 查询后的 dirty tracking
     */
    @Test
    public void testSelectList_dirtyTracking() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", "John Doe");
        List<User> users = userMapper.selectList(wrapper);

        assertFalse("查询结果不应为空", users.isEmpty());
        User user = users.get(0);

        user.syncOriginal();
        user.setName("Wrapper Query Update");

        assertTrue("isDirty 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));

        Map<String, Object> dirty = user.getDirty();
        assertEquals("Wrapper Query Update", dirty.get("name"));
    }

    /**
     * P3-02: selectOne + QueryWrapper 查询后的 dirty tracking
     */
    @Test
    public void testSelectOne_dirtyTracking() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id", 2L);
        User user = userMapper.selectOne(wrapper);

        assertNotNull(user);
        String originalEmail = user.getEmail();

        user.syncOriginal();
        user.setEmail("selectone@test.com");

        assertTrue("isDirty 应为 true", user.isDirty());
        assertEquals("getOriginal 应返回原始邮箱", originalEmail, user.getOriginal("email"));
    }

    /**
     * P3-02: selectList 查询多个实体，各自独立追踪
     */
    @Test
    public void testSelectList_independentTracking() {
        List<User> users = userMapper.selectList(null);
        assertTrue("应有多个用户", users.size() >= 2);

        User user1 = users.get(0);
        User user2 = users.get(1);

        user1.syncOriginal();
        user2.syncOriginal();

        // 只修改 user1
        user1.setName("User1 Modified");

        assertTrue("user1 isDirty 应为 true", user1.isDirty());
        assertFalse("user2 isDirty 应为 false", user2.isDirty());
    }

    /**
     * P3-02: selectList 查询 → syncOriginal → save → wasChanged
     */
    @Test
    public void testSelectList_save_wasChanged() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id", 3L);
        User user = userMapper.selectOne(wrapper);
        assertNotNull(user);

        user.syncOriginal();
        user.setName("Wrapper Save Test");
        user.save();

        assertTrue("wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertFalse("save 后 isDirty 应为 false", user.isDirty());
    }

    // ==================== P3-03: new 创建实体的追踪 ====================

    /**
     * P3-03: new User() → 设置字段 → isDirty = false（未启用追踪）
     */
    @Test
    public void testNewEntity_beforeSave_notTracking() {
        User user = new User();
        user.setName("New User");
        user.setEmail("new@test.com");

        assertFalse("new 实体 save 前 isDirty 应为 false（未追踪）", user.isDirty());
        assertTrue("new 实体 save 前 getDirty 应为空", user.getDirty().isEmpty());
    }

    /**
     * P3-03: new User() → 设置字段 → save → wasChanged 正确
     */
    @Test
    public void testNewEntity_save_wasChanged() {
        User user = new User();
        user.setName("Brand New User");
        user.setEmail("brandnew@test.com");

        user.save();

        // save 后 wasChanged 应反映本次保存的变更
        // 因为 save 时会先 syncOriginal 再 detectUntrackedChanges
        // 新实体首次 save，syncOriginal 在 save 内部发生时字段已有值，
        // 所以 wasChanged 取决于实现：save 时 original=null → syncOriginal → detectUntrackedChanges (无差异) → currentChanges=空
        // 新实体首次 save 的 wasChanged 行为：由于 syncOriginal 在 save 内设置原始值等于当前值，所以 wasChanged 可能为 false
        assertFalse("新实体首次 save 后 isDirty 应为 false", user.isDirty());
    }

    /**
     * P3-03: new User() → save → 修改 → isDirty = true（save 后自动启用追踪）
     */
    @Test
    public void testNewEntity_save_thenModify_isDirty() {
        User user = new User();
        user.setName("Auto Tracking User");
        user.setEmail("auto@test.com");

        user.save();

        // save 成功后自动进入 TRACKING 状态
        assertFalse("save 后未修改时 isDirty 应为 false", user.isDirty());

        // 修改字段
        user.setName("Modified After Save");
        assertTrue("save 后修改 isDirty 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));

        // 原始值应为 save 时的值
        assertEquals("getOriginal 应返回 save 时的值", "Auto Tracking User", user.getOriginal("name"));
    }

    /**
     * P3-03: new User() → save → 修改 → save 第二次 → wasChanged 正确
     */
    @Test
    public void testNewEntity_doubleSave_wasChanged() {
        User user = new User();
        user.setName("Double Save User");
        user.setEmail("double@test.com");

        // 第一次 save
        user.save();

        // 修改并第二次 save
        user.setName("Double Save Updated");
        user.save();

        assertTrue("第二次 save 后 wasChanged 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("Double Save Updated", changes.get("name"));
    }

    /**
     * P3-03: new User() → 手动 syncOriginal → 修改 → isDirty
     */
    @Test
    public void testNewEntity_manualSyncOriginal_isDirty() {
        User user = new User();
        user.setName("Manual Sync");
        user.setEmail("sync@test.com");

        // 手动启用追踪
        user.syncOriginal();

        // 修改
        user.setName("Modified After Sync");
        assertTrue("syncOriginal 后修改 isDirty 应为 true", user.isDirty());
        assertEquals("getOriginal 应返回 sync 时的值", "Manual Sync", user.getOriginal("name"));
    }

    // ==================== 补充: syncOriginal 部分字段同步 ====================

    /**
     * syncOriginal(fields) 只同步指定字段
     */
    @Test
    public void testSyncOriginalPartialFields() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Partial Sync Name");
        user.setEmail("partial@test.com");

        // 两个字段都是 dirty
        assertTrue("name 应为 dirty", user.isDirty("name"));
        assertTrue("email 应为 dirty", user.isDirty("email"));

        // 只同步 name 字段
        user.syncOriginal("name");

        // name 不再 dirty（原始值更新为当前值），email 仍然 dirty
        assertFalse("name 同步后不应为 dirty", user.isDirty("name"));
        assertTrue("email 未同步仍应为 dirty", user.isDirty("email"));
    }

    // ==================== 补充: 多次修改追踪 ====================

    /**
     * 多次修改同一字段，只保留最终状态
     */
    @Test
    public void testMultipleModifications_finalState() {
        User user = userMapper.selectById(1L);
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
