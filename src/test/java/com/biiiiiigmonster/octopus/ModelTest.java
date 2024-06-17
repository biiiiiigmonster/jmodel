package com.biiiiiigmonster.octopus;

import com.biiiiiigmonster.octopus.models.User;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class ModelTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void get() {
        List<User> users = new User().newQuery().get();
        System.out.println(users);
    }
}
