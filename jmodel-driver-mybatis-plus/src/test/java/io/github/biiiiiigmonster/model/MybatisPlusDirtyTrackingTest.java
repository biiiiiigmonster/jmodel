package io.github.biiiiiigmonster.model;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * MyBatis-Plus 特有的 Dirty Tracking 测试。
 * 仅保留依赖 QueryWrapper / selectList / selectOne 等 MyBatis-Plus API 的测试用例。
 * 通用 dirty tracking 测试已迁移到 jmodel-core 模块。
 */
public class MybatisPlusDirtyTrackingTest extends BaseTest {

    /**
     * P3-02: selectList + QueryWrapper 查询后的 dirty tracking
     */
    @Test
    public void testSelectList_dirtyTracking() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", "John Doe");
        List<User> users = userMapper.selectList(wrapper);

        assertFalse("查询结果不应为空", users.isEmpty());
        User user = users.get(0);

        user.syncOriginal();
        user.setName("Wrapper Query Update");

        assertTrue("isDirty 应为 true", user.isDirty());
        assertTrue("isDirty(\"name\") 应为 true", user.isDirty("name"));

        Map<String, Object> dirty = user.getDirty();
        assertEquals("Wrapper Query Update", dirty.get("name"));
    }

    /**
     * P3-02: selectOne + QueryWrapper 查询后的 dirty tracking
     */
    @Test
    public void testSelectOne_dirtyTracking() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id", 2L);
        User user = userMapper.selectOne(wrapper);

        assertNotNull(user);
        String originalEmail = user.getEmail();

        user.syncOriginal();
        user.setEmail("selectone@test.com");

        assertTrue("isDirty 应为 true", user.isDirty());
        assertEquals("getOriginal 应返回原始邮箱", originalEmail, user.getOriginal("email"));
    }

    /**
     * P3-02: selectList 查询多个实体，各自独立追踪
     */
    @Test
    public void testSelectList_independentTracking() {
        List<User> users = userMapper.selectList(null);
        assertTrue("应有多个用户", users.size() >= 2);

        User user1 = users.get(0);
        User user2 = users.get(1);

        user1.syncOriginal();
        user2.syncOriginal();

        // 只修改 user1
        user1.setName("User1 Modified");

        assertTrue("user1 isDirty 应为 true", user1.isDirty());
        assertFalse("user2 isDirty 应为 false", user2.isDirty());
    }

    /**
     * P3-02: selectList 查询 → syncOriginal → save → wasChanged
     */
    @Test
    public void testSelectList_save_wasChanged() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id", 3L);
        User user = userMapper.selectOne(wrapper);
        assertNotNull(user);

        user.syncOriginal();
        user.setName("Wrapper Save Test");
        user.save();

        assertTrue("wasChanged() 应为 true", user.wasChanged());
        assertTrue("wasChanged(\"name\") 应为 true", user.wasChanged("name"));
        assertFalse("save 后 isDirty 应为 false", user.isDirty());
    }
}
