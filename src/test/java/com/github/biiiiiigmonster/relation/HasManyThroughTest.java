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
        assertEquals(5, likes.size());
        assertEquals("非常实用的Spring Boot教程！👍", likes.get(0).getPraise());
        assertEquals("讲解得很清晰，收藏了", likes.get(1).getPraise());
        assertEquals("对初学者很友好", likes.get(2).getPraise());
        assertEquals("这篇JPA关系讲解太棒了", likes.get(3).getPraise());
        assertEquals("终于理解了多对多关系", likes.get(4).getPraise());
    }

    @Test
    public void shouldHasManyThroughEmpty() {
        User user = userMapper.selectById(10L);
        List<Likes> likes = user.get(User::getCommentLikes);
        assertNull(likes);
    }
}