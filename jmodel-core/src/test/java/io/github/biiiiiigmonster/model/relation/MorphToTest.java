package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Comment;
import io.github.biiiiiigmonster.entity.Image;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MorphToTest extends BaseTest {

    @Test
    public void shouldCommentMorphToPost() {
        Comment comment = findById(Comment.class, 1L);
        Post post = comment.get(Comment::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }

    @Test
    public void shouldImageMorphToUser() {
        Image image = findById(Image.class, 1L);
        User user = image.get(Image::getUser);
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void shouldImageMorphToPost() {
        Image image = findById(Image.class, 2L);
        Post post = image.get(Image::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }

    @Test
    public void shouldLoadMorphToForList() {
        List<Comment> commentList = findByIds(Comment.class, Arrays.asList(1L, 2L));
        assertEquals(2, commentList.size());

        RelationUtils.load(commentList, Comment::getPost);

        Comment comment1 = commentList.get(0);
        Post post1 = comment1.getPost();
        assertNotNull(post1);
        assertEquals("Getting Started with Spring Boot", post1.getTitle());

        Comment comment2 = commentList.get(1);
        Post post2 = comment2.getPost();
        assertNotNull(post2);
        assertEquals("Getting Started with Spring Boot", post2.getTitle());
    }
}
