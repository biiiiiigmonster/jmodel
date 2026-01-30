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

    @Test
    public void shouldLoadMorphToManyForList() {
        // 使用selectBatchIds获取文章列表
        List<Post> postList = postMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, postList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(postList, Post::getTags);

        // 直接使用getTags()获取已加载的数据
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