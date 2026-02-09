package io.github.biiiiiigmonster.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.entity.Profile;
import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HasOneTest extends BaseTest {

    @Test
    public void shouldNotNull() {
        User user = findById(User.class, 1L);
        Profile profile = user.get(User::getProfile);
        assertNotNull(profile);
        assertEquals("Software Engineer at Tech Corp", profile.getDescription());
    }

    @Test
    public void shouldEmpty() {
        User user = findById(User.class, 11L);
        Profile profile = user.get(User::getProfile);
        assertNull(profile);
    }

    @Test
    public void shouldLoadHasOneForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L));
        assertEquals(2, userList.size());

        RelationUtils.load(userList, User::getProfile);

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
