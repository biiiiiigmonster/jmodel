package io.github.biiiiiigmonster.model;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * 反射赋值测试 (Phase 4 - P4-04)
 * <p>
 * 验证通过 {@link cn.hutool.core.util.ReflectUtil#setFieldValue} 等反射方式修改的字段
 * 能在 save() 时被 {@code detectUntrackedChanges()} 快照对比机制检测到。
 */
public class ReflectionAssignmentTest extends BaseTest {

    /**
     * Hutool ReflectUtil 反射赋值在 save() 时被检测到
     */
    @Test
    public void testReflectionAssignment_detectedOnSave() {
        User user = userMapper.selectById(1L);
        assertNotNull(user);
        user.syncOriginal();

        // 使用 Hutool ReflectUtil 反射赋值（绕过增强 setter）
        ReflectUtil.setFieldValue(user, "name", "ReflectValue");

        // 反射赋值不经过增强 setter，isDirty 在 save 前可能为 false
        // 但 save() 中的 detectUntrackedChanges() 应检测到
        user.save();

        assertTrue("反射赋值后 save，wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertEquals("getChanges 应包含反射赋值的值", "ReflectValue", user.getChanges().get("name"));
    }

    /**
     * 反射赋值多个字段
     */
    @Test
    public void testReflectionAssignment_multipleFields_allDetected() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        ReflectUtil.setFieldValue(user, "name", "ReflectName");
        ReflectUtil.setFieldValue(user, "email", "reflect@test.com");

        user.save();

        assertTrue("wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertTrue("wasChanged(\"email\") 应为 true", user.wasChanged("email"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("ReflectName", changes.get("name"));
        assertEquals("reflect@test.com", changes.get("email"));
    }

    /**
     * 反射赋值与 setter 混合使用
     */
    @Test
    public void testReflectionAssignment_mixedWithSetter() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // setter 修改 name
        user.setName("SetterName");
        // 反射修改 email
        ReflectUtil.setFieldValue(user, "email", "reflect@mix.com");

        // name 应已被 setter 追踪
        assertTrue("setter 修改的 name 应为 dirty", user.isDirty("name"));

        user.save();

        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertTrue("wasChanged(\"email\") 应为 true", user.wasChanged("email"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("SetterName", changes.get("name"));
        assertEquals("reflect@mix.com", changes.get("email"));
    }

    /**
     * 反射赋值相同值 - 不应产生变更
     */
    @Test
    public void testReflectionAssignment_sameValue_noChange() {
        User user = userMapper.selectById(1L);
        String currentName = user.getName();
        user.syncOriginal();

        // 反射赋值为相同值
        ReflectUtil.setFieldValue(user, "name", currentName);

        user.save();

        assertFalse("相同值反射赋值不应产生 wasChanged(\"name\")", user.wasChanged("name"));
    }

    /**
     * 反射赋值 Long 类型字段
     */
    @Test
    public void testReflectionAssignment_longField() {
        Post post = postMapper.selectById(1L);
        assertNotNull(post);
        post.syncOriginal();

        ReflectUtil.setFieldValue(post, "userId", 888L);

        post.save();

        assertTrue("反射赋值 Long 字段后 wasChanged 应为 true", post.wasChanged("userId"));
        assertEquals("getChanges 应包含新 userId", 888L, post.getChanges().get("userId"));
    }

    /**
     * 反射赋值有值 → null
     */
    @Test
    public void testReflectionAssignment_valueToNull() {
        User user = userMapper.selectById(1L);
        assertNotNull("初始 email 不应为 null", user.getEmail());
        user.syncOriginal();

        ReflectUtil.setFieldValue(user, "email", null);

        user.save();

        assertTrue("有值 → null 反射赋值应被检测", user.wasChanged("email"));
        assertNull("getChanges 中 email 应为 null", user.getChanges().get("email"));
    }

    /**
     * 未追踪状态下反射赋值 - save 时的行为
     * <p>
     * 新实体首次 save 时，save 内部会先 syncOriginal（此时字段已有反射赋的值），
     * 然后 detectUntrackedChanges 不会发现差异
     */
    @Test
    public void testReflectionAssignment_untrackedState_saveDetection() {
        User user = userMapper.selectById(1L);
        // 不调用 syncOriginal

        ReflectUtil.setFieldValue(user, "name", "UntrackedReflect");

        // save 时：wasUntracked=true → syncOriginal() → detectUntrackedChanges() → 无差异
        // 因为 syncOriginal 时字段已经是 "UntrackedReflect"
        user.save();

        // save 后进入追踪状态，可以追踪后续变更
        user.setName("AfterSave");
        assertTrue("save 后修改应被追踪", user.isDirty("name"));
        assertEquals("getOriginal 应为 save 后同步的值", "UntrackedReflect", user.getOriginal("name"));
    }
}
