package com.biiiiiigmonster.jmodel;

import com.biiiiiigmonster.jmodel.models.User;
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

    public void get() {
        User user = new User();
        List<User> users = user.newQuery().get();
        System.out.println(users);
    }
}
