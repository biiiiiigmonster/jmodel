package com.github.biiiiiigmonster.model.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Profile;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HasOneTest extends BaseTest {

    @Test
    public void shouldNotNull() {
        User user = userMapper.selectById(1L);
        Profile profile = user.get(User::getProfile);
        assertNotNull(profile);
        assertEquals("Software Engineer at Tech Corp", profile.getDescription());
    }

    @Test
    public void shouldEmpty() {
        User user = userMapper.selectById(11L);
        Profile profile = user.get(User::getProfile);
        assertNull(profile);
    }

    @Test
    public void shouldLoadHasOneForList() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(userList, User::getProfile);

        // 直接使用getProfile()获取已加载的数据
        User user1 = userList.get(0);
        Profile profile1 = user1.getProfile();
        assertNotNull(profile1);
        assertEquals("Software Engineer at Tech Corp", profile1.getDescription());

        User user2 = userList.get(1);
        Profile profile2 = user2.getProfile();
        assertNotNull(profile2);
        assertEquals("Digital Marketing Specialist", profile2.getDescription());
    }
}