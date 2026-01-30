package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Profile;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModelTest extends BaseTest {

    @Test
    public void shouldLambdaGetEqualStringGet() {
        Profile lambdaGet = userMapper.selectById(1L).get(User::getProfile);
        Profile stringGet = (Profile) userMapper.selectById(1L).get("profile");
        Profile clazzGet = userMapper.selectById(1L).get("profile", Profile.class);
        assertNotNull(lambdaGet);
        assertEquals(lambdaGet.getId(), stringGet.getId());
        assertEquals(lambdaGet.getId(), clazzGet.getId());
    }
}