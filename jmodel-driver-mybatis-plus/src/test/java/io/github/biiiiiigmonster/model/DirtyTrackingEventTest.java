package io.github.biiiiiigmonster.model;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.listener.DirtyTrackingTestListener;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * 事件监听器中使用 dirty-tracking 的集成测试 (Phase 4 - P4-06)
 * <p>
 * 验证在 Spring 事件监听器中可以正确使用 dirty-tracking API：
 * <ul>
 *   <li>在 updating 事件中 isDirty / getDirty / getOriginal 可用</li>
 *   <li>在 updated 事件中 isDirty / getDirty 可用</li>
 *   <li>在 saved 事件中 isDirty / getDirty 可用</li>
 *   <li>save() 返回后 wasChanged / getChanges 可用</li>
 * </ul>
 * <p>
 * 本测试依赖 {@link DirtyTrackingTestListener} 组件捕获事件状态。
 */
public class DirtyTrackingEventTest extends BaseTest {

    @Before
    public void resetListener() {
        DirtyTrackingTestListener.reset();
    }

    // ==================== Updating 事件测试 ====================

    /**
     * updating 事件中可以访问脏字段信息
     */
    @Test
    public void testUpdatingEvent_canAccessDirtyFields() {
        User user = userMapper.selectById(1L);
        String originalName = user.getName();
        user.syncOriginal();

        user.setName("Event Test Name");
        user.save();

        // 验证 updating 事件被触发
        assertTrue("updating 事件应被触发", DirtyTrackingTestListener.isUpdatingCaptured());

        // 验证 updating 事件中 isDirty 为 true
        assertTrue("updating 事件中 isDirty 应为 true", DirtyTrackingTestListener.isUpdatingIsDirty());

        // 验证 updating 事件中 getDirty 包含变更字段
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatingDirtyFields();
        assertNotNull("updating 事件中 getDirty 不应为 null", dirtyFields);
        assertEquals("updating 事件中 getDirty 应包含 name", "Event Test Name", dirtyFields.get("name"));

        // 验证 updating 事件中 getOriginal 包含原始值
        Map<String, Object> original = DirtyTrackingTestListener.getUpdatingOriginal();
        assertNotNull("updating 事件中 getOriginal 不应为 null", original);
        assertEquals("updating 事件中 getOriginal(\"name\") 应为原始值", originalName, original.get("name"));
    }

    // ==================== Updated 事件测试 ====================

    /**
     * updated 事件中可以访问脏字段信息
     */
    @Test
    public void testUpdatedEvent_canAccessDirtyFields() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Updated Event Test");
        user.save();

        // 验证 updated 事件被触发
        assertTrue("updated 事件应被触发", DirtyTrackingTestListener.isUpdatedCaptured());

        // updated 事件在 save 逻辑中 savedChanges/syncOriginal 之前发布，
        // 因此 isDirty 仍为 true
        assertTrue("updated 事件中 isDirty 应为 true", DirtyTrackingTestListener.isUpdatedIsDirty());

        // getDirty 应包含变更
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatedDirtyFields();
        assertNotNull("updated 事件中 getDirty 不应为 null", dirtyFields);
        assertEquals("Updated Event Test", dirtyFields.get("name"));
    }

    // ==================== Saved 事件测试 ====================

    /**
     * saved 事件中可以访问脏字段信息
     * <p>
     * 注：saved 事件在 savedChanges 赋值和 syncOriginal 之前发布，
     * 因此 isDirty 仍为 true，getDirty 仍包含变更
     */
    @Test
    public void testSavedEvent_canAccessDirtyFields() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Saved Event Test");
        user.setEmail("saved@event.com");
        user.save();

        // 验证 saved 事件被触发
        assertTrue("saved 事件应被触发", DirtyTrackingTestListener.isSavedCaptured());

        // saved 事件中 isDirty 仍为 true（尚未重置）
        assertTrue("saved 事件中 isDirty 应为 true", DirtyTrackingTestListener.isSavedIsDirty());

        // saved 事件中 getDirty 包含变更
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getSavedDirtyFields();
        assertNotNull("saved 事件中 getDirty 不应为 null", dirtyFields);
        assertEquals("Saved Event Test", dirtyFields.get("name"));
        assertEquals("saved@event.com", dirtyFields.get("email"));
    }

    // ==================== save() 返回后 wasChanged/getChanges ====================

    /**
     * save() 返回后 wasChanged / getChanges 正确（供调用者使用）
     */
    @Test
    public void testAfterSave_wasChangedAndGetChanges() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("After Save Test");
        user.save();

        // save() 返回后
        assertFalse("save 返回后 isDirty 应为 false", user.isDirty());
        assertTrue("save 返回后 wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));

        Map<String, Object> changes = user.getChanges();
        assertEquals("After Save Test", changes.get("name"));
    }

    // ==================== 事件中检测敏感字段变更 ====================

    /**
     * 模拟在事件监听器中检测敏感字段变更的场景
     */
    @Test
    public void testEventListener_detectSensitiveFieldChanges() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 修改 email（模拟敏感字段变更）
        user.setEmail("sensitive@change.com");
        user.save();

        // 验证 updating 事件中可以检测到 email 变更
        assertTrue("updating 事件应被触发", DirtyTrackingTestListener.isUpdatingCaptured());

        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatingDirtyFields();
        assertNotNull(dirtyFields);
        assertTrue("事件中应能检测到 email 变更", dirtyFields.containsKey("email"));
        assertEquals("sensitive@change.com", dirtyFields.get("email"));
    }

    // ==================== 多字段变更事件测试 ====================

    /**
     * 多字段变更时，事件中可以获取所有变更字段
     */
    @Test
    public void testEventListener_multipleFieldChanges() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        user.setName("Multi Event Name");
        user.setEmail("multi@event.com");
        user.save();

        // 验证 updating 事件中所有变更字段都可访问
        Map<String, Object> dirtyFields = DirtyTrackingTestListener.getUpdatingDirtyFields();
        assertNotNull(dirtyFields);
        assertEquals("应有 2 个脏字段", 2, dirtyFields.size());
        assertEquals("Multi Event Name", dirtyFields.get("name"));
        assertEquals("multi@event.com", dirtyFields.get("email"));
    }

    // ==================== 无变更 save 的事件行为 ====================

    /**
     * 无变更 save 时，事件中 isDirty 为 false
     */
    @Test
    public void testEventListener_noChanges_isDirtyFalse() {
        User user = userMapper.selectById(1L);
        user.syncOriginal();

        // 不做修改直接 save
        user.save();

        // updating 事件中 isDirty 应为 false
        assertTrue("updating 事件应被触发", DirtyTrackingTestListener.isUpdatingCaptured());
        assertFalse("无变更时 updating 事件中 isDirty 应为 false", DirtyTrackingTestListener.isUpdatingIsDirty());
    }
}
