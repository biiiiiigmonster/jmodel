package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HasManyTest extends BaseTest {

    @Test
    public void shouldHasManyNotNull() {
        User user = userMapper.selectById(1L);
        List<Post> posts = user.get(User::getPosts);
        assertNotNull(posts);
        assertEquals(2, posts.size());
        assertEquals("Getting Started with Spring Boot", posts.get(0).getTitle());
        assertEquals("Mastering JPA Relationships", posts.get(1).getTitle());
    }

    @Test
    public void shouldHasManyEmpty() {
        User user = userMapper.selectById(10L);
        List<Post> posts = user.get(User::getPosts);
        assertNull(posts);
    }
}