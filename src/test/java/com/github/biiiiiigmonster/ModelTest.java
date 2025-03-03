package com.github.biiiiiigmonster;

import cn.hutool.json.JSONUtil;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@Slf4j
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class ModelTest {

    @Autowired
    UserService userService;

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        User user = userService.getById(1L);
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        log.info("User: {}", JSONUtil.toJsonStr(user));

        // 加载关联的电话
        user.load(User::getPhone);
        assertNotNull(user.getPhone());
        assertEquals("1234567890", user.getPhone().getNumber());
    }
}
