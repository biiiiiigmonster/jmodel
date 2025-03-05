package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Address;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HasOneThroughTest extends BaseTest {

    @Test
    public void shouldHasOneThroughNotNull() {
        User user = userMapper.selectById(1L);
        Address address = user.get(User::getProfileAddress);
        assertNotNull(address);
        assertEquals("New York, NY", address.getLocation());
    }

    @Test
    public void shouldHasOneThroughEmpty() {
        User user = userMapper.selectById(10L);
        Address address = user.get(User::getProfileAddress);
        assertNotNull(address);
        assertEquals("Denver, CO", address.getLocation());
    }
}