package io.github.biiiiiigmonster.driver.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.driver.entity.Permission;
import io.github.biiiiiigmonster.driver.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HasManyDeepTest extends BaseTest {

    @Test
    public void shouldHasManyDeepNotNull() {
        User user = findById(User.class, 1L);
        List<Permission> permissions = user.get(User::getPermissions);
        assertNotNull(permissions);
        assertEquals(7, permissions.size());
        assertEquals(Arrays.asList(
                "Manage users",
                "Manage roles",
                "System configuration",
                "Moderate posts",
                "Moderate comments",
                "Edit posts",
                "Publish posts"
        ), descList(permissions));
    }

    @Test
    public void shouldHasManyDeepPartialRoles() {
        User user = findById(User.class, 10L);
        List<Permission> permissions = user.get(User::getPermissions);
        assertNotNull(permissions);
        assertEquals(2, permissions.size());
        assertEquals(Arrays.asList("Moderate posts", "Moderate comments"), descList(permissions));
    }

    @Test
    public void shouldHasManyDeepEmpty() {
        User user = findById(User.class, 11L);
        List<Permission> permissions = user.get(User::getPermissions);
        assertEquals(0, permissions.size());
    }

    @Test
    public void shouldLoadHasManyDeepForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L, 5L));
        assertEquals(3, userList.size());

        RelationUtils.load(userList, User::getPermissions);

        User user1 = userList.get(0);
        List<Permission> permissions1 = user1.getPermissions();
        assertNotNull(permissions1);
        assertEquals(7, permissions1.size());
        assertEquals("Manage users", permissions1.get(0).getDesc());

        User user2 = userList.get(1);
        List<Permission> permissions2 = user2.getPermissions();
        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertEquals(Arrays.asList(
                "Moderate posts",
                "Moderate comments",
                "Edit posts",
                "Publish posts"
        ), descList(permissions2));

        User user5 = userList.get(2);
        List<Permission> permissions5 = user5.getPermissions();
        assertNotNull(permissions5);
        assertEquals(5, permissions5.size());
        assertEquals(Arrays.asList(
                "Manage users",
                "Manage roles",
                "System configuration",
                "Edit posts",
                "Publish posts"
        ), descList(permissions5));
    }

    private List<String> descList(List<Permission> permissions) {
        return permissions.stream().map(Permission::getDesc).collect(Collectors.toList());
    }
}
