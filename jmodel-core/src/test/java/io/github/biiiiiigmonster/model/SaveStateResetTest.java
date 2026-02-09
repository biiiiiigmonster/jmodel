package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class SaveStateResetTest extends BaseTest {

    @Test
    public void testSave_resetsIsDirty() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Before Save");
        assertTrue(user.isDirty());
        user.save();
        assertFalse(user.isDirty());
        assertFalse(user.isDirty("name"));
        assertTrue(user.getDirty().isEmpty());
    }

    @Test
    public void testSave_populatesWasChanged() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Save Changed Name");
        user.save();
        assertTrue(user.wasChanged());
        assertTrue(user.wasChanged("name"));
        assertFalse(user.wasChanged("email"));
    }

    @Test
    public void testSave_populatesGetChanges() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Changes Name");
        user.setEmail("changes@test.com");
        user.save();
        Map<String, Object> changes = user.getChanges();
        assertEquals(2, changes.size());
        assertEquals("Changes Name", changes.get("name"));
        assertEquals("changes@test.com", changes.get("email"));
    }

    @Test
    public void testSave_enablesTrackingAutomatically() {
        User user = new User();
        user.setName("Auto Track User");
        user.setEmail("auto@test.com");
        assertFalse(user.isDirty());
        user.save();
        assertFalse(user.isDirty());
        user.setName("Modified After Save");
        assertTrue(user.isDirty());
        assertTrue(user.isDirty("name"));
    }

    @Test
    public void testSave_afterSave_setterCallIsTracked() {
        User user = new User();
        user.setName("Original Save Name");
        user.setEmail("original@save.com");
        user.save();
        user.setName("After Save Name");
        assertTrue(user.isDirty("name"));
        assertEquals("Original Save Name", user.getOriginal("name"));
    }

    @Test
    public void testSave_existingEntity_originalUpdated() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Updated Name");
        user.save();
        assertEquals("Updated Name", user.getOriginal("name"));
        user.setName("Second Change");
        assertTrue(user.isDirty("name"));
        assertEquals("Updated Name", user.getOriginal("name"));
    }

    @Test
    public void testSave_secondSave_overridesWasChanged() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("First Save");
        user.save();
        assertTrue(user.wasChanged());
        assertEquals("First Save", user.getChanges().get("name"));
        user.setEmail("second@save.com");
        user.save();
        assertTrue(user.wasChanged());
        assertTrue(user.wasChanged("email"));
        assertFalse(user.wasChanged("name"));
        assertEquals("second@save.com", user.getChanges().get("email"));
    }

    @Test
    public void testSave_noChanges_wasChangedFalse() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.save();
        assertFalse(user.wasChanged());
        assertTrue(user.getChanges().isEmpty());
    }

    @Test
    public void testSave_newEntity_isDirtyFalseAfterSave() {
        User user = new User();
        user.setName("Brand New");
        user.setEmail("brandnew@test.com");
        user.save();
        assertFalse(user.isDirty());
    }

    @Test
    public void testSave_newEntity_doubleSave() {
        User user = new User();
        user.setName("New Entity");
        user.setEmail("new@entity.com");
        user.save();
        user.setName("Updated Entity");
        user.save();
        assertTrue(user.wasChanged());
        assertTrue(user.wasChanged("name"));
        assertEquals("Updated Entity", user.getChanges().get("name"));
    }

    @Test
    public void testSave_originalMatchesCurrentValues() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Consistent Name");
        user.setEmail("consistent@test.com");
        user.save();
        assertEquals("Consistent Name", user.getOriginal("name"));
        assertEquals("consistent@test.com", user.getOriginal("email"));
        assertEquals(user.getName(), user.getOriginal("name"));
        assertEquals(user.getEmail(), user.getOriginal("email"));
    }
}
