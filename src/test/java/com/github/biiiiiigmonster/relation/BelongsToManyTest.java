package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Role;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
}