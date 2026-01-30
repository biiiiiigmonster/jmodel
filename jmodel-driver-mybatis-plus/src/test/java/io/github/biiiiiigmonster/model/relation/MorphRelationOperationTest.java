package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.*;
import io.github.biiiiiigmonster.entity.Comment;
import io.github.biiiiiigmonster.entity.Image;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.Tag;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.entity.Video;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MorphRelationOperationTest extends BaseTest {

    @Test
    public void shouldAssociateMorphOneRelation() {
        // 创建用户
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        // 创建图片
        Image image = new Image();
        image.setUrl("https://example.com/user-avatar.jpg");

        // 建立多态一对一关联
        user.associate(User::getImage, image);

        // 验证关联已保存
        User savedUser = userMapper.selectById(user.getId());
        Image savedImage = savedUser.get(User::getImage);

        assertNotNull(savedImage);
        assertEquals("https://example.com/user-avatar.jpg", savedImage.getUrl());
        assertEquals("User", savedImage.getImageableType());
        assertEquals(user.getId(), savedImage.getImageableId());
    }

    @Test
    public void shouldAssociateMorphManyRelation() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建评论列表
        Comment comment1 = new Comment();
        comment1.setContent("Great post!");

        Comment comment2 = new Comment();
        comment2.setContent("Very helpful content");

        List<Comment> comments = Arrays.asList(comment1, comment2);

        // 建立多态一对多关联
        post.associate(Post::getComments, comments);

        // 验证关联已保存
        Post savedPost = postMapper.selectById(post.getId());
        List<Comment> savedComments = savedPost.get(Post::getComments);

        assertNotNull(savedComments);
        assertEquals(2, savedComments.size());
        assertEquals("Great post!", savedComments.get(0).getContent());
        assertEquals("Very helpful content", savedComments.get(1).getContent());
        assertEquals("Post", savedComments.get(0).getCommentableType());
        assertEquals("Post", savedComments.get(1).getCommentableType());
        assertEquals(post.getId(), savedComments.get(0).getCommentableId());
        assertEquals(post.getId(), savedComments.get(1).getCommentableId());
    }

    @Test
    public void shouldAttachMorphToManyRelation() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建标签
        Tag tag1 = new Tag();
        tag1.setName("Java");
        tagMapper.insert(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tagMapper.insert(tag2);

        // 附加多态多对多关联
        post.attach(Post::getTags, tag1, tag2);

        // 验证关联已创建
        Post savedPost = postMapper.selectById(post.getId());
        List<Tag> tags = savedPost.get(Post::getTags);

        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> "Spring".equals(t.getName())));
    }

    @Test
    public void shouldDetachMorphToManyRelation() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建标签
        Tag tag1 = new Tag();
        tag1.setName("Java");
        tagMapper.insert(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tagMapper.insert(tag2);

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tagMapper.insert(tag3);

        // 先附加所有标签
        post.attach(Post::getTags, tag1, tag2, tag3);

        // 验证初始状态
        Post savedPost = postMapper.selectById(post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertEquals(3, initialTags.size());

        // 分离指定标签
        post.detach(Post::getTags, tag1, tag2);

        // 验证分离后的状态
        Post updatedPost = postMapper.selectById(post.getId());
        List<Tag> remainingTags = updatedPost.get(Post::getTags);
        assertEquals(1, remainingTags.size());
        assertEquals("Hibernate", remainingTags.get(0).getName());
    }

    @Test
    public void shouldSyncMorphToManyRelation() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建标签
        Tag tag1 = new Tag();
        tag1.setName("Java");
        tagMapper.insert(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tagMapper.insert(tag2);

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tagMapper.insert(tag3);

        // 先附加标签1和2
        post.attach(Post::getTags, tag1, tag2);

        // 验证初始状态
        Post savedPost = postMapper.selectById(post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertEquals(2, initialTags.size());

        // 同步为标签2和3
        post.sync(Post::getTags, tag2, tag3);

        // 验证同步后的状态
        Post updatedPost = postMapper.selectById(post.getId());
        List<Tag> syncedTags = updatedPost.get(Post::getTags);
        assertEquals(2, syncedTags.size());
        assertTrue(syncedTags.stream().anyMatch(t -> "Spring".equals(t.getName())));
        assertTrue(syncedTags.stream().anyMatch(t -> "Hibernate".equals(t.getName())));
        assertFalse(syncedTags.stream().anyMatch(t -> "Java".equals(t.getName())));
    }

    @Test
    public void shouldSyncWithoutDetachingMorphToManyRelation() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建标签
        Tag tag1 = new Tag();
        tag1.setName("Java");
        tagMapper.insert(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tagMapper.insert(tag2);

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tagMapper.insert(tag3);

        // 先附加标签1和2
        post.attach(Post::getTags, tag1, tag2);

        // 验证初始状态
        Post savedPost = postMapper.selectById(post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertEquals(2, initialTags.size());

        // 同步为标签2和3（不移除现有关联）
        post.syncWithoutDetaching(Post::getTags, tag2, tag3);

        // 验证同步后的状态（应该包含所有标签）
        Post updatedPost = postMapper.selectById(post.getId());
        List<Tag> syncedTags = updatedPost.get(Post::getTags);
        assertEquals(3, syncedTags.size());
        assertTrue(syncedTags.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(syncedTags.stream().anyMatch(t -> "Spring".equals(t.getName())));
        assertTrue(syncedTags.stream().anyMatch(t -> "Hibernate".equals(t.getName())));
    }

    @Test
    public void shouldToggleMorphToManyRelation() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建标签
        Tag javaTag = new Tag();
        javaTag.setName("Java");
        tagMapper.insert(javaTag);

        Tag springTag = new Tag();
        springTag.setName("Spring");
        tagMapper.insert(springTag);

        Tag hibernateTag = new Tag();
        hibernateTag.setName("Hibernate");
        tagMapper.insert(hibernateTag);

        // 初始状态：没有任何标签
        Post savedPost = postMapper.selectById(post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertTrue(initialTags.isEmpty());

        // 切换Java标签（添加）
        post.toggle(Post::getTags, javaTag);

        // 验证关联已添加
        savedPost = postMapper.selectById(post.getId());
        List<Tag> tags1 = savedPost.get(Post::getTags);
        assertEquals(1, tags1.size());
        assertEquals("Java", tags1.get(0).getName());

        // 切换Spring标签（添加）
        post.toggle(Post::getTags, springTag);

        // 验证关联已添加
        savedPost = postMapper.selectById(post.getId());
        List<Tag> tags2 = savedPost.get(Post::getTags);
        assertEquals(2, tags2.size());
        assertTrue(tags2.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(tags2.stream().anyMatch(t -> "Spring".equals(t.getName())));

        // 切换Java标签（移除，因为已存在）
        post.toggle(Post::getTags, javaTag);

        // 验证关联已移除
        savedPost = postMapper.selectById(post.getId());
        List<Tag> tags3 = savedPost.get(Post::getTags);
        assertEquals(1, tags3.size());
        assertEquals("Spring", tags3.get(0).getName());

        // 切换多个标签
        post.toggle(Post::getTags, springTag, hibernateTag);

        // 验证多个关联已添加
        savedPost = postMapper.selectById(post.getId());
        List<Tag> tags4 = savedPost.get(Post::getTags);
        assertEquals(1, tags4.size());
        assertTrue(tags4.stream().anyMatch(t -> "Hibernate".equals(t.getName())));
    }

    @Test
    public void shouldAssociateMorphOneWithStringMethod() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建图片
        Image image = new Image();
        image.setUrl("https://example.com/post-image.jpg");

        // 使用字符串方式建立多态一对一关联
        post.associate("image", image);

        // 验证关联已保存
        Post savedPost = postMapper.selectById(post.getId());
        Image savedImage = savedPost.get(Post::getImage);

        assertNotNull(savedImage);
        assertEquals("https://example.com/post-image.jpg", savedImage.getUrl());
        assertEquals("Post", savedImage.getImageableType());
        assertEquals(post.getId(), savedImage.getImageableId());
    }

    @Test
    public void shouldAttachMorphToManyWithStringMethod() {
        // 创建视频
        Video video = new Video();
        video.setTitle("Test Video");
        video.setUrl("https://example.com/video.mp4");
        video.save();

        // 创建标签
        Tag tag = new Tag();
        tag.setName("Video");
        tagMapper.insert(tag);

        // 使用字符串方式附加多态多对多关联
        video.attach("tags", tag);

        // 验证关联已创建
        Video savedVideo = videoMapper.selectById(video.getId());
        List<Tag> tags = savedVideo.get(Video::getTags);

        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("Video", tags.get(0).getName());
    }

    @Test
    public void shouldToggleMorphToManyWithStringMethod() {
        // 创建视频
        Video video = new Video();
        video.setTitle("Test Video");
        video.setUrl("https://example.com/video.mp4");
        video.save();

        // 创建标签
        Tag tag = new Tag();
        tag.setName("Video");
        tagMapper.insert(tag);

        // 使用字符串方式切换标签
        video.toggle("tags", tag);

        // 验证关联已添加
        Video savedVideo = videoMapper.selectById(video.getId());
        List<Tag> tags1 = savedVideo.get(Video::getTags);
        assertEquals(1, tags1.size());
        assertEquals("Video", tags1.get(0).getName());

        // 再次切换同一个标签（移除）
        video.toggle("tags", tag);

        // 验证关联已移除
        savedVideo = videoMapper.selectById(video.getId());
        List<Tag> tags2 = savedVideo.get(Video::getTags);
        assertTrue(tags2.isEmpty());
    }

    @Test
    public void shouldAssociateMorphManyForVideo() {
        // 创建视频
        Video video = new Video();
        video.setTitle("Test Video");
        video.setUrl("https://example.com/video.mp4");
        video.save();

        // 创建评论列表
        Comment comment1 = new Comment();
        comment1.setContent("Great video!");

        Comment comment2 = new Comment();
        comment2.setContent("Very informative");

        List<Comment> comments = Arrays.asList(comment1, comment2);

        // 建立多态一对多关联
        video.associate(Video::getComments, comments);

        // 验证关联已保存
        Video savedVideo = videoMapper.selectById(video.getId());
        List<Comment> savedComments = savedVideo.get(Video::getComments);

        assertNotNull(savedComments);
        assertEquals(2, savedComments.size());
        assertEquals("Great video!", savedComments.get(0).getContent());
        assertEquals("Very informative", savedComments.get(1).getContent());
        assertEquals("Video", savedComments.get(0).getCommentableType());
        assertEquals("Video", savedComments.get(1).getCommentableType());
        assertEquals(video.getId(), savedComments.get(0).getCommentableId());
        assertEquals(video.getId(), savedComments.get(1).getCommentableId());
    }

    @Test
    public void shouldToggleMultipleMorphToManyIds() {
        // 创建帖子
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        // 创建多个标签
        Tag tag1 = new Tag();
        tag1.setName("Java");
        tagMapper.insert(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tagMapper.insert(tag2);

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tagMapper.insert(tag3);

        // 切换多个
        List<Tag> list = Arrays.asList(tag1, tag2, tag3);
        post.toggle(Post::getTags, list);

        // 验证所有关联都已添加
        Post savedPost = postMapper.selectById(post.getId());
        List<Tag> tags = savedPost.get(Post::getTags);
        assertEquals(3, tags.size());
        assertTrue(tags.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> "Spring".equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> "Hibernate".equals(t.getName())));

        // 再次切换相同的ID（应该全部移除）
        post.toggle(Post::getTags, list);

        // 验证所有关联都已移除
        savedPost = postMapper.selectById(post.getId());
        List<Tag> tags2 = savedPost.get(Post::getTags);
        assertTrue(tags2.isEmpty());
    }
}
