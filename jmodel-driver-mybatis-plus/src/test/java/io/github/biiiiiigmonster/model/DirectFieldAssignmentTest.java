package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 直接字段赋值测试 (Phase 4 - P4-03)
 * <p>
 * 验证通过直接字段赋值（绕过 setter）修改的字段能在 save() 时
 * 被 {@code detectUntrackedChanges()} 快照对比机制检测到。
 * <p>
 * 由于 Java 的访问控制，测试中使用 {@link java.lang.reflect.Field#set} 模拟直接字段赋值。
 */
public class DirectFieldAssignmentTest extends BaseTest {

    /**
     * 直接字段赋值在 save() 时被检测到
     */
    @Test
    public void testDirectFieldAssignment_detectedOnSave() throws Exception {
        User user = userMapper.selectById(1L);
        assertNotNull(user);

        user.syncOriginal();

        // 通过反射直接设置字段值，绕过增强后的 setter
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(user, "DirectAssignValue");

        // setter 拦截不会触发，所以 isDirty 可能为 false
        // 但 save() 时的 detectUntrackedChanges() 应能检测到
        user.save();

        // save 后 wasChanged 应检测到直接赋值的变更
        assertTrue("直接字段赋值后 save，wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("getChanges 应包含直接赋值的值", "DirectAssignValue", changes.get("name"));
    }

    /**
     * 直接字段赋值 - 多个字段同时修改
     */
    @Test
    public void testDirectFieldAssignment_multipleFields() throws Exception {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 直接赋值 name 和 email
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(user, "DirectName");

        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(user, "direct@email.com");

        user.save();

        assertTrue("wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertTrue("wasChanged(\"email\") 应为 true", user.wasChanged("email"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("DirectName", changes.get("name"));
        assertEquals("direct@email.com", changes.get("email"));
    }

    /**
     * 直接字段赋值与 setter 混合使用
     */
    @Test
    public void testDirectFieldAssignment_mixedWithSetter() throws Exception {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // setter 修改 name（被拦截追踪）
        user.setName("SetterName");

        // 直接赋值 email（绕过 setter）
        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(user, "direct@email.com");

        // name 应该已经被 setter 追踪
        assertTrue("setter 修改的 name 应为 dirty", user.isDirty("name"));

        user.save();

        // 两个字段的变更都应被记录
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertTrue("wasChanged(\"email\") 应为 true", user.wasChanged("email"));
    }

    /**
     * 直接字段赋值为相同值 - 不应产生变更
     */
    @Test
    public void testDirectFieldAssignment_sameValue_noChange() throws Exception {
        User user = userMapper.selectById(1L);
        String currentName = user.getName();
        user.syncOriginal();

        // 直接赋值为相同值
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(user, currentName);

        user.save();

        // 相同值不应产生变更
        assertFalse("相同值直接赋值不应产生 wasChanged(\"name\")", user.wasChanged("name"));
    }

    /**
     * 直接字段赋值 null → 有值
     */
    @Test
    public void testDirectFieldAssignment_nullToValue() throws Exception {
        // 先创建一个 email 为 null 的用户并保存（获得自增 id）
        User user = new User();
        user.setName("Direct Null Test");
        // email 为 null
        user.save();

        // save 后自动进入追踪状态，此时 email 原始值为 null

        // 直接赋值 email（绕过 setter）
        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(user, "fromNull@test.com");

        user.save();

        assertTrue("null → 有值的直接赋值应被检测", user.wasChanged("email"));
        assertEquals("fromNull@test.com", user.getChanges().get("email"));
    }
}
