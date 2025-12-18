package com.github.biiiiiigmonster.model.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Likes;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


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
        assertEquals(0, likes.size());
    }

    @Test
    public void shouldLoadHasManyThroughForList() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(userList, User::getCommentLikes);

        // 直接使用getCommentLikes()获取已加载的数据
        User user1 = userList.get(0);
        List<Likes> likes1 = user1.getCommentLikes();
        assertNotNull(likes1);
        assertEquals(4, likes1.size());
        assertEquals("Excellent explanation of Spring Boot concepts!", likes1.get(0).getPraise());
        assertEquals("Very helpful for beginners", likes1.get(1).getPraise());
        assertEquals("Great insights into JPA relationships", likes1.get(2).getPraise());
        assertEquals("Clear and concise explanation", likes1.get(3).getPraise());

        User user2 = userList.get(1);
        List<Likes> likes2 = user2.getCommentLikes();
        assertNotNull(likes2);
        // 根据data.sql中的数据，用户2应该有4个点赞
        assertEquals(4, likes2.size());
        assertEquals("Really useful React Hooks tutorial", likes2.get(0).getPraise());
        assertEquals("Helped me understand hooks better", likes2.get(1).getPraise());
        assertEquals("Advanced concepts well explained", likes2.get(2).getPraise());
        assertEquals("Excellent TypeScript patterns", likes2.get(3).getPraise());
    }
}