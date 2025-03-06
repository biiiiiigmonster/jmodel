package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Likes;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class HasManyThroughTest extends BaseTest {

    @Test
    public void shouldHasManyThroughNotNull() {
        User user = userMapper.selectById(1L);
        List<Likes> likes = user.get(User::getCommentLikes);
        assertNotNull(likes);
        assertEquals(4, likes.size());
        assertEquals("Excellent explanation of Spring Boot concepts!", likes.get(0).getPraise());
        assertEquals("Very helpful for beginners", likes.get(1).getPraise());
        assertEquals("Great insights into JPA relationships", likes.get(2).getPraise());
        assertEquals("Clear and concise explanation", likes.get(3).getPraise());
    }

    @Test
    public void shouldHasManyThroughEmpty() {
        User user = userMapper.selectById(10L);
        List<Likes> likes = user.get(User::getCommentLikes);
        assertNull(likes);
    }
}