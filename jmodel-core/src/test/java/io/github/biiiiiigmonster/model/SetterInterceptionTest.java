package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class SetterInterceptionTest extends BaseTest {

    @Test
    public void testSetterInterception_tracksStringField() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Intercepted Name");
        assertTrue(user.isDirty("name"));
        assertEquals("Intercepted Name", user.getDirty().get("name"));
    }

    @Test
    public void testSetterInterception_tracksEmailField() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setEmail("intercepted@test.com");
        assertTrue(user.isDirty("email"));
        assertEquals("intercepted@test.com", user.getDirty().get("email"));
    }

    @Test
    public void testSetterInterception_tracksLongField() {
        Post post = findById(Post.class, 1L);
        assertNotNull(post);
        post.syncOriginal();
        Long originalUserId = post.getUserId();
        post.setUserId(999L);
        assertTrue(post.isDirty("userId"));
        assertEquals(999L, post.getDirty().get("userId"));
        assertEquals(originalUserId, post.getOriginal("userId"));
    }

    @Test
    public void testSetterInterception_tracksMultipleFields() {
        User user = findById(User.class, 1L);
        user.syncOriginal();
        user.setName("Multi Field Name");
        user.setEmail("multi@field.com");
        assertTrue(user.isDirty("name"));
        assertTrue(user.isDirty("email"));
        Map<String, Object> dirty = user.getDirty();
        assertEquals(2, dirty.size());
        assertEquals("Multi Field Name", dirty.get("name"));
        assertEquals("multi@field.com", dirty.get("email"));
    }

    @Test
    public void testSetterInterception_revertToOriginal_removesFromChanges() {
        User user = findById(User.class, 1L);
        String originalName = user.getName();
        user.syncOriginal();
        user.setName("Temporary Change");
        assertTrue(user.isDirty("name"));
        user.setName(originalName);
        assertFalse(user.isDirty("name"));
        assertTrue(user.getDirty().isEmpty());
    }

    @Test
    public void testSetterInterception_nullToValue() {
        User user = new User();
        user.setName("Test User");
        user.save();
        assertNull(user.getOriginal("email"));
        user.setEmail("nonnull@test.com");
        assertTrue(user.isDirty("email"));
        assertEquals("nonnull@test.com", user.getDirty().get("email"));
    }

    @Test
    public void testSetterInterception_valueToNull() {
        User user = findById(User.class, 1L);
        assertNotNull(user.getEmail());
        user.syncOriginal();
        user.setEmail(null);
        assertTrue(user.isDirty("email"));
        assertNull(user.getDirty().get("email"));
    }

    @Test
    public void testSetterInterception_sameValue_noDirty() {
        User user = findById(User.class, 1L);
        String currentName = user.getName();
        user.syncOriginal();
        user.setName(currentName);
        assertFalse(user.isDirty("name"));
        assertTrue(user.getDirty().isEmpty());
    }

    @Test
    public void testSetterInterception_nullToNull_noDirty() {
        User user = new User();
        user.setName("Test");
        user.save();
        user.setEmail(null);
        assertFalse(user.isDirty("email"));
    }

    @Test
    public void testSetterInterception_differentEntityType() {
        Post post = findById(Post.class, 1L);
        assertNotNull(post);
        String originalTitle = post.getTitle();
        post.syncOriginal();
        post.setTitle("Intercepted Title");
        assertTrue(post.isDirty("title"));
        assertEquals("Intercepted Title", post.getDirty().get("title"));
        assertEquals(originalTitle, post.getOriginal("title"));
    }

    @Test
    public void testSetterInterception_untrackedState_noEffect() {
        User user = findById(User.class, 1L);
        user.setName("Should Not Track");
        user.setEmail("notrack@test.com");
        assertFalse(user.isDirty());
        assertTrue(user.getDirty().isEmpty());
    }
}
