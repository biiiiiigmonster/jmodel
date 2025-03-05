package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Comment;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.Video;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MorphManyTest extends BaseTest {

    @Test
    public void shouldPostMorphManyComments() {
        Post post = postMapper.selectById(1L);
        List<Comment> comments = post.get(Post::getComments);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("Great tutorial!", comments.get(0).getContext());
        assertEquals("Very helpful content", comments.get(1).getContext());
    }

    @Test
    public void shouldVideoMorphManyComments() {
        Video video = videoMapper.selectById(1L);
        List<Comment> comments = video.get(Video::getComments);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Excellent video!", comments.get(0).getContext());
    }

    @Test
    public void shouldPostMorphManyEmpty() {
        Post post = postMapper.selectById(10L);
        List<Comment> comments = post.get(Post::getComments);
        assertNull(comments);
    }
}