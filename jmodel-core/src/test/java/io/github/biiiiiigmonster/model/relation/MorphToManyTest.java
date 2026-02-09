package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.Tag;
import io.github.biiiiiigmonster.entity.Video;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MorphToManyTest extends BaseTest {

    @Test
    public void shouldPostMorphToManyTags() {
        Post post = findById(Post.class, 1L);
        List<Tag> tags = post.get(Post::getTags);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("Java", tags.get(0).getName());
        assertEquals("Spring", tags.get(1).getName());
    }

    @Test
    public void shouldVideoMorphToManyTags() {
        Video video = findById(Video.class, 1L);
        List<Tag> tags = video.get(Video::getTags);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("Java", tags.get(0).getName());
        assertEquals("Spring", tags.get(1).getName());
    }

    @Test
    public void shouldPostMorphToManyEmpty() {
        Post post = findById(Post.class, 10L);
        List<Tag> tags = post.get(Post::getTags);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("Testing", tags.get(0).getName());
        assertEquals("DevOps", tags.get(1).getName());
    }

    @Test
    public void shouldLoadMorphToManyForList() {
        List<Post> postList = findByIds(Post.class, Arrays.asList(1L, 2L));
        assertEquals(2, postList.size());

        RelationUtils.load(postList, Post::getTags);

        Post post1 = postList.get(0);
        List<Tag> tags1 = post1.getTags();
        assertNotNull(tags1);
        assertEquals(2, tags1.size());
        assertEquals("Java", tags1.get(0).getName());
        assertEquals("Spring", tags1.get(1).getName());

        Post post2 = postList.get(1);
        List<Tag> tags2 = post2.getTags();
        assertNotNull(tags2);
        assertEquals(2, tags2.size());
        assertEquals("JavaScript", tags2.get(0).getName());
        assertEquals("React", tags2.get(1).getName());
    }
}
