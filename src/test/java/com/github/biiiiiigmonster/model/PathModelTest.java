package com.github.biiiiiigmonster.model;

import cn.hutool.json.JSONUtil;
import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PathModelTest extends BaseTest {
    @Resource
    protected MockMvc mockMvc;

    @Test
    public void userPathModelTest() throws Exception {
        Long userId = 1L;
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{user}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        User respUser = JSONUtil.toBean(response, User.class);
        assertNotNull(respUser);
        User user = userMapper.selectById(userId);
        assertTrue(respUser.is(user));
    }
}
