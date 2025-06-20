package com.github.biiiiiigmonster.model.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Phone;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.Role;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.mapper.PhoneMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RelatedModelOperationTest extends BaseTest {

    @Autowired
    protected PhoneMapper phoneMapper;

    @Test
    public void shouldSaveHasOneRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建电话并设置关联
        Phone phone = new Phone();
        phone.setNumber("1234567890");
        
        // 设置关联
        user.setPhone(phone);
        
        // 保存关联
        user.save(User::getPhone);
        
        // 验证关联已保存
        User savedUser = userMapper.selectById(user.getId());
        Phone savedPhone = savedUser.get(User::getPhone);
        
        assertNotNull(savedPhone);
        assertEquals("1234567890", savedPhone.getNumber());
        assertEquals(user.getId(), savedPhone.getUserId());
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
        
        // 设置关联
        user.setPosts(posts);
        
        // 保存关联
        user.save(User::getPosts);
        
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
    public void shouldCreateHasOneRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建并保存电话关联
        Phone phone = new Phone();
        phone.setNumber("1234567890");
        phone = user.create(User::getPhone, phone);
        
        assertNotNull(phone);
        assertEquals("1234567890", phone.getNumber());
        assertEquals(user.getId(), phone.getUserId());
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
    public void shouldUpdateHasOneRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建电话
        Phone phone = new Phone();
        phone.setNumber("1234567890");
        phoneMapper.insert(phone);
        
        // 设置关联
        user.setPhone(phone);
        user.save(User::getPhone);
        
        // 更新电话
        phone.setNumber("9876543210");
        user.update(User::getPhone, phone);
        
        // 验证更新
        User savedUser = userMapper.selectById(user.getId());
        Phone updatedPhone = savedUser.get(User::getPhone);
        
        assertEquals("9876543210", updatedPhone.getNumber());
    }

    @Test
    public void shouldSaveRelationWithStringMethod() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建电话
        Phone phone = new Phone();
        phone.setNumber("1234567890");
        
        // 设置关联
        user.setPhone(phone);
        
        // 使用字符串方式保存关联
        user.save("phone");
        
        // 验证关联已保存
        User savedUser = userMapper.selectById(user.getId());
        Phone savedPhone = savedUser.get(User::getPhone);
        
        assertNotNull(savedPhone);
        assertEquals("1234567890", savedPhone.getNumber());
        assertEquals(user.getId(), savedPhone.getUserId());
    }

    @Test
    public void shouldCreateRelationWithStringMethod() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 使用字符串方式创建并保存电话关联
        Phone phone = new Phone();
        phone.setNumber("1234567890");
        phone = user.create("phone", phone);
        
        assertNotNull(phone);
        assertEquals("1234567890", phone.getNumber());
        assertEquals(user.getId(), phone.getUserId());
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
}
