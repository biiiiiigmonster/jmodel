package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Setter 拦截测试 (Phase 4 - P4-02)
 * <p>
 * 验证 ByteBuddy 增强后的 setter 方法能正确触发 {@code $jmodel$trackChange}，
 * 覆盖不同字段类型和各种赋值场景。
 * <p>
 * 测试场景：
 * <ul>
 *   <li>String 类型字段拦截</li>
 *   <li>Long 包装类型字段拦截</li>
 *   <li>多字段同时拦截</li>
 *   <li>恢复原值后移除变更标记</li>
 *   <li>null → 有值 / 有值 → null 变更</li>
 *   <li>相同值赋值不产生脏数据</li>
 *   <li>不同实体类型的 setter 拦截</li>
 * </ul>
 */
public class SetterInterceptionTest extends BaseTest {

    // ==================== String 类型 setter 拦截 ====================

    /**
     * 验证 String 类型字段的 setter 拦截追踪
     */
    @Test
    public void testSetterInterception_tracksStringField() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Intercepted Name");

        assertTrue("String 字段 setter 拦截后 isDirty 应为 true", user.isDirty("name"));
        assertEquals("getDirty 应包含新值", "Intercepted Name", user.getDirty().get("name"));
    }

    /**
     * 验证 email (String) 字段的 setter 拦截追踪
     */
    @Test
    public void testSetterInterception_tracksEmailField() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setEmail("intercepted@test.com");

        assertTrue("email setter 拦截后 isDirty 应为 true", user.isDirty("email"));
        assertEquals("intercepted@test.com", user.getDirty().get("email"));
    }

    // ==================== Long 类型 setter 拦截 ====================

    /**
     * 验证 Long 包装类型字段的 setter 拦截追踪
     * <p>
     * Post 实体有 Long userId 字段，用于测试 Long 类型
     */
    @Test
    public void testSetterInterception_tracksLongField() {
        Post post = postMapper.selectById(1L);
        assertNotNull(post);

        post.syncOriginal();

        Long originalUserId = post.getUserId();
        post.setUserId(999L);

        assertTrue("Long 字段 setter 拦截后 isDirty 应为 true", post.isDirty("userId"));
        assertEquals("getDirty 应包含新 userId", 999L, post.getDirty().get("userId"));
        assertEquals("getOriginal 应返回原始 userId", originalUserId, post.getOriginal("userId"));
    }

    // ==================== 多字段 setter 拦截 ====================

    /**
     * 验证多个字段同时被 setter 拦截追踪
     */
    @Test
    public void testSetterInterception_tracksMultipleFields() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Multi Field Name");
        user.setEmail("multi@field.com");

        assertTrue("name isDirty 应为 true", user.isDirty("name"));
        assertTrue("email isDirty 应为 true", user.isDirty("email"));

        Map<String, Object> dirty = user.getDirty();
        assertEquals("脏字段数量应为 2", 2, dirty.size());
        assertEquals("Multi Field Name", dirty.get("name"));
        assertEquals("multi@field.com", dirty.get("email"));
    }

    // ==================== 恢复原值 ====================

    /**
     * 验证恢复为原始值后，变更标记被移除
     */
    @Test
    public void testSetterInterception_revertToOriginal_removesFromChanges() {
        User user = userMapper.selectById(1L);
        String originalName = user.getName();
        user.syncOriginal();

        // 修改
        user.setName("Temporary Change");
        assertTrue("修改后 isDirty 应为 true", user.isDirty("name"));

        // 恢复原值
        user.setName(originalName);
        assertFalse("恢复原值后 isDirty 应为 false", user.isDirty("name"));
        assertTrue("恢复原值后 getDirty 应为空", user.getDirty().isEmpty());
    }

    // ==================== null 值变更 ====================

    /**
     * 验证 null → 有值 的变更被追踪
     */
    @Test
    public void testSetterInterception_nullToValue() {
        // 先创建用户并保存，email 为 null
        User user = new User();
        user.setName("Test User");
        // email 未设置，默认为 null
        user.save();

        // save 后自动进入追踪状态，email 原始值为 null
        assertNull("email 原始值应为 null", user.getOriginal("email"));

        user.setEmail("nonnull@test.com");
        assertTrue("null → 有值 应为 dirty", user.isDirty("email"));
        assertEquals("nonnull@test.com", user.getDirty().get("email"));
    }

    /**
     * 验证 有值 → null 的变更被追踪
     */
    @Test
    public void testSetterInterception_valueToNull() {
        User user = userMapper.selectById(1L);
        assertNotNull("初始 email 不应为 null", user.getEmail());

        user.syncOriginal();
        user.setEmail(null);

        assertTrue("有值 → null 应为 dirty", user.isDirty("email"));
        assertNull("getDirty 中 email 应为 null", user.getDirty().get("email"));
    }

    // ==================== 相同值赋值 ====================

    /**
     * 验证设置相同值不产生脏数据
     */
    @Test
    public void testSetterInterception_sameValue_noDirty() {
        User user = userMapper.selectById(1L);
        String currentName = user.getName();
        user.syncOriginal();

        // 设置相同值
        user.setName(currentName);

        assertFalse("设置相同值不应产生脏数据", user.isDirty("name"));
        assertTrue("设置相同值后 getDirty 应为空", user.getDirty().isEmpty());
    }

    /**
     * 验证 null → null 不产生脏数据
     */
    @Test
    public void testSetterInterception_nullToNull_noDirty() {
        // 先创建用户并保存，email 为 null
        User user = new User();
        user.setName("Test");
        // email 为 null
        user.save();

        // save 后自动进入追踪状态
        user.setEmail(null);

        assertFalse("null → null 不应产生脏数据", user.isDirty("email"));
    }

    // ==================== 不同实体类型 setter 拦截 ====================

    /**
     * 验证 Post 实体的 setter 拦截也能正确工作
     */
    @Test
    public void testSetterInterception_differentEntityType() {
        Post post = postMapper.selectById(1L);
        assertNotNull(post);

        String originalTitle = post.getTitle();
        post.syncOriginal();

        post.setTitle("Intercepted Title");

        assertTrue("Post.title setter 拦截后 isDirty 应为 true", post.isDirty("title"));
        assertEquals("Intercepted Title", post.getDirty().get("title"));
        assertEquals("getOriginal 应返回原始 title", originalTitle, post.getOriginal("title"));
    }

    // ==================== 未追踪状态下 setter 不触发追踪 ====================

    /**
     * 验证未追踪状态下增强 setter 不触发追踪
     */
    @Test
    public void testSetterInterception_untrackedState_noEffect() {
        User user = userMapper.selectById(1L);

        // 不调用 syncOriginal，直接调用 setter
        user.setName("Should Not Track");
        user.setEmail("notrack@test.com");

        assertFalse("未追踪状态下 isDirty 应为 false", user.isDirty());
        assertTrue("未追踪状态下 getDirty 应为空", user.getDirty().isEmpty());
    }
}
