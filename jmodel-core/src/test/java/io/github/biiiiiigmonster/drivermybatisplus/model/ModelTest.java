package io.github.biiiiiigmonster.drivermybatisplus.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.drivermybatisplus.entity.Profile;
import io.github.biiiiiigmonster.drivermybatisplus.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModelTest extends BaseTest {

    @Test
    public void shouldLambdaGetEqualStringGet() {
        Profile lambdaGet = findById(User.class, 1L).get(User::getProfile);
        Profile stringGet = (Profile) findById(User.class, 1L).get("profile");
        Profile clazzGet = findById(User.class, 1L).get("profile", Profile.class);
        assertNotNull(lambdaGet);
        assertEquals(lambdaGet.getId(), stringGet.getId());
        assertEquals(lambdaGet.getId(), clazzGet.getId());
    }
}
