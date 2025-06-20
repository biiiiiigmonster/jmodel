package com.github.biiiiiigmonster.model;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Profile;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModelTest extends BaseTest {

    @Test
    public void shouldLambdaGetEqualStringGet() {
        Profile lambdaGet = userMapper.selectById(1L).get(User::getProfile);
        Profile stringGet = (Profile) userMapper.selectById(1L).get("profile");
        assertNotNull(lambdaGet);
        assertEquals(lambdaGet.getId(), stringGet.getId());
    }
}
