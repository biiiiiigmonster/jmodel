package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Likes;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HasManyThroughTest extends BaseTest {

    @Test
    public void shouldHasManyThroughNotNull() {
        User user = findById(User.class, 1L);
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
        User user = findById(User.class, 10L);
        List<Likes> likes = user.get(User::getCommentLikes);
        assertEquals(0, likes.size());
    }

    @Test
    public void shouldLoadHasManyThroughForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        RelationUtils.load(userList, User::getCommentLikes);

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
        assertEquals(4, likes2.size());
        assertEquals("Really useful React Hooks tutorial", likes2.get(0).getPraise());
        assertEquals("Helped me understand hooks better", likes2.get(1).getPraise());
        assertEquals("Advanced concepts well explained", likes2.get(2).getPraise());
        assertEquals("Excellent TypeScript patterns", likes2.get(3).getPraise());
    }
}
