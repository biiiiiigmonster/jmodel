package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

public class DirectFieldAssignmentTest extends BaseTest {

    @Test
    public void testDirectFieldAssignment_detectedOnSave() throws Exception {
        User user = findById(User.class, 1L);
        assertNotNull(user);
        user.syncOriginal();
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(user, "DirectAssignValue");
        user.save();
        assertTrue("直接字段赋值后 save，wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        Map<String, Object> changes = user.getChanges();
        assertEquals("DirectAssignValue", changes.get("name"));
    }

    @Test
    public void testDirectFieldAssignment_multipleFields() throws Exception {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(user, "DirectName");
        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(user, "direct@email.com");
        user.save();
        assertTrue(user.wasChanged());
        assertTrue(user.wasChanged("name"));
        assertTrue(user.wasChanged("email"));
        Map<String, Object> changes = user.getChanges();
        assertEquals("DirectName", changes.get("name"));
        assertEquals("direct@email.com", changes.get("email"));
    }

    @Test
    public void testDirectFieldAssignment_mixedWithSetter() throws Exception {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("SetterName");
        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(user, "direct@email.com");
        assertTrue("setter 修改的 name 应为 dirty", user.isDirty("name"));
        user.save();
        assertTrue(user.wasChanged("name"));
        assertTrue(user.wasChanged("email"));
    }

    @Test
    public void testDirectFieldAssignment_sameValue_noChange() throws Exception {
        User user = findById(User.class, 1L);
        String currentName = user.getName();
        user.syncOriginal();
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(user, currentName);
        user.save();
        assertFalse(user.wasChanged("name"));
    }

    @Test
    public void testDirectFieldAssignment_nullToValue() throws Exception {
        User user = new User();
        user.setName("Direct Null Test");
        user.save();
        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(user, "fromNull@test.com");
        user.save();
        assertTrue(user.wasChanged("email"));
        assertEquals("fromNull@test.com", user.getChanges().get("email"));
    }
}
