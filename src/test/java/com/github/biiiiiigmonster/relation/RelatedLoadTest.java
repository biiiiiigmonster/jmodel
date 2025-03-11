package com.github.biiiiiigmonster.relation;

import com.github.biiiiiigmonster.BaseTest;
import com.github.biiiiiigmonster.entity.Phone;
import com.github.biiiiiigmonster.entity.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RelatedLoadTest extends BaseTest {

    @Test
    public void shouldNotNull() {
        User user = userMapper.selectById(1L);
        Phone phone = user.get(User::getPhone);
        assertNotNull(phone);
        assertEquals("10001", phone.getNumber());
    }

    @Test
    public void shouldLoadHasOneForList() {
        List<User> userList = userMapper.selectBatchIds(Arrays.asList(1L, 2L, 11L));
        assertEquals(3, userList.size());

        RelationUtils.load(userList, User::getPhone);

        User user1 = userList.get(0);
        Phone phone1 = user1.getPhone();
        assertNotNull(phone1);
        assertEquals("10001", phone1.getNumber());

        User user2 = userList.get(1);
        Phone phone2 = user2.getPhone();
        assertNotNull(phone2);
        assertEquals("10002", phone2.getNumber());

        User user3 = userList.get(2);
        Phone phone3 = user3.getPhone();
        assertNull(phone3);
    }
}
