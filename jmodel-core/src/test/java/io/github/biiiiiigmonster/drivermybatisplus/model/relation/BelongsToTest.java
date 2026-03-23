package io.github.biiiiiigmonster.drivermybatisplus.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.drivermybatisplus.entity.Comment;
import io.github.biiiiiigmonster.drivermybatisplus.entity.Post;
import io.github.biiiiiigmonster.drivermybatisplus.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BelongsToTest extends BaseTest {

    @Test
    public void shouldBelongsToPostUser() {
        Post post = findById(Post.class, 1L);
        User user = post.get(Post::getUser);
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void shouldBelongsToCommentPost() {
        Comment comment = findById(Comment.class, 1L);
        Post post = comment.get(Comment::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }
}
