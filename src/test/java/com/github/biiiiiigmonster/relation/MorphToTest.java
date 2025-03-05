package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Comment;
import com.github.biiiiiigmonster.entity.Image;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MorphToTest extends BaseTest {

    @Test
    public void shouldCommentMorphToPost() {
        Comment comment = commentMapper.selectById(1L);
        Post post = comment.get(Comment::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }

    @Test
    public void shouldImageMorphToUser() {
        Image image = imageMapper.selectById(1L);
        User user = image.get(Image::getUser);
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void shouldImageMorphToPost() {
        Image image = imageMapper.selectById(2L);
        Post post = image.get(Image::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }
}