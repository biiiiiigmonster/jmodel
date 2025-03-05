package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Profile;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HasOneTest extends BaseTest {

    @Test
    public void shouldNotNull() {
        User user = userMapper.selectById(1L);
        Profile profile = user.get(User::getProfile);
        assertNotNull(profile);
        assertEquals("Software Engineer at Tech Corp", profile.getDescription());
    }
}
