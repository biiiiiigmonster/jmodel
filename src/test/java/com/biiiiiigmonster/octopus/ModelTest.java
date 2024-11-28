package com.biiiiiigmonster.octopus;

import com.biiiiiigmonster.octopus.models.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@Slf4j
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
        List<User> users = User.query().get();
        log.info("result: {}", users);
    }
}
