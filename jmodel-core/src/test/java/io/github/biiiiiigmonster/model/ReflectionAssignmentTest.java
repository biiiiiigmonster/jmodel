package io.github.biiiiiigmonster.model;

import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ReflectionAssignmentTest extends BaseTest {

    @Test
    public void testReflectionAssignment_detectedOnSave() {
        User user = findById(User.class, 1L);
        assertNotNull(user);
        user.syncOriginal();
        ReflectUtil.setFieldValue(user, "name", "ReflectValue");
        user.save();
        assertTrue(user.wasChanged());
        assertTrue(user.wasChanged("name"));
        assertEquals("ReflectValue", user.getChanges().get("name"));
    }

    @Test
    public void testReflectionAssignment_multipleFields_allDetected() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        ReflectUtil.setFieldValue(user, "name", "ReflectName");
        ReflectUtil.setFieldValue(user, "email", "reflect@test.com");
        user.save();
        assertTrue(user.wasChanged());
        assertTrue(user.wasChanged("name"));
        assertTrue(user.wasChanged("email"));
        Map<String, Object> changes = user.getChanges();
        assertEquals("ReflectName", changes.get("name"));
        assertEquals("reflect@test.com", changes.get("email"));
    }

    @Test
    public void testReflectionAssignment_mixedWithSetter() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("SetterName");
        ReflectUtil.setFieldValue(user, "email", "reflect@mix.com");
        assertTrue(user.isDirty("name"));
        user.save();
        assertTrue(user.wasChanged("name"));
        assertTrue(user.wasChanged("email"));
        Map<String, Object> changes = user.getChanges();
        assertEquals("SetterName", changes.get("name"));
        assertEquals("reflect@mix.com", changes.get("email"));
    }

    @Test
    public void testReflectionAssignment_sameValue_noChange() {
        User user = findById(User.class, 1L);
        String currentName = user.getName();
        user.syncOriginal();
        ReflectUtil.setFieldValue(user, "name", currentName);
        user.save();
        assertFalse(user.wasChanged("name"));
    }

    @Test
    public void testReflectionAssignment_longField() {
        Post post = findById(Post.class, 1L);
        assertNotNull(post);
        post.syncOriginal();
        ReflectUtil.setFieldValue(post, "userId", 888L);
        post.save();
        assertTrue(post.wasChanged("userId"));
        assertEquals(888L, post.getChanges().get("userId"));
    }

    @Test
    public void testReflectionAssignment_valueToNull() {
        User user = findById(User.class, 1L);
        assertNotNull(user.getEmail());
        user.syncOriginal();
        ReflectUtil.setFieldValue(user, "email", null);
        user.save();
        assertTrue(user.wasChanged("email"));
        assertNull(user.getChanges().get("email"));
    }

    @Test
    public void testReflectionAssignment_untrackedState_saveDetection() {
        User user = findById(User.class, 1L);
        ReflectUtil.setFieldValue(user, "name", "UntrackedReflect");
        user.save();
        user.setName("AfterSave");
        assertTrue(user.isDirty("name"));
        assertEquals("UntrackedReflect", user.getOriginal("name"));
    }
}
