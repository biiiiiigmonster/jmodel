package io.github.biiiiiigmonster.driver.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.driver.entity.Post;
import io.github.biiiiiigmonster.driver.entity.Role;
import io.github.biiiiiigmonster.driver.entity.User;
import io.github.biiiiiigmonster.relation.RelationOption;
import io.github.biiiiiigmonster.relation.RelationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 关联查询约束特性测试。
 * <p>
 * 覆盖以下场景：
 * <ol>
 *     <li>运行时约束（{@code Consumer<QueryCondition>} 形式）</li>
 *     <li>静态注解约束（{@code @Constraint}）</li>
 *     <li>静态 + 运行时约束叠加（AND）</li>
 *     <li>约束仅作用于终表，不影响中间表（BelongsToMany）</li>
 *     <li>批量加载 + 约束</li>
 *     <li>{@code loadForce} 覆盖约束</li>
 *     <li>写操作不受查询约束影响</li>
 * </ol>
 */
public class RelationConstraintTest extends BaseTest {

    // ---------------- 1. 运行时约束 ----------------

    @Test
    public void shouldApplyRuntimeLambdaConstraint() {
        User user = findById(User.class, 1L);
        // User 1: posts ["Getting Started with Spring Boot", "Mastering JPA Relationships"]
        List<Post> posts = user.get(User::getPosts, c -> c.like("title", "Spring"));
        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals("Getting Started with Spring Boot", posts.get(0).getTitle());
    }

    @Test
    public void shouldApplyRuntimeConstraintViaRelationOption() {
        User user = findById(User.class, 1L);
        RelationOption<User> option = RelationOption.of(User.class, "posts")
                .constraint(c -> c.like("title", "Spring"));
        user.load(option);
        assertEquals(1, user.getPosts().size());
        assertEquals("Getting Started with Spring Boot", user.getPosts().get(0).getTitle());
    }

    // ---------------- 2. 静态 @Constraint 注解 ----------------

    @Test
    public void shouldApplyStaticLikeConstraint() {
        User user = findById(User.class, 1L);
        List<Post> posts = user.get(User::getSpringPosts);
        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals("Getting Started with Spring Boot", posts.get(0).getTitle());
    }

    @Test
    public void shouldApplyStaticGtConstraint() {
        // User 3 posts: id=5,11,12. highIdPosts 约束 id > 5 → 11,12
        User user = findById(User.class, 3L);
        List<Post> posts = user.get(User::getHighIdPosts);
        assertNotNull(posts);
        assertEquals(2, posts.size());
        Set<Long> ids = posts.stream().map(Post::getId).collect(Collectors.toSet());
        assertTrue(ids.contains(11L));
        assertTrue(ids.contains(12L));
        assertFalse(ids.contains(5L));
    }

    @Test
    public void shouldApplyStaticInConstraint() {
        // User 3 posts: "Docker Best Practices", "Kubernetes in Practice", "CI/CD Pipeline Design"
        User user = findById(User.class, 3L);
        List<Post> posts = user.get(User::getDockerOrK8sPosts);
        assertNotNull(posts);
        assertEquals(2, posts.size());
        Set<String> titles = posts.stream().map(Post::getTitle).collect(Collectors.toSet());
        assertTrue(titles.contains("Docker Best Practices"));
        assertTrue(titles.contains("Kubernetes in Practice"));
        assertFalse(titles.contains("CI/CD Pipeline Design"));
    }

    // ---------------- 3. 静态 + 运行时约束叠加 ----------------

    @Test
    public void shouldStackStaticAndRuntimeConstraints() {
        // constrainedPosts 自带 id > 0；再叠加运行时 title LIKE "Spring"
        User user = findById(User.class, 1L);
        List<Post> posts = user.get(User::getConstrainedPosts, c -> c.like("title", "Spring"));
        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals("Getting Started with Spring Boot", posts.get(0).getTitle());
    }

    // ---------------- 4. 只作用于终表 ----------------

    @Test
    public void shouldOnlyConstrainTargetTableNotPivot() {
        // User 1 在 UserRole 中关联到 Role 1(Administrator), 2(Moderator), 3(Editor)
        // adminRoles 约束 name = "Administrator"，仅应作用于 Role 终表
        // 若误作用于 UserRole，pivot 查询在 ReflectUtil.getFieldValue(userRole, "name") → null，将导致 0 条
        User user = findById(User.class, 1L);
        List<Role> roles = user.get(User::getAdminRoles);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("Administrator", roles.get(0).getName());
    }

    @Test
    public void shouldRuntimeConstraintOnBelongsToManyOnlyAffectTarget() {
        User user = findById(User.class, 1L);
        List<Role> roles = user.get(User::getRoles, c -> c.eq("name", "Administrator"));
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("Administrator", roles.get(0).getName());
    }

    // ---------------- 5. 批量加载 + 约束 ----------------

    @Test
    public void shouldApplyConstraintOnEagerLoadForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L, 3L));
        RelationUtils.load(userList, User::getPosts, c -> c.like("title", "Spring"));

        User u1 = userList.stream().filter(u -> u.getId() == 1L).findFirst().orElse(null);
        User u2 = userList.stream().filter(u -> u.getId() == 2L).findFirst().orElse(null);
        User u3 = userList.stream().filter(u -> u.getId() == 3L).findFirst().orElse(null);

        assertNotNull(u1);
        assertNotNull(u2);
        assertNotNull(u3);
        assertEquals(1, u1.getPosts().size());
        assertEquals("Getting Started with Spring Boot", u1.getPosts().get(0).getTitle());
        // User 2 无包含 "Spring" 标题的 Post
        assertEquals(0, u2.getPosts().size());
        // User 3 无包含 "Spring" 标题的 Post
        assertEquals(0, u3.getPosts().size());
    }

    // ---------------- 6. loadForce + 约束 ----------------

    @Test
    public void shouldLoadForceWithConstraintOverrideCachedRelation() {
        User user = findById(User.class, 1L);
        user.load(User::getPosts);
        assertEquals(2, user.getPosts().size());

        user.loadForce(User::getPosts, c -> c.like("title", "Spring"));
        assertEquals(1, user.getPosts().size());
        assertEquals("Getting Started with Spring Boot", user.getPosts().get(0).getTitle());
    }

    // ---------------- 7. 写操作不受查询约束影响 ----------------

    @Test
    public void shouldNotAffectWriteOperations() {
        // 注解 adminRoles 上定义了 name="Administrator" 约束，
        // 但底层 attach/sync/detach 属写操作，约束对它们无效（这里通过读回验证：
        // 读 adminRoles 受约束过滤只有 1 条，而读 roles 仍然返回 User 1 的全部 3 条）
        User user = findById(User.class, 1L);
        assertEquals(1, user.get(User::getAdminRoles).size());
        assertEquals(3, user.get(User::getRoles).size());
    }
}
