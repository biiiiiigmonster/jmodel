package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Comment;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.Video;
import org.junit.Test;

import java.util.Arrays;
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

    @Test
    public void shouldLoadMorphManyForList() {
        // 使用selectBatchIds获取文章列表
        List<Post> postList = postMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, postList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(postList, Post::getComments);

        // 直接使用getComments()获取已加载的数据
        Post post1 = postList.get(0);
        List<Comment> comments1 = post1.getComments();
        assertNotNull(comments1);
        assertEquals(2, comments1.size());
        assertEquals("Great tutorial!", comments1.get(0).getContext());
        assertEquals("Very helpful content", comments1.get(1).getContext());

        Post post2 = postList.get(1);
        List<Comment> comments2 = post2.getComments();
        assertNotNull(comments2);
        assertEquals(1, comments2.size());
        assertEquals("Nice explanation", comments2.get(0).getContext());
    }
}