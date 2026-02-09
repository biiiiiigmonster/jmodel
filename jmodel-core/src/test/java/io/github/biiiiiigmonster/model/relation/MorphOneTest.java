package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Image;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MorphOneTest extends BaseTest {

    @Test
    public void shouldPostMorphOneImage() {
        Post post = findById(Post.class, 1L);
        Image image = post.get(Post::getImage);
        assertNotNull(image);
        assertEquals("https://example.com/images/post1.jpg", image.getUrl());
    }

    @Test
    public void shouldUserMorphOneImage() {
        User user = findById(User.class, 1L);
        Image image = user.get(User::getImage);
        assertNotNull(image);
        assertEquals("https://example.com/images/user1.jpg", image.getUrl());
    }

    @Test
    public void shouldPostMorphOneEmpty() {
        Post post = findById(Post.class, 10L);
        Image image = post.get(Post::getImage);
        assertNull(image);
    }

    @Test
    public void shouldLoadMorphOneForList() {
        List<Post> postList = findByIds(Post.class, Arrays.asList(1L, 2L));
        assertEquals(2, postList.size());

        RelationUtils.load(postList, Post::getImage);

        Post post1 = postList.get(0);
        Image image1 = post1.getImage();
        assertNotNull(image1);
        assertEquals("https://example.com/images/post1.jpg", image1.getUrl());

        Post post2 = postList.get(1);
        Image image2 = post2.getImage();
        assertNotNull(image2);
        assertEquals("https://example.com/images/post2.jpg", image2.getUrl());
    }
}
