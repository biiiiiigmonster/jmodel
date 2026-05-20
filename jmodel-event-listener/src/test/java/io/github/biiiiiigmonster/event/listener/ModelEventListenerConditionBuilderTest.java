package io.github.biiiiiigmonster.event.listener;

import io.github.biiiiiigmonster.Model;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModelEventListenerConditionBuilderTest {

    public static class SampleUser extends Model<SampleUser> {
    }

    public static class SamplePost extends Model<SamplePost> {
    }

    @Test
    public void buildReturnsEmptyWhenModelsAndConditionAreEmpty() {
        assertEquals("", ModelEventListenerConditionBuilder.build(new Class[0], ""));
        assertEquals("", ModelEventListenerConditionBuilder.build(null, null));
    }

    @Test
    public void buildReturnsConditionWhenModelsAreEmpty() {
        assertEquals("#event.model.status == 1",
                ModelEventListenerConditionBuilder.build(new Class[0], "#event.model.status == 1"));
    }

    @Test
    public void buildReturnsModelsConditionForSingleModel() {
        assertEquals("event.model instanceof T(io.github.biiiiiigmonster.event.listener.ModelEventListenerConditionBuilderTest$SampleUser)",
                ModelEventListenerConditionBuilder.build(new Class[]{SampleUser.class}, ""));
    }

    @Test
    public void buildJoinsMultipleModelsWithOr() {
        assertEquals(
                "event.model instanceof T(io.github.biiiiiigmonster.event.listener.ModelEventListenerConditionBuilderTest$SampleUser)"
                        + " || event.model instanceof T(io.github.biiiiiigmonster.event.listener.ModelEventListenerConditionBuilderTest$SamplePost)",
                ModelEventListenerConditionBuilder.build(new Class[]{SampleUser.class, SamplePost.class}, ""));
    }

    @Test
    public void buildMergesModelsAndConditionWithAnd() {
        assertEquals(
                "(event.model instanceof T(io.github.biiiiiigmonster.event.listener.ModelEventListenerConditionBuilderTest$SampleUser))"
                        + " && (#event.model.status == 1)",
                ModelEventListenerConditionBuilder.build(new Class[]{SampleUser.class}, "#event.model.status == 1"));
    }
}
