package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.*;
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
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.save();

        Image image = new Image();
        image.setUrl("https://example.com/user-avatar.jpg");

        user.associate(User::getImage, image);

        User savedUser = findById(User.class, user.getId());
        Image savedImage = savedUser.get(User::getImage);

        assertNotNull(savedImage);
        assertEquals("https://example.com/user-avatar.jpg", savedImage.getUrl());
        assertEquals("User", savedImage.getImageableType());
        assertEquals(user.getId(), savedImage.getImageableId());
    }

    @Test
    public void shouldAssociateMorphManyRelation() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Comment comment1 = new Comment();
        comment1.setContent("Great post!");

        Comment comment2 = new Comment();
        comment2.setContent("Very helpful content");

        List<Comment> comments = Arrays.asList(comment1, comment2);

        post.associate(Post::getComments, comments);

        Post savedPost = findById(Post.class, post.getId());
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
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Tag tag1 = new Tag();
        tag1.setName("Java");
        tag1.save();

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tag2.save();

        post.attach(Post::getTags, tag1, tag2);

        Post savedPost = findById(Post.class, post.getId());
        List<Tag> tags = savedPost.get(Post::getTags);

        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> "Spring".equals(t.getName())));
    }

    @Test
    public void shouldDetachMorphToManyRelation() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Tag tag1 = new Tag();
        tag1.setName("Java");
        tag1.save();

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tag2.save();

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tag3.save();

        post.attach(Post::getTags, tag1, tag2, tag3);

        Post savedPost = findById(Post.class, post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertEquals(3, initialTags.size());

        post.detach(Post::getTags, tag1, tag2);

        Post updatedPost = findById(Post.class, post.getId());
        List<Tag> remainingTags = updatedPost.get(Post::getTags);
        assertEquals(1, remainingTags.size());
        assertEquals("Hibernate", remainingTags.get(0).getName());
    }

    @Test
    public void shouldSyncMorphToManyRelation() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Tag tag1 = new Tag();
        tag1.setName("Java");
        tag1.save();

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tag2.save();

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tag3.save();

        post.attach(Post::getTags, tag1, tag2);

        Post savedPost = findById(Post.class, post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertEquals(2, initialTags.size());

        post.sync(Post::getTags, tag2, tag3);

        Post updatedPost = findById(Post.class, post.getId());
        List<Tag> syncedTags = updatedPost.get(Post::getTags);
        assertEquals(2, syncedTags.size());
        assertTrue(syncedTags.stream().anyMatch(t -> "Spring".equals(t.getName())));
        assertTrue(syncedTags.stream().anyMatch(t -> "Hibernate".equals(t.getName())));
        assertFalse(syncedTags.stream().anyMatch(t -> "Java".equals(t.getName())));
    }

    @Test
    public void shouldSyncWithoutDetachingMorphToManyRelation() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Tag tag1 = new Tag();
        tag1.setName("Java");
        tag1.save();

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tag2.save();

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tag3.save();

        post.attach(Post::getTags, tag1, tag2);

        Post savedPost = findById(Post.class, post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertEquals(2, initialTags.size());

        post.syncWithoutDetaching(Post::getTags, tag2, tag3);

        Post updatedPost = findById(Post.class, post.getId());
        List<Tag> syncedTags = updatedPost.get(Post::getTags);
        assertEquals(3, syncedTags.size());
        assertTrue(syncedTags.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(syncedTags.stream().anyMatch(t -> "Spring".equals(t.getName())));
        assertTrue(syncedTags.stream().anyMatch(t -> "Hibernate".equals(t.getName())));
    }

    @Test
    public void shouldToggleMorphToManyRelation() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Tag javaTag = new Tag();
        javaTag.setName("Java");
        javaTag.save();

        Tag springTag = new Tag();
        springTag.setName("Spring");
        springTag.save();

        Tag hibernateTag = new Tag();
        hibernateTag.setName("Hibernate");
        hibernateTag.save();

        Post savedPost = findById(Post.class, post.getId());
        List<Tag> initialTags = savedPost.get(Post::getTags);
        assertTrue(initialTags.isEmpty());

        post.toggle(Post::getTags, javaTag);

        savedPost = findById(Post.class, post.getId());
        List<Tag> tags1 = savedPost.get(Post::getTags);
        assertEquals(1, tags1.size());
        assertEquals("Java", tags1.get(0).getName());

        post.toggle(Post::getTags, springTag);

        savedPost = findById(Post.class, post.getId());
        List<Tag> tags2 = savedPost.get(Post::getTags);
        assertEquals(2, tags2.size());
        assertTrue(tags2.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(tags2.stream().anyMatch(t -> "Spring".equals(t.getName())));

        post.toggle(Post::getTags, javaTag);

        savedPost = findById(Post.class, post.getId());
        List<Tag> tags3 = savedPost.get(Post::getTags);
        assertEquals(1, tags3.size());
        assertEquals("Spring", tags3.get(0).getName());

        post.toggle(Post::getTags, springTag, hibernateTag);

        savedPost = findById(Post.class, post.getId());
        List<Tag> tags4 = savedPost.get(Post::getTags);
        assertEquals(1, tags4.size());
        assertTrue(tags4.stream().anyMatch(t -> "Hibernate".equals(t.getName())));
    }

    @Test
    public void shouldAssociateMorphOneWithStringMethod() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Image image = new Image();
        image.setUrl("https://example.com/post-image.jpg");

        post.associate("image", image);

        Post savedPost = findById(Post.class, post.getId());
        Image savedImage = savedPost.get(Post::getImage);

        assertNotNull(savedImage);
        assertEquals("https://example.com/post-image.jpg", savedImage.getUrl());
        assertEquals("Post", savedImage.getImageableType());
        assertEquals(post.getId(), savedImage.getImageableId());
    }

    @Test
    public void shouldAttachMorphToManyWithStringMethod() {
        Video video = new Video();
        video.setTitle("Test Video");
        video.setUrl("https://example.com/video.mp4");
        video.save();

        Tag tag = new Tag();
        tag.setName("Video");
        tag.save();

        video.attach("tags", tag);

        Video savedVideo = findById(Video.class, video.getId());
        List<Tag> tags = savedVideo.get(Video::getTags);

        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("Video", tags.get(0).getName());
    }

    @Test
    public void shouldToggleMorphToManyWithStringMethod() {
        Video video = new Video();
        video.setTitle("Test Video");
        video.setUrl("https://example.com/video.mp4");
        video.save();

        Tag tag = new Tag();
        tag.setName("Video");
        tag.save();

        video.toggle("tags", tag);

        Video savedVideo = findById(Video.class, video.getId());
        List<Tag> tags1 = savedVideo.get(Video::getTags);
        assertEquals(1, tags1.size());
        assertEquals("Video", tags1.get(0).getName());

        video.toggle("tags", tag);

        savedVideo = findById(Video.class, video.getId());
        List<Tag> tags2 = savedVideo.get(Video::getTags);
        assertTrue(tags2.isEmpty());
    }

    @Test
    public void shouldAssociateMorphManyForVideo() {
        Video video = new Video();
        video.setTitle("Test Video");
        video.setUrl("https://example.com/video.mp4");
        video.save();

        Comment comment1 = new Comment();
        comment1.setContent("Great video!");

        Comment comment2 = new Comment();
        comment2.setContent("Very informative");

        List<Comment> comments = Arrays.asList(comment1, comment2);

        video.associate(Video::getComments, comments);

        Video savedVideo = findById(Video.class, video.getId());
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
        Post post = new Post();
        post.setTitle("Test Post");
        post.setUserId(1L);
        post.save();

        Tag tag1 = new Tag();
        tag1.setName("Java");
        tag1.save();

        Tag tag2 = new Tag();
        tag2.setName("Spring");
        tag2.save();

        Tag tag3 = new Tag();
        tag3.setName("Hibernate");
        tag3.save();

        List<Tag> list = Arrays.asList(tag1, tag2, tag3);
        post.toggle(Post::getTags, list);

        Post savedPost = findById(Post.class, post.getId());
        List<Tag> tags = savedPost.get(Post::getTags);
        assertEquals(3, tags.size());
        assertTrue(tags.stream().anyMatch(t -> "Java".equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> "Spring".equals(t.getName())));
        assertTrue(tags.stream().anyMatch(t -> "Hibernate".equals(t.getName())));

        post.toggle(Post::getTags, list);

        savedPost = findById(Post.class, post.getId());
        List<Tag> tags2 = savedPost.get(Post::getTags);
        assertTrue(tags2.isEmpty());
    }
}
