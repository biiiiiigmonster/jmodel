package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HasOneTest extends BaseTest {

    @Test
    public void shouldHasOneNotNull() {
        User user = userService.getById(1L);
    }
}
