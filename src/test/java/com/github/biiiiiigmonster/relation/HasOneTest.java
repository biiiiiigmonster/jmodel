package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HasOneTest extends BaseTest {

    @Test
    public void shouldNotNull() {
        User user = userService.getById(1L);
        user.load(User::getProfile);
        assertNotNull(user.getProfile());
        assertEquals("Happy code, happy life.", user.getProfile().getDescription());
    }
}
