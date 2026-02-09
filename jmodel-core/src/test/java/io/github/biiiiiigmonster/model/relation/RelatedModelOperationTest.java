package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.Profile;
import io.github.biiiiiigmonster.entity.Role;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RelatedModelOperationTest extends BaseTest {

    @Test
    public void shouldSaveHasOneRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Profile profile = new Profile();
        profile.setDescription("Test Profile Description");

        user.associate(User::getProfile, profile);

        User savedUser = findById(User.class, user.getId());
        Profile savedProfile = savedUser.get(User::getProfile);

        assertNotNull(savedProfile);
        assertEquals("Test Profile Description", savedProfile.getDescription());
        assertEquals(user.getId(), savedProfile.getUserId());
    }

    @Test
    public void shouldSaveHasManyRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Post post1 = new Post();
        post1.setTitle("First Post");

        Post post2 = new Post();
        post2.setTitle("Second Post");

        List<Post> posts = Arrays.asList(post1, post2);

        user.associate(User::getPosts, posts);

        User savedUser = findById(User.class, user.getId());
        List<Post> savedPosts = savedUser.get(User::getPosts);

        assertNotNull(savedPosts);
        assertEquals(2, savedPosts.size());
        assertEquals("First Post", savedPosts.get(0).getTitle());
        assertEquals("Second Post", savedPosts.get(1).getTitle());
        assertEquals(user.getId(), savedPosts.get(0).getUserId());
        assertEquals(user.getId(), savedPosts.get(1).getUserId());
    }

    @Test
    public void shouldAttachBelongsToManyRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role1 = new Role();
        role1.setName("admin");
        role1.save();

        Role role2 = new Role();
        role2.setName("user");
        role2.save();

        user.attach(User::getRoles, role1, role2);

        User savedUser = findById(User.class, user.getId());
        List<Role> roles = savedUser.get(User::getRoles);

        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(roles.stream().anyMatch(r -> "user".equals(r.getName())));
    }

    @Test
    public void shouldDetachBelongsToManyRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role1 = new Role();
        role1.setName("admin");
        role1.save();

        Role role2 = new Role();
        role2.setName("user");
        role2.save();

        Role role3 = new Role();
        role3.setName("guest");
        role3.save();

        user.attach(User::getRoles, role1, role2, role3);

        User savedUser = findById(User.class, user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(3, initialRoles.size());

        user.detach(User::getRoles, role1, role2);

        User updatedUser = findById(User.class, user.getId());
        List<Role> remainingRoles = updatedUser.get(User::getRoles);
        assertEquals(1, remainingRoles.size());
        assertEquals("guest", remainingRoles.get(0).getName());
    }

    @Test
    public void shouldSyncBelongsToManyRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role1 = new Role();
        role1.setName("admin");
        role1.save();

        Role role2 = new Role();
        role2.setName("user");
        role2.save();

        Role role3 = new Role();
        role3.setName("guest");
        role3.save();

        user.attach(User::getRoles, role1, role2);

        User savedUser = findById(User.class, user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(2, initialRoles.size());

        user.sync(User::getRoles, role2, role3);

        User updatedUser = findById(User.class, user.getId());
        List<Role> syncedRoles = updatedUser.get(User::getRoles);
        assertEquals(2, syncedRoles.size());
        assertTrue(syncedRoles.stream().anyMatch(r -> "user".equals(r.getName())));
        assertTrue(syncedRoles.stream().anyMatch(r -> "guest".equals(r.getName())));
        assertFalse(syncedRoles.stream().anyMatch(r -> "admin".equals(r.getName())));
    }

    @Test
    public void shouldSyncWithoutDetachingBelongsToManyRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role1 = new Role();
        role1.setName("admin");
        role1.save();

        Role role2 = new Role();
        role2.setName("user");
        role2.save();

        Role role3 = new Role();
        role3.setName("guest");
        role3.save();

        user.attach(User::getRoles, role1, role2);

        User savedUser = findById(User.class, user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(2, initialRoles.size());

        user.syncWithoutDetaching(User::getRoles, role2, role3);

        User updatedUser = findById(User.class, user.getId());
        List<Role> syncedRoles = updatedUser.get(User::getRoles);
        assertEquals(3, syncedRoles.size());
        assertTrue(syncedRoles.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(syncedRoles.stream().anyMatch(r -> "user".equals(r.getName())));
        assertTrue(syncedRoles.stream().anyMatch(r -> "guest".equals(r.getName())));
    }

    @Test
    public void shouldSyncWithoutDetachingBelongsToManyRelationWithStringMethod() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role1 = new Role();
        role1.setName("admin");
        role1.save();

        Role role2 = new Role();
        role2.setName("user");
        role2.save();

        Role role3 = new Role();
        role3.setName("guest");
        role3.save();

        user.attach(User::getRoles, role1, role2);

        User savedUser = findById(User.class, user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(2, initialRoles.size());

        user.syncWithoutDetaching("roles", role2, role3);

        User updatedUser = findById(User.class, user.getId());
        List<Role> syncedRoles = updatedUser.get(User::getRoles);
        assertEquals(3, syncedRoles.size());
        assertTrue(syncedRoles.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(syncedRoles.stream().anyMatch(r -> "user".equals(r.getName())));
        assertTrue(syncedRoles.stream().anyMatch(r -> "guest".equals(r.getName())));
    }

    @Test
    public void shouldAssociateRelationWithStringMethod() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Profile profile = new Profile();
        profile.setDescription("Test Profile Description");

        user.associate("profile", profile);

        User savedUser = findById(User.class, user.getId());
        Profile savedProfile = savedUser.get(User::getProfile);

        assertNotNull(savedProfile);
        assertEquals("Test Profile Description", savedProfile.getDescription());
        assertEquals(user.getId(), savedProfile.getUserId());
    }

    @Test
    public void shouldAttachRelationWithStringMethod() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role = new Role();
        role.setName("admin");
        role.save();

        user.attach("roles", role);

        User savedUser = findById(User.class, user.getId());
        List<Role> roles = savedUser.get(User::getRoles);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("admin", roles.get(0).getName());
    }

    @Test
    public void shouldToggleBelongsToManyRelation() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role adminRole = new Role();
        adminRole.setName("admin");
        adminRole.save();

        Role userRole = new Role();
        userRole.setName("user");
        userRole.save();

        Role guestRole = new Role();
        guestRole.setName("guest");
        guestRole.save();

        User savedUser = findById(User.class, user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertTrue(initialRoles.isEmpty());

        user.toggle(User::getRoles, adminRole);

        savedUser = findById(User.class, user.getId());
        List<Role> roles1 = savedUser.get(User::getRoles);
        assertEquals(1, roles1.size());
        assertEquals("admin", roles1.get(0).getName());

        user.toggle(User::getRoles, userRole);

        savedUser = findById(User.class, user.getId());
        List<Role> roles2 = savedUser.get(User::getRoles);
        assertEquals(2, roles2.size());
        assertTrue(roles2.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(roles2.stream().anyMatch(r -> "user".equals(r.getName())));

        user.toggle(User::getRoles, adminRole);

        savedUser = findById(User.class, user.getId());
        List<Role> roles3 = savedUser.get(User::getRoles);
        assertEquals(1, roles3.size());
        assertEquals("user", roles3.get(0).getName());

        user.toggle(User::getRoles, userRole, guestRole);

        savedUser = findById(User.class, user.getId());
        List<Role> roles4 = savedUser.get(User::getRoles);
        assertEquals(1, roles4.size());
        assertTrue(roles4.stream().anyMatch(r -> "guest".equals(r.getName())));
    }

    @Test
    public void testToggleRelationWithString() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role adminRole = new Role();
        adminRole.setName("admin");
        adminRole.save();

        Role userRole = new Role();
        userRole.setName("user");
        userRole.save();

        user.toggle("roles", adminRole);

        User savedUser = findById(User.class, user.getId());
        List<Role> roles1 = savedUser.get(User::getRoles);
        assertEquals(1, roles1.size());
        assertEquals("admin", roles1.get(0).getName());

        user.toggle("roles", adminRole);

        savedUser = findById(User.class, user.getId());
        List<Role> roles2 = savedUser.get(User::getRoles);
        assertTrue(roles2.isEmpty());
    }

    @Test
    public void testToggleMultipleIds() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Role role1 = new Role();
        role1.setName("admin");
        role1.save();

        Role role2 = new Role();
        role2.setName("user");
        role2.save();

        Role role3 = new Role();
        role3.setName("guest");
        role3.save();

        List<Role> list = Arrays.asList(role1, role2, role3);
        user.toggle(User::getRoles, list);

        User savedUser = findById(User.class, user.getId());
        List<Role> roles = savedUser.get(User::getRoles);
        assertEquals(3, roles.size());
        assertTrue(roles.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(roles.stream().anyMatch(r -> "user".equals(r.getName())));
        assertTrue(roles.stream().anyMatch(r -> "guest".equals(r.getName())));

        user.toggle(User::getRoles, list);

        savedUser = findById(User.class, user.getId());
        List<Role> roles2 = savedUser.get(User::getRoles);
        assertTrue(roles2.isEmpty());
    }
}
