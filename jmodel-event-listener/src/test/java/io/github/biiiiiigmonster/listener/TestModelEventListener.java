package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.ModelEventListener;
import io.github.biiiiiigmonster.entity.Post;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.event.ModelCreatedEvent;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Getter
public class TestModelEventListener {

    private final AtomicInteger userSavedCount = new AtomicInteger();
    private final AtomicInteger postSavedCount = new AtomicInteger();
    private final AtomicInteger anySavedCount = new AtomicInteger();
    private final AtomicInteger afterCommitCount = new AtomicInteger();
    private final AtomicInteger multiEventNoArgCount = new AtomicInteger();
    private volatile boolean activeUserSaved;

    public void reset() {
        userSavedCount.set(0);
        postSavedCount.set(0);
        anySavedCount.set(0);
        afterCommitCount.set(0);
        multiEventNoArgCount.set(0);
        activeUserSaved = false;
    }

    @ModelEventListener(models = User.class, fallbackExecution = true)
    public void onUserSaved(ModelSavedEvent<User> event) {
        userSavedCount.incrementAndGet();
    }

    @ModelEventListener(models = Post.class, fallbackExecution = true)
    public void onPostSaved(ModelSavedEvent<Post> event) {
        postSavedCount.incrementAndGet();
    }

    @ModelEventListener(fallbackExecution = true)
    public void onAnySaved(ModelSavedEvent<?> event) {
        anySavedCount.incrementAndGet();
    }

    @ModelEventListener(
            models = User.class,
            condition = "#event.model.name == 'active'",
            fallbackExecution = true
    )
    public void onActiveUserSaved(ModelSavedEvent<User> event) {
        activeUserSaved = true;
    }

    @ModelEventListener(models = User.class)
    public void onUserSavedAfterCommit(ModelSavedEvent<User> event) {
        afterCommitCount.incrementAndGet();
    }

    @ModelEventListener(
            models = User.class,
            classes = {ModelSavedEvent.class, ModelCreatedEvent.class},
            fallbackExecution = true
    )
    public void onUserSavedOrCreatedWithoutParameter() {
        multiEventNoArgCount.incrementAndGet();
    }
}
