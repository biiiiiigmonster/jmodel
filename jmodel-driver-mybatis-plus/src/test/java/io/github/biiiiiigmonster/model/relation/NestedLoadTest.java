package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Phone;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.Tag;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationOption;
import io.github.biiiiiigmonster.relation.RelationUtils;
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
    public void shouldLoadPostsWithTagsByRelationOption() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationOption.of设置嵌套关联关系
        RelationOption<User> option = RelationOption.of(User::getPosts).appendNested(Post::getComments, Post::getTags);
        // 使用RelationUtils.load加载嵌套关联数据
        RelationUtils.load(userList, option);

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
    public void shouldLoadPostsWithTagsWhenOneOfPostsEmpty() {
        // 使用selectBatchIds获取用户列表，包含一个没有关联文章的用户
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 10L));
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

        // 验证没有关联文章的用户
        User user2 = userList.get(1);
        List<Post> posts2 = user2.getPosts();
        assertEquals(0, posts2.size());
    }

    @Test
    public void shouldLoadPostsWithTagsWhenPostsEmpty() {
        // 使用selectBatchIds获取用户列表，包含一个没有关联文章的用户
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(10L, 11L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载嵌套关联数据
        RelationUtils.load(userList, "posts.tags");

        User user1 = userList.get(0);
        List<Post> posts1 = user1.getPosts();
        assertEquals(0, posts1.size());

        User user2 = userList.get(1);
        List<Post> posts2 = user2.getPosts();
        assertEquals(0, posts2.size());
    }

    @Test
    public void shouldLoadPhoneWithTagsByRelated() {
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 3L, 4L));
        assertEquals(3, userList.size());

        // 使用RelationUtils.load加载嵌套关联数据
        RelationUtils.load(userList, "phone.tags");

        User user1 = userList.get(0);
        Phone phone1 = user1.getPhone();
        assertNotNull(phone1);

        List<Tag> tags1 = phone1.getTags();
        assertNotNull(tags1);
        assertEquals(2, tags1.size());
        assertEquals("Java", tags1.get(0).getName());
        assertEquals("Spring", tags1.get(1).getName());

        User user3 = userList.get(1);
        Phone phone3 = user3.getPhone();
        assertNotNull(phone3);

        List<Tag> tags3 = phone3.getTags();
        assertNotNull(tags3);
        assertEquals(1, tags3.size());
        assertEquals("Docker", tags3.get(0).getName());

        User user4 = userList.get(2);
        Phone phone4 = user4.getPhone();
        assertNotNull(phone4);

        List<Tag> tags4 = phone4.getTags();
        assertNotNull(tags4);
        assertEquals(0, tags4.size());
    }
}
