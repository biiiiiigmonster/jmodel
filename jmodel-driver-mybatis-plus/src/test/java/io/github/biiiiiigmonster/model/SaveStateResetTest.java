package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Save 后状态重置测试 (Phase 4 - P4-05)
 * <p>
 * 验证 save() 方法对 dirty-tracking 状态的正确管理：
 * <ul>
 *   <li>save 成功后 isDirty 重置为 false</li>
 *   <li>save 成功后 wasChanged / getChanges 正确记录本次变更</li>
 *   <li>save 成功后自动进入 TRACKING 状态</li>
 *   <li>新实体首次 save 后自动启用追踪</li>
 *   <li>连续多次 save 的状态正确性</li>
 * </ul>
 */
public class SaveStateResetTest extends BaseTest {

    // ==================== save 后 isDirty 重置 ====================

    /**
     * save 成功后 isDirty 应重置为 false
     */
    @Test
    public void testSave_resetsIsDirty() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Before Save");
        assertTrue("save 前 isDirty 应为 true", user.isDirty());

        user.save();

        assertFalse("save 后 isDirty 应为 false", user.isDirty());
        assertFalse("save 后 isDirty(\"name\") 应为 false", user.isDirty("name"));
        assertTrue("save 后 getDirty 应为空", user.getDirty().isEmpty());
    }

    // ==================== save 后 wasChanged / getChanges ====================

    /**
     * save 成功后 wasChanged 返回 true，getChanges 包含变更
     */
    @Test
    public void testSave_populatesWasChanged() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Save Changed Name");
        user.save();

        assertTrue("save 后 wasChanged() 应为 true", user.wasChanged());
        assertTrue("save 后 wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertFalse("save 后 wasChanged(\"email\") 应为 false", user.wasChanged("email"));
    }

    /**
     * save 成功后 getChanges 包含所有本次保存的变更字段
     */
    @Test
    public void testSave_populatesGetChanges() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Changes Name");
        user.setEmail("changes@test.com");
        user.save();

        Map<String, Object> changes = user.getChanges();
        assertEquals("getChanges 应有 2 个字段", 2, changes.size());
        assertEquals("Changes Name", changes.get("name"));
        assertEquals("changes@test.com", changes.get("email"));
    }

    // ==================== save 后自动启用追踪 ====================

    /**
     * save 成功后自动进入 TRACKING 状态，后续 setter 被追踪
     */
    @Test
    public void testSave_enablesTrackingAutomatically() {
        User user = new User();
        user.setName("Auto Track User");
        user.setEmail("auto@test.com");

        // save 前不追踪
        assertFalse("save 前不追踪", user.isDirty());

        user.save();

        // save 后自动启用追踪
        assertFalse("save 后未修改，isDirty 应为 false", user.isDirty());

        user.setName("Modified After Save");
        assertTrue("save 后修改 isDirty 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));
    }

    /**
     * save 后 setter 调用被追踪，getOriginal 返回 save 时的值
     */
    @Test
    public void testSave_afterSave_setterCallIsTracked() {
        User user = new User();
        user.setName("Original Save Name");
        user.setEmail("original@save.com");

        user.save();

        // save 后修改
        user.setName("After Save Name");

        assertTrue("save 后 setter 应被追踪", user.isDirty("name"));
        assertEquals("getOriginal 应返回 save 时的值", "Original Save Name", user.getOriginal("name"));
    }

    // ==================== 已存在实体 save 后追踪 ====================

    /**
     * 已存在实体 save 后 original 更新为最新值
     */
    @Test
    public void testSave_existingEntity_originalUpdated() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Updated Name");
        user.save();

        // save 后 original 应更新为 "Updated Name"
        assertEquals("save 后 getOriginal 应为保存后的值", "Updated Name", user.getOriginal("name"));

        // 后续修改相对于新的 original
        user.setName("Second Change");
        assertTrue("isDirty 应为 true", user.isDirty("name"));
        assertEquals("getOriginal 仍为 save 后的值", "Updated Name", user.getOriginal("name"));
    }

    // ==================== 连续多次 save ====================

    /**
     * 连续两次 save，第二次的 wasChanged / getChanges 覆盖第一次
     */
    @Test
    public void testSave_secondSave_overridesWasChanged() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 第一次修改并保存
        user.setName("First Save");
        user.save();

        assertTrue("第一次 save 后 wasChanged 应为 true", user.wasChanged());
        assertEquals("First Save", user.getChanges().get("name"));

        // 第二次修改并保存（只修改 email）
        user.setEmail("second@save.com");
        user.save();

        // wasChanged 应反映第二次 save 的变更
        assertTrue("第二次 save 后 wasChanged 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"email\") 应为 true", user.wasChanged("email"));
        assertFalse("wasChanged(\"name\") 应为 false（第二次未修改 name）", user.wasChanged("name"));
        assertEquals("second@save.com", user.getChanges().get("email"));
    }

    /**
     * 无变更时 save，wasChanged 应为 false
     */
    @Test
    public void testSave_noChanges_wasChangedFalse() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 不做任何修改直接 save
        user.save();

        assertFalse("无变更 save 后 wasChanged 应为 false", user.wasChanged());
        assertTrue("无变更 save 后 getChanges 应为空", user.getChanges().isEmpty());
    }

    // ==================== 新实体 save 后的状态 ====================

    /**
     * 新实体首次 save 后 isDirty 为 false
     */
    @Test
    public void testSave_newEntity_isDirtyFalseAfterSave() {
        User user = new User();
        user.setName("Brand New");
        user.setEmail("brandnew@test.com");

        user.save();

        assertFalse("新实体首次 save 后 isDirty 应为 false", user.isDirty());
    }

    /**
     * 新实体 save → 修改 → 再 save → wasChanged 正确
     */
    @Test
    public void testSave_newEntity_doubleSave() {
        User user = new User();
        user.setName("New Entity");
        user.setEmail("new@entity.com");

        // 第一次 save
        user.save();

        // 修改并第二次 save
        user.setName("Updated Entity");
        user.save();

        assertTrue("第二次 save 后 wasChanged 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertEquals("Updated Entity", user.getChanges().get("name"));
    }

    // ==================== save 后 syncOriginal 的一致性 ====================

    /**
     * save 后 getOriginal 与实际字段值一致
     */
    @Test
    public void testSave_originalMatchesCurrentValues() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Consistent Name");
        user.setEmail("consistent@test.com");
        user.save();

        // save 后 original 应与当前值一致
        assertEquals("Consistent Name", user.getOriginal("name"));
        assertEquals("consistent@test.com", user.getOriginal("email"));
        assertEquals("name 当前值应与 original 一致", user.getName(), user.getOriginal("name"));
        assertEquals("email 当前值应与 original 一致", user.getEmail(), user.getOriginal("email"));
    }
}
