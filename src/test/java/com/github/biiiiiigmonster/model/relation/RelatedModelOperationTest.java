package com.github.biiiiiigmonster.model.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Profile;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.Role;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.entity.Comment;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class RelatedModelOperationTest extends BaseTest {

    @Test
    public void shouldSaveHasOneRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建个人资料并设置关联
        Profile profile = new Profile();
        profile.setDescription("Test Profile Description");

        // 保存关联
        user.save(User::getProfile, profile);

        // 验证关联已保存
        User savedUser = userMapper.selectById(user.getId());
        Profile savedProfile = savedUser.get(User::getProfile);

        assertNotNull(savedProfile);
        assertEquals("Test Profile Description", savedProfile.getDescription());
        assertEquals(user.getId(), savedProfile.getUserId());
    }

    @Test
    public void shouldSaveHasManyRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建帖子列表
        Post post1 = new Post();
        post1.setTitle("First Post");

        Post post2 = new Post();
        post2.setTitle("Second Post");

        List<Post> posts = Arrays.asList(post1, post2);

        // 保存关联
        user.save(User::getPosts, posts);

        // 验证关联已保存
        User savedUser = userMapper.selectById(user.getId());
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
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role role1 = new Role();
        role1.setName("admin");
        roleMapper.insert(role1);

        Role role2 = new Role();
        role2.setName("user");
        roleMapper.insert(role2);

        // 附加角色关联
        user.attach(User::getRoles, role1, role2);

        // 验证关联已创建
        User savedUser = userMapper.selectById(user.getId());
        List<Role> roles = savedUser.get(User::getRoles);

        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(roles.stream().anyMatch(r -> "user".equals(r.getName())));
    }

    @Test
    public void shouldDetachBelongsToManyRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role role1 = new Role();
        role1.setName("admin");
        roleMapper.insert(role1);

        Role role2 = new Role();
        role2.setName("user");
        roleMapper.insert(role2);

        Role role3 = new Role();
        role3.setName("guest");
        roleMapper.insert(role3);

        // 先附加所有角色
        user.attach(User::getRoles, role1, role2, role3);

        // 验证初始状态
        User savedUser = userMapper.selectById(user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(3, initialRoles.size());

        // 分离指定角色
        user.detach(User::getRoles, role1, role2);

        // 验证分离后的状态
        User updatedUser = userMapper.selectById(user.getId());
        List<Role> remainingRoles = updatedUser.get(User::getRoles);
        assertEquals(1, remainingRoles.size());
        assertEquals("guest", remainingRoles.get(0).getName());
    }

    @Test
    public void shouldDetachAllBelongsToManyRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role role1 = new Role();
        role1.setName("admin");
        roleMapper.insert(role1);

        Role role2 = new Role();
        role2.setName("user");
        roleMapper.insert(role2);

        // 先附加角色
        user.attach(User::getRoles, role1, role2);

        // 验证初始状态
        User savedUser = userMapper.selectById(user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(2, initialRoles.size());

        // 分离所有角色
        user.detach(User::getRoles);

        // 验证分离后的状态
        User updatedUser = userMapper.selectById(user.getId());
        List<Role> remainingRoles = updatedUser.get(User::getRoles);
        assertEquals(0, remainingRoles.size());
    }

    @Test
    public void shouldSyncBelongsToManyRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role role1 = new Role();
        role1.setName("admin");
        roleMapper.insert(role1);

        Role role2 = new Role();
        role2.setName("user");
        roleMapper.insert(role2);

        Role role3 = new Role();
        role3.setName("guest");
        roleMapper.insert(role3);

        // 先附加角色1和2
        user.attach(User::getRoles, role1, role2);

        // 验证初始状态
        User savedUser = userMapper.selectById(user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertEquals(2, initialRoles.size());

        // 同步为角色2和3
        user.sync(User::getRoles, role2, role3);

        // 验证同步后的状态
        User updatedUser = userMapper.selectById(user.getId());
        List<Role> syncedRoles = updatedUser.get(User::getRoles);
        assertEquals(2, syncedRoles.size());
        assertTrue(syncedRoles.stream().anyMatch(r -> "user".equals(r.getName())));
        assertTrue(syncedRoles.stream().anyMatch(r -> "guest".equals(r.getName())));
        assertFalse(syncedRoles.stream().anyMatch(r -> "admin".equals(r.getName())));
    }

    @Test
    public void shouldSaveRelationWithStringMethod() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建个人资料
        Profile profile = new Profile();
        profile.setDescription("Test Profile Description");

        // 使用字符串方式保存关联
        user.save("profile", profile);

        // 验证关联已保存
        User savedUser = userMapper.selectById(user.getId());
        Profile savedProfile = savedUser.get(User::getProfile);

        assertNotNull(savedProfile);
        assertEquals("Test Profile Description", savedProfile.getDescription());
        assertEquals(user.getId(), savedProfile.getUserId());
    }

    @Test
    public void shouldAttachRelationWithStringMethod() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role role = new Role();
        role.setName("admin");
        roleMapper.insert(role);

        // 使用字符串方式附加角色关联
        user.attach("roles", role);

        // 验证关联已创建
        User savedUser = userMapper.selectById(user.getId());
        List<Role> roles = savedUser.get(User::getRoles);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("admin", roles.get(0).getName());
    }

    @Test
    public void testToggleRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role adminRole = new Role();
        adminRole.setName("admin");
        roleMapper.insert(adminRole);

        Role userRole = new Role();
        userRole.setName("user");
        roleMapper.insert(userRole);

        Role guestRole = new Role();
        guestRole.setName("guest");
        roleMapper.insert(guestRole);

        // 初始状态：没有任何角色
        User savedUser = userMapper.selectById(user.getId());
        List<Role> initialRoles = savedUser.get(User::getRoles);
        assertTrue(initialRoles.isEmpty());

        // 切换admin角色（添加）
        List<Long> result1 = user.toggle(User::getRoles, adminRole.getId());
        assertEquals(1, result1.size());
        assertTrue(result1.contains(adminRole.getId()));

        // 验证关联已添加
        savedUser = userMapper.selectById(user.getId());
        List<Role> roles1 = savedUser.get(User::getRoles);
        assertEquals(1, roles1.size());
        assertEquals("admin", roles1.get(0).getName());

        // 切换user角色（添加）
        List<Long> result2 = user.toggle(User::getRoles, userRole.getId());
        assertEquals(2, result2.size());
        assertTrue(result2.contains(adminRole.getId()));
        assertTrue(result2.contains(userRole.getId()));

        // 验证关联已添加
        savedUser = userMapper.selectById(user.getId());
        List<Role> roles2 = savedUser.get(User::getRoles);
        assertEquals(2, roles2.size());
        assertTrue(roles2.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(roles2.stream().anyMatch(r -> "user".equals(r.getName())));

        // 切换admin角色（移除，因为已存在）
        List<Long> result3 = user.toggle(User::getRoles, adminRole.getId());
        assertEquals(1, result3.size());
        assertTrue(result3.contains(userRole.getId()));
        assertFalse(result3.contains(adminRole.getId()));

        // 验证关联已移除
        savedUser = userMapper.selectById(user.getId());
        List<Role> roles3 = savedUser.get(User::getRoles);
        assertEquals(1, roles3.size());
        assertEquals("user", roles3.get(0).getName());

        // 切换多个角色
        List<Long> result4 = user.toggle(User::getRoles, Arrays.asList(adminRole.getId(), guestRole.getId()));
        assertEquals(3, result4.size());
        assertTrue(result4.contains(userRole.getId()));
        assertTrue(result4.contains(adminRole.getId()));
        assertTrue(result4.contains(guestRole.getId()));

        // 验证多个关联已添加
        savedUser = userMapper.selectById(user.getId());
        List<Role> roles4 = savedUser.get(User::getRoles);
        assertEquals(3, roles4.size());
        assertTrue(roles4.stream().anyMatch(r -> "admin".equals(r.getName())));
        assertTrue(roles4.stream().anyMatch(r -> "user".equals(r.getName())));
        assertTrue(roles4.stream().anyMatch(r -> "guest".equals(r.getName())));
    }

    @Test
    public void testToggleRelationWithString() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建角色
        Role adminRole = new Role();
        adminRole.setName("admin");
        roleMapper.insert(adminRole);

        Role userRole = new Role();
        userRole.setName("user");
        roleMapper.insert(userRole);

        // 使用字符串方式切换角色
        List<Long> result1 = user.toggle("roles", adminRole.getId());
        assertEquals(1, result1.size());
        assertTrue(result1.contains(adminRole.getId()));

        // 验证关联已添加
        User savedUser = userMapper.selectById(user.getId());
        List<Role> roles1 = savedUser.get(User::getRoles);
        assertEquals(1, roles1.size());
        assertEquals("admin", roles1.get(0).getName());

        // 再次切换同一个角色（移除）
        List<Long> result2 = user.toggle("roles", adminRole.getId());
        assertEquals(0, result2.size());
        assertFalse(result2.contains(adminRole.getId()));

        // 验证关联已移除
        savedUser = userMapper.selectById(user.getId());
        List<Role> roles2 = savedUser.get(User::getRoles);
        assertTrue(roles2.isEmpty());
    }

    @Test
    public void testToggleMultipleIds() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建多个角色
        Role role1 = new Role();
        role1.setName("role1");
        roleMapper.insert(role1);

        Role role2 = new Role();
        role2.setName("role2");
        roleMapper.insert(role2);

        Role role3 = new Role();
        role3.setName("role3");
        roleMapper.insert(role3);

        // 切换多个ID
        List<Long> ids = Arrays.asList(role1.getId(), role2.getId(), role3.getId());
        List<Long> result = user.toggle(User::getRoles, ids);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(ids));

        // 验证所有关联都已添加
        User savedUser = userMapper.selectById(user.getId());
        List<Role> roles = savedUser.get(User::getRoles);
        assertEquals(3, roles.size());
        assertTrue(roles.stream().anyMatch(r -> "role1".equals(r.getName())));
        assertTrue(roles.stream().anyMatch(r -> "role2".equals(r.getName())));
        assertTrue(roles.stream().anyMatch(r -> "role3".equals(r.getName())));

        // 再次切换相同的ID（应该全部移除）
        List<Long> result2 = user.toggle(User::getRoles, ids);
        assertEquals(0, result2.size());

        // 验证所有关联都已移除
        savedUser = userMapper.selectById(user.getId());
        List<Role> roles2 = savedUser.get(User::getRoles);
        assertTrue(roles2.isEmpty());
    }
}
