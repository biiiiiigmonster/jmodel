package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertEquals(0, posts.size());
    }

    @Test
    public void shouldLoadHasManyForList() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(userList, User::getPosts);

        // 直接使用getPosts()获取已加载的数据
        User user1 = userList.get(0);
        List<Post> posts1 = user1.getPosts();
        assertNotNull(posts1);
        assertEquals(2, posts1.size());
        assertEquals("Getting Started with Spring Boot", posts1.get(0).getTitle());
        assertEquals("Mastering JPA Relationships", posts1.get(1).getTitle());

        User user2 = userList.get(1);
        List<Post> posts2 = user2.getPosts();
        assertNotNull(posts2);
        assertEquals(2, posts2.size());
        assertEquals("Introduction to React Hooks", posts2.get(0).getTitle());
        assertEquals("Advanced TypeScript Patterns", posts2.get(1).getTitle());
    }
}