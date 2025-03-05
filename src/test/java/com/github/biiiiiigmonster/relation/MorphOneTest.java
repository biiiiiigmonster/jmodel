package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Image;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MorphOneTest extends BaseTest {

    @Test
    public void shouldPostMorphOneImage() {
        Post post = postMapper.selectById(1L);
        Image image = post.get(Post::getImage);
        assertNotNull(image);
        assertEquals("https://example.com/images/post1.jpg", image.getUrl());
    }

    @Test
    public void shouldUserMorphOneImage() {
        User user = userMapper.selectById(1L);
        Image image = user.get(User::getImage);
        assertNotNull(image);
        assertEquals("https://example.com/images/user1.jpg", image.getUrl());
    }

    @Test
    public void shouldPostMorphOneEmpty() {
        Post post = postMapper.selectById(10L);
        Image image = post.get(Post::getImage);
        assertNull(image);
    }
}