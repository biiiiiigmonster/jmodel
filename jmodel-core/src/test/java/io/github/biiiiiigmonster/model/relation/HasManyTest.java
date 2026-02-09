package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HasManyTest extends BaseTest {

    @Test
    public void shouldHasManyNotNull() {
        User user = findById(User.class, 1L);
        List<Post> posts = user.get(User::getPosts);
        assertNotNull(posts);
        assertEquals(2, posts.size());
        assertEquals("Getting Started with Spring Boot", posts.get(0).getTitle());
        assertEquals("Mastering JPA Relationships", posts.get(1).getTitle());
    }

    @Test
    public void shouldHasManyEmpty() {
        User user = findById(User.class, 10L);
        List<Post> posts = user.get(User::getPosts);
        assertEquals(0, posts.size());
    }

    @Test
    public void shouldLoadHasManyForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        RelationUtils.load(userList, User::getPosts);

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

    @Test
    public void shouldHasManyChaperoneForList() {
        User user = findById(User.class, 1L);
        user.load(User::getPosts, User::getPostChaperones);

        List<Post> posts = user.getPosts();
        List<Post> postChaperones = user.getPostChaperones();
        assertNotNull(posts);
        assertNotNull(postChaperones);
        assertEquals(posts.size(), postChaperones.size());
        assertNull(posts.get(0).getUser());
        assertEquals(postChaperones.get(0).getUser(), user);
    }
}
