package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import io.github.biiiiiigmonster.event.ModelUpdatedEvent;
import io.github.biiiiiigmonster.event.ModelUpdatingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 测试用事件监听器，用于捕获 dirty-tracking 事件中的状态。
 * <p>
 * 在事件处理器中记录 model 的 isDirty、getDirty、wasChanged、getChanges 等信息，
 * 供 {@link io.github.biiiiiigmonster.model.DirtyTrackingEventTest} 断言验证。
 * <p>
 * 使用前需调用 {@link #reset()} 清空捕获的状态。
 * <p>
 * 注意：由于 Java 泛型擦除，事件监听器方法签名中的泛型参数在运行时不生效，
 * 因此需要在方法内部显式检查 model 类型，避免非 User 实体触发 ClassCastException。
 */
@Component
public class DirtyTrackingTestListener {

    // ==================== Updating 事件捕获 ====================

    private static volatile boolean updatingCaptured = false;
    private static volatile boolean updatingIsDirty = false;
    private static volatile Map<String, Object> updatingDirtyFields = null;
    private static volatile Map<String, Object> updatingOriginal = null;

    // ==================== Updated 事件捕获 ====================

    private static volatile boolean updatedCaptured = false;
    private static volatile boolean updatedIsDirty = false;
    private static volatile Map<String, Object> updatedDirtyFields = null;

    // ==================== Saved 事件捕获 ====================

    private static volatile boolean savedCaptured = false;
    private static volatile boolean savedIsDirty = false;
    private static volatile Map<String, Object> savedDirtyFields = null;

    /**
     * 重置所有捕获的状态
     */
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

    // ==================== 事件处理器 ====================

    @SuppressWarnings("rawtypes")
    @EventListener
    public void onUpdating(ModelUpdatingEvent event) {
        if (!(event.getModel() instanceof User)) {
            return;
        }
        User user = (User) event.getModel();
        updatingCaptured = true;
        updatingIsDirty = user.isDirty();
        updatingDirtyFields = user.getDirty();
        updatingOriginal = user.getOriginal();
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void onUpdated(ModelUpdatedEvent event) {
        if (!(event.getModel() instanceof User)) {
            return;
        }
        User user = (User) event.getModel();
        updatedCaptured = true;
        updatedIsDirty = user.isDirty();
        updatedDirtyFields = user.getDirty();
    }

    @SuppressWarnings("rawtypes")
    @EventListener
    public void onSaved(ModelSavedEvent event) {
        if (!(event.getModel() instanceof User)) {
            return;
        }
        User user = (User) event.getModel();
        savedCaptured = true;
        savedIsDirty = user.isDirty();
        savedDirtyFields = user.getDirty();
    }

    // ==================== Getters ====================

    public static boolean isUpdatingCaptured() {
        return updatingCaptured;
    }

    public static boolean isUpdatingIsDirty() {
        return updatingIsDirty;
    }

    public static Map<String, Object> getUpdatingDirtyFields() {
        return updatingDirtyFields;
    }

    public static Map<String, Object> getUpdatingOriginal() {
        return updatingOriginal;
    }

    public static boolean isUpdatedCaptured() {
        return updatedCaptured;
    }

    public static boolean isUpdatedIsDirty() {
        return updatedIsDirty;
    }

    public static Map<String, Object> getUpdatedDirtyFields() {
        return updatedDirtyFields;
    }

    public static boolean isSavedCaptured() {
        return savedCaptured;
    }

    public static boolean isSavedIsDirty() {
        return savedIsDirty;
    }

    public static Map<String, Object> getSavedDirtyFields() {
        return savedDirtyFields;
    }
}
