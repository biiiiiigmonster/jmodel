package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import io.github.biiiiiigmonster.event.ModelUpdatedEvent;
import io.github.biiiiiigmonster.event.ModelUpdatingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DirtyTrackingTestListener {

    private static volatile boolean updatingCaptured = false;
    private static volatile boolean updatingIsDirty = false;
    private static volatile Map<String, Object> updatingDirtyFields = null;
    private static volatile Map<String, Object> updatingOriginal = null;

    private static volatile boolean updatedCaptured = false;
    private static volatile boolean updatedIsDirty = false;
    private static volatile Map<String, Object> updatedDirtyFields = null;

    private static volatile boolean savedCaptured = false;
    private static volatile boolean savedIsDirty = false;
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

    public static boolean isUpdatingCaptured() { return updatingCaptured; }
    public static boolean isUpdatingIsDirty() { return updatingIsDirty; }
    public static Map<String, Object> getUpdatingDirtyFields() { return updatingDirtyFields; }
    public static Map<String, Object> getUpdatingOriginal() { return updatingOriginal; }
    public static boolean isUpdatedCaptured() { return updatedCaptured; }
    public static boolean isUpdatedIsDirty() { return updatedIsDirty; }
    public static Map<String, Object> getUpdatedDirtyFields() { return updatedDirtyFields; }
    public static boolean isSavedCaptured() { return savedCaptured; }
    public static boolean isSavedIsDirty() { return savedIsDirty; }
    public static Map<String, Object> getSavedDirtyFields() { return savedDirtyFields; }
}
