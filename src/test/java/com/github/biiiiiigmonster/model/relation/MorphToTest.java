package com.github.biiiiiigmonster.model.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Comment;
import com.github.biiiiiigmonster.entity.Image;
import com.github.biiiiiigmonster.entity.Post;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MorphToTest extends BaseTest {

    @Test
    public void shouldCommentMorphToPost() {
        Comment comment = commentMapper.selectById(1L);
        Post post = comment.get(Comment::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }

    @Test
    public void shouldImageMorphToUser() {
        Image image = imageMapper.selectById(1L);
        User user = image.get(Image::getUser);
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void shouldImageMorphToPost() {
        Image image = imageMapper.selectById(2L);
        Post post = image.get(Image::getPost);
        assertNotNull(post);
        assertEquals("Getting Started with Spring Boot", post.getTitle());
    }

    @Test
    public void shouldLoadMorphToForList() {
        // 使用selectBatchIds获取评论列表
        List<Comment> commentList = commentMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, commentList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(commentList, Comment::getPost);

        // 直接使用getPost()获取已加载的数据
        Comment comment1 = commentList.get(0);
        Post post1 = comment1.getPost();
        assertNotNull(post1);
        assertEquals("Getting Started with Spring Boot", post1.getTitle());

        Comment comment2 = commentList.get(1);
        Post post2 = comment2.getPost();
        assertNotNull(post2);
        assertEquals("Getting Started with Spring Boot", post2.getTitle());
    }
}