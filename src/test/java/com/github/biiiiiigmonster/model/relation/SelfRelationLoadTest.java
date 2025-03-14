package com.github.biiiiiigmonster.model.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Tag;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SelfRelationLoadTest extends BaseTest {

    @Test
    public void shouldLoadParentTest() {
        List<Tag> tags = tagMapper.selectBatchIds(Arrays.asList(2L, 4L));
        assertEquals(2, tags.size());

        RelationUtils.load(tags, Tag::getParent);

        Tag tag1 = tags.get(0);
        Tag parent1 = tag1.getParent();
        assertNotNull(parent1);
        assertEquals("Java", parent1.getName());

        Tag tag2 = tags.get(1);
        Tag parent2 = tag2.getParent();
        assertNotNull(parent2);
        assertEquals("JavaScript", parent2.getName());
    }

    @Test
    public void shouldLoadChildrenTest() {
        List<Tag> tags = tagMapper.selectBatchIds(Arrays.asList(1L, 3L));
        assertEquals(2, tags.size());

        RelationUtils.load(tags, Tag::getChildren);

        Tag tag1 = tags.get(0);
        List<Tag> children1 = tag1.getChildren();
        assertEquals(2, children1.size());
        assertEquals("Spring", children1.get(0).getName());
        assertEquals("Database", children1.get(1).getName());

        Tag tag2 = tags.get(1);
        List<Tag> children2 = tag2.getChildren();
        assertEquals(1, children2.size());
        assertEquals("React", children2.get(0).getName());
    }

    @Test
    public void shouldParentNullTest() {
        Tag tag = tagMapper.selectById(1L);
        assertNotNull(tag);

        tag.load(Tag::getParent);

        assertNull(tag.getParent());
    }

    @Test
    public void shouldChildrenEmptyTest() {
        Tag tag = tagMapper.selectById(10L);
        assertNotNull(tag);

        tag.load(Tag::getChildren);

        List<Tag> children = tag.getChildren();
        assertNotNull(children);
        assertEquals(0, children.size());
    }
}
