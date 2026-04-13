package io.github.biiiiiigmonster.driver.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.driver.entity.Post;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SiblingManyTest extends BaseTest {

    @Test
    public void shouldGetSiblings() {
        Post post = findById(Post.class, 1L);
        List<Post> siblings = post.get(Post::getSiblingsUserPosts);
        assertNotNull(siblings);
        assertEquals(1, siblings.size());
        assertEquals("Mastering JPA Relationships", siblings.get(0).getTitle());
    }

    @Test
    public void shouldGetSiblingsEmpty() {
        Post post = findById(Post.class, 10L);
        List<Post> siblings = post.get(Post::getSiblingsUserPosts);
        assertNotNull(siblings);
        assertEquals(0, siblings.size());
    }

    @Test
    public void shouldGetSiblingsExcludeSelf() {
        Post post = findById(Post.class, 1L);
        List<Post> siblings = post.get(Post::getSiblingsUserPosts);
        Set<Long> siblingIds = siblings.stream().map(Post::getId).collect(Collectors.toSet());
        assertFalse(siblingIds.contains(post.getId()));
    }

    @Test
    public void shouldLoadSiblingsForList() {
        List<Post> postList = findByIds(Post.class, Arrays.asList(1L, 3L, 10L));
        assertEquals(3, postList.size());

        RelationUtils.load(postList, Post::getSiblingsUserPosts);

        Post post1 = postList.stream().filter(p -> p.getId().equals(1L)).findFirst().orElse(null);
        assertNotNull(post1);
        List<Post> siblings1 = post1.getSiblingsUserPosts();
        assertNotNull(siblings1);
        assertEquals(1, siblings1.size());
        assertEquals("Mastering JPA Relationships", siblings1.get(0).getTitle());

        Post post3 = postList.stream().filter(p -> p.getId().equals(3L)).findFirst().orElse(null);
        assertNotNull(post3);
        List<Post> siblings3 = post3.getSiblingsUserPosts();
        assertNotNull(siblings3);
        assertEquals(1, siblings3.size());
        assertEquals("Advanced TypeScript Patterns", siblings3.get(0).getTitle());

        Post post10 = postList.stream().filter(p -> p.getId().equals(10L)).findFirst().orElse(null);
        assertNotNull(post10);
        List<Post> siblings10 = post10.getSiblingsUserPosts();
        assertNotNull(siblings10);
        assertEquals(0, siblings10.size());
    }

    @Test
    public void shouldGetMultipleSiblings() {
        Post post = findById(Post.class, 5L);
        List<Post> siblings = post.get(Post::getSiblingsUserPosts);
        assertNotNull(siblings);
        assertEquals(2, siblings.size());

        Set<String> siblingTitles = siblings.stream().map(Post::getTitle).collect(Collectors.toSet());
        assertTrue(siblingTitles.contains("Kubernetes in Practice"));
        assertTrue(siblingTitles.contains("CI/CD Pipeline Design"));

        Set<Long> siblingIds = siblings.stream().map(Post::getId).collect(Collectors.toSet());
        assertFalse(siblingIds.contains(5L));
    }
}
