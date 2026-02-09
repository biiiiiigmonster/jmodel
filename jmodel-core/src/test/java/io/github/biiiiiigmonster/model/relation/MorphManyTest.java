package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Comment;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.Video;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MorphManyTest extends BaseTest {

    @Test
    public void shouldPostMorphManyComments() {
        Post post = findById(Post.class, 1L);
        List<Comment> comments = post.get(Post::getComments);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("Great tutorial!", comments.get(0).getContent());
        assertEquals("Very helpful content", comments.get(1).getContent());
    }

    @Test
    public void shouldVideoMorphManyComments() {
        Video video = findById(Video.class, 1L);
        List<Comment> comments = video.get(Video::getComments);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Excellent video!", comments.get(0).getContent());
    }

    @Test
    public void shouldPostMorphManyEmpty() {
        Post post = findById(Post.class, 10L);
        List<Comment> comments = post.get(Post::getComments);
        assertEquals(0, comments.size());
    }

    @Test
    public void shouldLoadMorphManyForList() {
        List<Post> postList = findByIds(Post.class, Arrays.asList(1L, 2L));
        assertEquals(2, postList.size());

        RelationUtils.load(postList, Post::getComments);

        Post post1 = postList.get(0);
        List<Comment> comments1 = post1.getComments();
        assertNotNull(comments1);
        assertEquals(2, comments1.size());
        assertEquals("Great tutorial!", comments1.get(0).getContent());
        assertEquals("Very helpful content", comments1.get(1).getContent());

        Post post2 = postList.get(1);
        List<Comment> comments2 = post2.getComments();
        assertNotNull(comments2);
        assertEquals(1, comments2.size());
        assertEquals("Nice explanation", comments2.get(0).getContent());
    }
}
