package io.github.biiiiiigmonster.driver.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.driver.entity.Post;
import io.github.biiiiiigmonster.driver.entity.Region;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HasOneDeepTest extends BaseTest {

    @Test
    public void shouldHasOneDeepNotNull() {
        Post post = findById(Post.class, 1L);
        Region region = post.get(Post::getAuthorRegion);
        assertNotNull(region);
        assertEquals("East Coast", region.getName());
    }

    @Test
    public void shouldHasOneDeepDifferentRegion() {
        Post post = findById(Post.class, 3L);
        Region region = post.get(Post::getAuthorRegion);
        assertNotNull(region);
        assertEquals("West Coast", region.getName());
    }

    @Test
    public void shouldHasOneDeepEmpty() {
        Post post = findById(Post.class, 10L);
        Region region = post.get(Post::getAuthorRegion);
        assertNull(region);
    }

    @Test
    public void shouldLoadHasOneDeepForList() {
        List<Post> postList = findByIds(Post.class, Arrays.asList(1L, 3L, 5L, 10L));
        assertEquals(4, postList.size());

        RelationUtils.load(postList, Post::getAuthorRegion);

        assertEquals("East Coast", postList.get(0).getAuthorRegion().getName());
        assertEquals("West Coast", postList.get(1).getAuthorRegion().getName());
        assertEquals("Midwest", postList.get(2).getAuthorRegion().getName());
        assertNull(postList.get(3).getAuthorRegion());
    }
}
