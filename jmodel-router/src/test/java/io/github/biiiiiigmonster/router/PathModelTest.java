package io.github.biiiiiigmonster.router;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.biiiiiigmonster.ModelNotFoundException;
import io.github.biiiiiigmonster.router.entity.Post;
import io.github.biiiiiigmonster.router.entity.User;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PathModelTest extends BaseTest {
    @Resource
    protected MockMvc mockMvc;

    @Test
    public void pathModelTest() throws Exception {
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

    @Test
    public void pathModelRouteKeyTest() throws Exception {
        String name = "John Doe";
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/byName/{user}", name)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        User respUser = JSONUtil.toBean(response, User.class);
        assertNotNull(respUser);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        User user = userMapper.selectOne(queryWrapper);
        assertTrue(respUser.is(user));
    }


    @Test
    public void multiPathModelTest() throws Exception {
        Long userId = 1L;
        Long postId = 5L;
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{user}/posts/{post}", userId, postId)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        User respUser = JSONUtil.parseArray(response).get(0, User.class);
        assertNotNull(respUser);
        User user = userMapper.selectById(userId);
        assertTrue(respUser.is(user));

        Post respPost = JSONUtil.parseArray(response).get(1, Post.class);
        assertNotNull(respPost);
        Post post = postMapper.selectById(postId);
        assertTrue(respPost.is(post));
    }

    @Test
    public void pathModelNotFoundTest() throws Exception {
        Long userId = 1000L;
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{user}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(response, new ModelNotFoundException(User.class).getMessage());
    }

    @Test
    public void shouldScopeBindingModelTest() throws Exception {
        Long userId = 1L;
        Long postId = 2L;
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{user}/posts/{post}/scopeBinding", userId, postId)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        User respUser = JSONUtil.parseArray(response).get(0, User.class);
        assertNotNull(respUser);
        User user = userMapper.selectById(userId);
        assertTrue(respUser.is(user));

        Post respPost = JSONUtil.parseArray(response).get(1, Post.class);
        assertNotNull(respPost);
        Post post = postMapper.selectById(postId);
        assertTrue(respPost.is(post));

        assertEquals(post.getUserId(), user.getId());
    }

    @Test
    public void shouldScopeBindingNotFoundTest() throws Exception {
        Long userId = 1L;
        Long postId = 5L;
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{user}/posts/{post}/scopeBinding", userId, postId)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(response, new ModelNotFoundException(Post.class).getMessage());
    }
}
