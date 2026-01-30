package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Address;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HasOneThroughTest extends BaseTest {

    @Test
    public void shouldHasOneThroughNotNull() {
        User user = userMapper.selectById(1L);
        Address address = user.get(User::getProfileAddress);
        assertNotNull(address);
        assertEquals("New York, NY", address.getLocation());
    }

    @Test
    public void shouldHasOneThroughEmpty() {
        User user = userMapper.selectById(11L);
        Address address = user.get(User::getProfileAddress);
        assertNull(address);
    }

    @Test
    public void shouldLoadHasOneThroughForList() {
        // 使用selectBatchIds获取用户列表
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        // 使用RelationUtils.load加载关联数据
        RelationUtils.load(userList, User::getProfileAddress);

        // 直接使用getProfileAddress()获取已加载的数据
        User user1 = userList.get(0);
        Address address1 = user1.getProfileAddress();
        assertNotNull(address1);
        assertEquals("New York, NY", address1.getLocation());

        User user2 = userList.get(1);
        Address address2 = user2.getProfileAddress();
        assertNotNull(address2);
        assertEquals("San Francisco, CA", address2.getLocation());
    }
}