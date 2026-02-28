package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import io.github.biiiiiigmonster.event.ModelUpdatedEvent;
import io.github.biiiiiigmonster.event.ModelUpdatingEvent;
import lombok.Getter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DirtyTrackingTestListener {

    @Getter
    private static volatile boolean updatingCaptured = false;
    @Getter
    private static volatile boolean updatingIsDirty = false;
    @Getter
    private static volatile Map<String, Object> updatingDirtyFields = null;
    @Getter
    private static volatile Map<String, Object> updatingOriginal = null;

    @Getter
    private static volatile boolean updatedCaptured = false;
    @Getter
    private static volatile boolean updatedIsDirty = false;
    @Getter
    private static volatile Map<String, Object> updatedDirtyFields = null;

    @Getter
    private static volatile boolean savedCaptured = false;
    @Getter
    private static volatile boolean savedIsDirty = false;
    @Getter
    private static volatile Map<String, Object> savedDirtyFields = null;

    public static void reset() {
        updatingCaptured = false;
        updatingIsDirty = false;
        updatingDirtyFields = null;
        updatingOriginal = null;
        updatedCaptured = false;
        updatedIsDirty = false;
        updatedDirtyFields = null;
        savedCaptured = false;
        savedIsDirty = false;
        savedDirtyFields = null;
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void onUpdating(ModelUpdatingEvent event) {
        if (!(event.getModel() instanceof User)) return;
        User user = (User) event.getModel();
        updatingCaptured = true;
        updatingIsDirty = user.isDirty();
        updatingDirtyFields = user.getDirty();
        updatingOriginal = user.getOriginal();
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void onUpdated(ModelUpdatedEvent event) {
        if (!(event.getModel() instanceof User)) return;
        User user = (User) event.getModel();
        updatedCaptured = true;
        updatedIsDirty = user.isDirty();
        updatedDirtyFields = user.getDirty();
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void onSaved(ModelSavedEvent event) {
        if (!(event.getModel() instanceof User)) return;
        User user = (User) event.getModel();
        savedCaptured = true;
        savedIsDirty = user.isDirty();
        savedDirtyFields = user.getDirty();
    }
}
