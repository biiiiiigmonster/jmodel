package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.Tag;
import com.github.biiiiiigmonster.entity.Video;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MorphToManyTest extends BaseTest {

    @Test
    public void shouldPostMorphToManyTags() {
        Post post = postMapper.selectById(1L);
        List<Tag> tags = post.get(Post::getTags);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("Java", tags.get(0).getName());
        assertEquals("Spring", tags.get(1).getName());
    }

    @Test
    public void shouldVideoMorphToManyTags() {
        Video video = videoMapper.selectById(1L);
        List<Tag> tags = video.get(Video::getTags);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("Java", tags.get(0).getName());
        assertEquals("Spring", tags.get(1).getName());
    }

    @Test
    public void shouldPostMorphToManyEmpty() {
        Post post = postMapper.selectById(10L);
        List<Tag> tags = post.get(Post::getTags);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("Testing", tags.get(0).getName());
        assertEquals("DevOps", tags.get(1).getName());
    }
}