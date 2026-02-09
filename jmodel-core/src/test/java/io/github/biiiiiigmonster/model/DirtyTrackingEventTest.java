package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.listener.DirtyTrackingTestListener;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class DirtyTrackingEventTest extends BaseTest {

    @Before
    public void resetListener() {
        DirtyTrackingTestListener.reset();
    }

    @Test
    public void testUpdatingEvent_canAccessDirtyFields() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        user.setName("Event Test Name");
        user.save();
        assertTrue("updating 事件应被触发", DirtyTrackingTestListener.isUpdatingCaptured());
        assertTrue("updating 事件中 isDirty 应为 true", DirtyTrackingTestListener.isUpdatingIsDirty());
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatingDirtyFields();
        assertNotNull(dirtyFields);
        assertEquals("Event Test Name", dirtyFields.get("name"));
        Map<String, Object> original = DirtyTrackingTestListener.getUpdatingOriginal();
        assertNotNull(original);
        assertEquals(originalName, original.get("name"));
    }

    @Test
    public void testUpdatedEvent_canAccessDirtyFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Updated Event Test");
        user.save();
        assertTrue("updated 事件应被触发", DirtyTrackingTestListener.isUpdatedCaptured());
        assertTrue("updated 事件中 isDirty 应为 true", DirtyTrackingTestListener.isUpdatedIsDirty());
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatedDirtyFields();
        assertNotNull(dirtyFields);
        assertEquals("Updated Event Test", dirtyFields.get("name"));
    }

    @Test
    public void testSavedEvent_canAccessDirtyFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Saved Event Test");
        user.setEmail("saved@event.com");
        user.save();
        assertTrue("saved 事件应被触发", DirtyTrackingTestListener.isSavedCaptured());
        assertTrue("saved 事件中 isDirty 应为 true", DirtyTrackingTestListener.isSavedIsDirty());
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getSavedDirtyFields();
        assertNotNull(dirtyFields);
        assertEquals("Saved Event Test", dirtyFields.get("name"));
        assertEquals("saved@event.com", dirtyFields.get("email"));
    }

    @Test
    public void testAfterSave_wasChangedAndGetChanges() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("After Save Test");
        user.save();
        assertFalse("save 返回后 isDirty 应为 false", user.isDirty());
        assertTrue("save 返回后 wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        Map<String, Object> changes = user.getChanges();
        assertEquals("After Save Test", changes.get("name"));
    }

    @Test
    public void testEventListener_detectSensitiveFieldChanges() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setEmail("sensitive@change.com");
        user.save();
        assertTrue(DirtyTrackingTestListener.isUpdatingCaptured());
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatingDirtyFields();
        assertNotNull(dirtyFields);
        assertTrue(dirtyFields.containsKey("email"));
        assertEquals("sensitive@change.com", dirtyFields.get("email"));
    }

    @Test
    public void testEventListener_multipleFieldChanges() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Multi Event Name");
        user.setEmail("multi@event.com");
        user.save();
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatingDirtyFields();
        assertNotNull(dirtyFields);
        assertEquals(2, dirtyFields.size());
        assertEquals("Multi Event Name", dirtyFields.get("name"));
        assertEquals("multi@event.com", dirtyFields.get("email"));
    }

    @Test
    public void testEventListener_noChanges_isDirtyFalse() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.save();
        assertTrue(DirtyTrackingTestListener.isUpdatingCaptured());
        assertFalse(DirtyTrackingTestListener.isUpdatingIsDirty());
    }
}
