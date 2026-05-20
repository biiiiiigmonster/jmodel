package io.github.biiiiiigmonster.event.listener;

import io.github.biiiiiigmonster.event.ModelCreatedEvent;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ModelEventListenerTestConfiguration.class)
public class ModelEventListenerIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestModelEventListener listener;

    @Before
    public void setUp() {
        listener.reset();
    }

    @Test
    public void filtersEventsByModelType() {
        TestUser user = new TestUser();
        TestPost post = new TestPost();

        applicationContext.publishEvent(new ModelSavedEvent<>(user, user));
        applicationContext.publishEvent(new ModelSavedEvent<>(post, post));

        assertEquals(1, listener.getUserSavedCount().get());
        assertEquals(1, listener.getPostSavedCount().get());
        assertEquals(2, listener.getAnySavedCount().get());
    }

    @Test
    public void mergesModelsConditionWithCustomCondition() {
        TestUser activeUser = new TestUser();
        activeUser.setName("active");
        TestUser inactiveUser = new TestUser();
        inactiveUser.setName("inactive");

        applicationContext.publishEvent(new ModelSavedEvent<>(activeUser, activeUser));
        applicationContext.publishEvent(new ModelSavedEvent<>(inactiveUser, inactiveUser));

        assertTrue(listener.isActiveUserSaved());
        assertEquals(2, listener.getUserSavedCount().get());
    }

    @Test
    public void supportsNoArgListenerWithClassesAttribute() {
        TestUser user = new TestUser();

        applicationContext.publishEvent(new ModelSavedEvent<>(user, user));
        applicationContext.publishEvent(new ModelCreatedEvent<>(user, user));
        applicationContext.publishEvent(new ModelSavedEvent<>(new TestPost(), new TestPost()));

        assertEquals(2, listener.getMultiEventNoArgCount().get());
    }

    @Test
    public void skipsListenerWithoutFallbackWhenNoTransactionIsActive() {
        TestUser user = new TestUser();

        applicationContext.publishEvent(new ModelSavedEvent<>(user, user));

        assertEquals(1, listener.getUserSavedCount().get());
        assertEquals(0, listener.getAfterCommitCount().get());
    }
}
