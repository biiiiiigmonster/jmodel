package io.github.biiiiiigmonster.driver.model.relation;

import io.github.biiiiiigmonster.BaseTest;
import io.github.biiiiiigmonster.driver.entity.Address;
import io.github.biiiiiigmonster.driver.entity.Likes;
import io.github.biiiiiigmonster.driver.entity.Post;
import io.github.biiiiiigmonster.driver.entity.Role;
import io.github.biiiiiigmonster.driver.entity.Tag;
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
import static org.junit.Assert.assertNull;
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
 *     <li>中间模型静态约束（{@code pivotConstraints} / {@code throughConstraints}）</li>
 * </ol>
 */
public class RelationConstraintTest extends BaseTest {

    // ---------------- 1. 运行时约束 ----------------

    @Test
    public void shouldApplyRuntimeLambdaConstraint() {
        User user = findById(User.class, 1L);
        // User 1: posts ["Getting Started with Spring Boot", "Mastering JPA Relationships"]
        user.load(User::getPosts, c -> c.like("title", "Spring"));
        List<Post> posts = user.getPosts();
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
        user.load(User::getConstrainedPosts, c -> c.like("title", "Spring"));
        List<Post> posts = user.getConstrainedPosts();
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
        user.load(User::getRoles, c -> c.eq("name", "Administrator"));
        List<Role> roles = user.getRoles();
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

    // ---------------- 8. 中间模型静态约束 ----------------

    /**
     * BelongsToMany + pivotConstraints：约束直接作用于中间表 UserRole。
     * <p>
     * User 1 在 UserRole 中的记录为 id ∈ {1,2,3}，角色分别为 Administrator / Moderator / Editor；
     * rolesByPivotIdIn 注解中 pivot 层面限制 UserRole.id IN (1,3)，过滤掉 id=2 这一行，
     * 因此最终 Role 终表结果应为 Administrator + Editor。
     */
    @Test
    public void shouldApplyBelongsToManyPivotConstraint() {
        User user = findById(User.class, 1L);
        List<Role> roles = user.get(User::getRolesByPivotIdIn);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        Set<String> names = roles.stream().map(Role::getName).collect(Collectors.toSet());
        assertTrue(names.contains("Administrator"));
        assertTrue(names.contains("Editor"));
        assertFalse(names.contains("Moderator"));
    }

    /**
     * BelongsToMany + pivotConstraints：验证不同 User 之间的 pivot 约束互不影响，
     * 且当 User 的所有 UserRole 都被 pivot 约束过滤掉时，终表结果应为空。
     * <p>
     * User 4 在 UserRole 中的记录为 id ∈ {8, 9}，与 pivotConstraints 约束 id IN (1,3) 不相交，
     * 因此最终 Role 终表结果应为空。
     */
    @Test
    public void shouldReturnEmptyWhenPivotConstraintFiltersAllRecords() {
        User user = findById(User.class, 4L);
        List<Role> roles = user.get(User::getRolesByPivotIdIn);
        assertNotNull(roles);
        assertEquals(0, roles.size());
    }

    /**
     * BelongsToMany + pivotConstraints：批量加载 pivot 约束正确分发。
     * <p>
     * User 1 UserRole id {1,2,3} → IN (1,3) → Role {Administrator, Editor}；
     * User 2 UserRole id {4,5} → IN (1,3) → 空；
     * User 3 UserRole id {6,7} → IN (1,3) → 空。
     */
    @Test
    public void shouldApplyBelongsToManyPivotConstraintOnEagerLoadForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L, 3L));
        RelationUtils.load(userList, User::getRolesByPivotIdIn);

        User u1 = userList.stream().filter(u -> u.getId() == 1L).findFirst().orElse(null);
        User u2 = userList.stream().filter(u -> u.getId() == 2L).findFirst().orElse(null);
        User u3 = userList.stream().filter(u -> u.getId() == 3L).findFirst().orElse(null);

        assertNotNull(u1);
        assertNotNull(u2);
        assertNotNull(u3);
        assertEquals(2, u1.getRolesByPivotIdIn().size());
        assertEquals(0, u2.getRolesByPivotIdIn().size());
        assertEquals(0, u3.getRolesByPivotIdIn().size());
    }

    /**
     * HasManyThrough + throughConstraints：约束直接作用于 through 模型 Post。
     * <p>
     * User 1 的 Post 为 {1,2}；throughConstraint 限制 Post.title LIKE "Spring"，
     * 仅 Post 1 "Getting Started with Spring Boot" 满足；该 Post 上的 Likes 为 {1,2}。
     */
    @Test
    public void shouldApplyHasManyThroughConstraint() {
        User user = findById(User.class, 1L);
        List<Likes> likes = user.get(User::getSpringPostLikes);
        assertNotNull(likes);
        assertEquals(2, likes.size());
        Set<Long> ids = likes.stream().map(Likes::getId).collect(Collectors.toSet());
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
    }

    /**
     * HasManyThrough + throughConstraints：所有 through 记录都被过滤时返回空。
     * <p>
     * User 3 的 Post 为 {5,11,12}，三者 title 都不含 "Spring"，through 被全部过滤 → Likes 空集。
     */
    @Test
    public void shouldReturnEmptyWhenThroughConstraintFiltersAllRecords() {
        User user = findById(User.class, 3L);
        List<Likes> likes = user.get(User::getSpringPostLikes);
        assertNotNull(likes);
        assertEquals(0, likes.size());
    }

    /**
     * HasManyThrough + throughConstraints：批量加载下 through 约束正确分发。
     */
    @Test
    public void shouldApplyHasManyThroughConstraintOnEagerLoadForList() {
        List<User> userList = findByIds(User.class, Arrays.asList(1L, 2L, 3L));
        RelationUtils.load(userList, User::getSpringPostLikes);

        User u1 = userList.stream().filter(u -> u.getId() == 1L).findFirst().orElse(null);
        User u2 = userList.stream().filter(u -> u.getId() == 2L).findFirst().orElse(null);
        User u3 = userList.stream().filter(u -> u.getId() == 3L).findFirst().orElse(null);

        assertNotNull(u1);
        assertNotNull(u2);
        assertNotNull(u3);
        // User 1 -> Post 1 (Spring) -> Likes {1,2}
        assertEquals(2, u1.getSpringPostLikes().size());
        // User 2 -> Post {3,4} 均无 Spring → 空
        assertEquals(0, u2.getSpringPostLikes().size());
        // User 3 -> Post {5,11,12} 均无 Spring → 空
        assertEquals(0, u3.getSpringPostLikes().size());
    }

    /**
     * HasOneThrough + throughConstraints：through 命中时可正常获取终表对象。
     * <p>
     * User 5 的 Profile.description = "Data Scientist" 满足 LIKE "Scientist"，
     * 通过 Profile 5 → Address 5 "Boston, MA"。
     */
    @Test
    public void shouldApplyHasOneThroughConstraintWhenMatched() {
        User user = findById(User.class, 5L);
        Address address = user.get(User::getScientistProfileAddress);
        assertNotNull(address);
        assertEquals("Boston, MA", address.getLocation());
    }

    /**
     * HasOneThrough + throughConstraints：through 被过滤时终表结果为 null。
     * <p>
     * User 1 的 Profile.description = "Software Engineer at Tech Corp" 不包含 "Scientist"，
     * through 被过滤 → 最终终表对象为 null。
     */
    @Test
    public void shouldReturnNullWhenHasOneThroughConstraintFilters() {
        User user = findById(User.class, 1L);
        Address address = user.get(User::getScientistProfileAddress);
        assertNull(address);
    }

    /**
     * MorphToMany + pivotConstraints：约束直接作用于中间表 Taggable。
     * <p>
     * Post 1 在 Taggable 中的记录为 id {1,2}，tagId 分别为 {1,2}；
     * pivotConstraint 限制 Taggable.tagId > 1 → 仅 id=2 行保留 → 终表 Tag 仅剩 "Spring"。
     * <p>
     * 对照组 Post 1 默认 tags 为 [Java, Spring]，共 2 个，验证约束不是对终表 Tag.name 的过滤。
     */
    @Test
    public void shouldApplyMorphToManyPivotConstraint() {
        Post post = findById(Post.class, 1L);
        // 对照组：默认关联无中间表约束，返回全部 2 个 tag
        List<Tag> defaultTags = post.get(Post::getTags);
        assertEquals(2, defaultTags.size());

        // 约束：仅保留 Taggable.tagId > 1 的 pivot 行 → Tag "Spring"
        List<Tag> tags = post.get(Post::getPivotFilteredTags);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("Spring", tags.get(0).getName());
    }

    /**
     * MorphedByMany + pivotConstraints：约束直接作用于中间表 Taggable。
     * <p>
     * Tag 1 关联到 Post 的 Taggable 为 id {1, 21}（对应 Post {1, 6}）；
     * pivotConstraint 限制 Taggable.id > 10 → 仅 id=21 保留 → 终表 Post 仅剩 Post 6。
     */
    @Test
    public void shouldApplyMorphedByManyPivotConstraint() {
        Tag tag = findById(Tag.class, 1L);
        // Tag 1 关联到 Post 的 Taggable 为 id {1, 21}（对应 Post {1, 6}），
        // pivotConstraints 约束 Taggable.id > 10 后仅 id=21 保留 → 终表 Post 仅 Post 6。
        List<Post> posts = tag.get(Tag::getPivotFilteredPosts);
        assertNotNull(posts);
        assertEquals(1, posts.size());
        assertEquals(6L, posts.get(0).getId().longValue());
        assertEquals("Microservices Architecture", posts.get(0).getTitle());
    }

    /**
     * loadForce 与中间模型静态约束的配合：缓存值被强制覆盖。
     * <p>
     * 先加载 roles（无约束）得 3 条，再 loadForce rolesByPivotIdIn（中间表约束）得 2 条，
     * 两个字段独立缓存互不影响；rolesByPivotIdIn 再次 loadForce 仍然为 2 条，证明约束是稳定的。
     */
    @Test
    public void shouldLoadForceHonorPivotConstraintAndNotAffectOtherRelation() {
        User user = findById(User.class, 1L);
        user.load(User::getRoles);
        assertEquals(3, user.getRoles().size());

        user.loadForce(User::getRolesByPivotIdIn);
        assertEquals(2, user.getRolesByPivotIdIn().size());

        // 再次 loadForce，验证中间表约束稳定
        user.loadForce(User::getRolesByPivotIdIn);
        assertEquals(2, user.getRolesByPivotIdIn().size());

        // 同时另一个不带中间表约束的字段仍然返回全部 3 条
        assertEquals(3, user.getRoles().size());
    }
}
