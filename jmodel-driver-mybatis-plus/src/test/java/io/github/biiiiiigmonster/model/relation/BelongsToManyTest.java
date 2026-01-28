package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Role;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.entity.UserRole;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BelongsToManyTest extends BaseTest {

    @Test
    public void shouldBelongsToManyNotNull() {
        User user = userMapper.selectById(1L);
        List<Role> roles = user.get(User::getRoles);
        assertNotNull(roles);
        assertEquals(3, roles.size());
        assertEquals("Administrator", roles.get(0).getName());
        assertEquals("Moderator", roles.get(1).getName());
        assertEquals("Editor", roles.get(2).getName());
    }

    @Test
    public void shouldBelongsToManyLessRoles() {
        User user = userMapper.selectById(10L);
        List<Role> roles = user.get(User::getRoles);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("Moderator", roles.get(0).getName());
    }

    @Test
    public void shouldBelongsToManyEmpty() {
        User user = userMapper.selectById(11L);
        List<Role> roles = user.get(User::getRoles);
        assertEquals(0, roles.size());
    }

    @Test
    public void shouldLoadBelongsToManyForList() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 10L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(userList, User::getRoles);

        // 直接使用getRoles()获取已加载的数据
        User user1 = userList.get(0);
        List<Role> roles1 = user1.getRoles();
        assertNotNull(roles1);
        assertEquals(3, roles1.size());
        assertEquals("Administrator", roles1.get(0).getName());
        assertEquals("Moderator", roles1.get(1).getName());
        assertEquals("Editor", roles1.get(2).getName());

        User user2 = userList.get(1);
        List<Role> roles2 = user2.getRoles();
        assertNotNull(roles2);
        assertEquals(1, roles2.size());
        assertEquals("Moderator", roles2.get(0).getName());
    }

    @Test
    public void shouldBelongsToManyWithPivotForList() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 10L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(userList, User::getRoles, User::getRoleWithPivots);

        // 直接使用getRoles()获取已加载的数据
        User user1 = userList.get(0);
        List<Role> roles1 = user1.getRoles();
        List<Role> roleWithPivots1 = user1.getRoleWithPivots();
        assertNotNull(roles1);
        assertNotNull(roleWithPivots1);
        assertEquals(3, roles1.size());
        assertEquals(roles1.size(), roleWithPivots1.size());
        assertEquals("Administrator", roles1.get(0).getName());
        assertEquals("Administrator", roleWithPivots1.get(0).getName());
        assertNull(roles1.get(0).getPivot());
        UserRole userRole1 = (UserRole) roleWithPivots1.get(0).getPivot();
        assertNotNull(userRole1);
        assertEquals(user1.getId(), userRole1.getUserId());
        assertEquals(roleWithPivots1.get(0).getId(), userRole1.getRoleId());

        User user2 = userList.get(1);
        List<Role> roles2 = user2.getRoles();
        List<Role> roleWithPivots2 = user2.getRoleWithPivots();
        assertNotNull(roles2);
        assertNotNull(roleWithPivots2);
        assertEquals(1, roles2.size());
        assertEquals(roles2.size(), roleWithPivots2.size());
        assertEquals("Moderator", roles2.get(0).getName());
        assertEquals("Moderator", roleWithPivots2.get(0).getName());
        assertNull(roles2.get(0).getPivot());
        UserRole userRole2 = (UserRole) roleWithPivots2.get(0).getPivot();
        assertNotNull(userRole2);
        assertEquals(user2.getId(), userRole2.getUserId());
        assertEquals(roleWithPivots2.get(0).getId(), userRole2.getRoleId());
    }
}