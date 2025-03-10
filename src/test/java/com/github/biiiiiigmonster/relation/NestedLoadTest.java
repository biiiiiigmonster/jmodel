package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Comment;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.Tag;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NestedLoadTest extends BaseTest {

    @Test
    public void shouldLoadPostsWithTags() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载嵌套关联数据
        RelationUtils.load(userList, "posts.tags");

        // 验证第一个用户的文章标签
        User user1 = userList.get(0);
        List<Post> posts1 = user1.getPosts();
        assertNotNull(posts1);
        assertEquals(2, posts1.size());

        Post post1 = posts1.get(0);
        List<Tag> tags1 = post1.getTags();
        assertNotNull(tags1);
        assertEquals(2, tags1.size());
        assertEquals("Java", tags1.get(0).getName());
        assertEquals("Spring", tags1.get(1).getName());

        Post post2 = posts1.get(1);
        List<Tag> tags2 = post2.getTags();
        assertNotNull(tags2);
        assertEquals(2, tags2.size());
        assertEquals("JavaScript", tags2.get(0).getName());
        assertEquals("React", tags2.get(1).getName());

        // 验证第二个用户的文章标签
        User user2 = userList.get(1);
        List<Post> posts2 = user2.getPosts();
        assertNotNull(posts2);
        assertEquals(2, posts2.size());

        Post post3 = posts2.get(0);
        List<Tag> tags3 = post3.getTags();
        assertNotNull(tags3);
        assertEquals(2, tags3.size());
        assertEquals("Docker", tags3.get(0).getName());
        assertEquals("Cloud", tags3.get(1).getName());
    }

    @Test
    public void shouldLoadPostsWithComments() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载嵌套关联数据
        RelationUtils.load(userList, "posts.comments");

        // 验证第一个用户的文章评论
        User user1 = userList.get(0);
        List<Post> posts1 = user1.getPosts();
        assertNotNull(posts1);
        assertEquals(2, posts1.size());

        Post post1 = posts1.get(0);
        List<Comment> comments1 = post1.getComments();
        assertNotNull(comments1);
        assertEquals(2, comments1.size());
        assertEquals("Great tutorial!", comments1.get(0).getContent());
        assertEquals("Very helpful content", comments1.get(1).getContent());

        Post post2 = posts1.get(1);
        List<Comment> comments2 = post2.getComments();
        assertNotNull(comments2);
        assertEquals(1, comments2.size());
        assertEquals("Nice explanation", comments2.get(0).getContent());

        // 验证第二个用户的文章评论
        User user2 = userList.get(1);
        List<Post> posts2 = user2.getPosts();
        assertNotNull(posts2);
        assertEquals(2, posts2.size());

        Post post3 = posts2.get(0);
        List<Comment> comments3 = post3.getComments();
        assertNotNull(comments3);
        assertEquals(1, comments3.size());
        assertEquals("Looking forward to more", comments3.get(0).getContent());
    }
}
