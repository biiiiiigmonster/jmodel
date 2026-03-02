# jmodel

**jmodel** 是一个为 Java 设计的 ORM 框架，灵感来源于 Laravel Eloquent。它提供了优雅的 DSL 查询语法和强大的模型关联管理功能，让你能够以自然、流畅的方式处理数据库操作。

## 目录

- [简介](#简介)
- [安装](#安装)
- [快速开始](#快速开始)
  - [定义模型](#定义模型)
  - [基本查询](#基本查询)
  - [模型关联](#模型关联)
- [核心特性](#核心特性)
  - [零配置](#零配置)
  - [Dirty Tracking](#dirty-tracking)
  - [路由绑定](#路由绑定)
- [关联](#关联)
  - [一对一](#一对一)
  - [一对多](#一对多)
  - [一对一（反向）](#一对一反向)
  - [远程一对一](#远程一对一)
  - [远程一对多](#远程一对多)
  - [多对多](#多对多)
  - [一对一（多态）](#一对一多态)
  - [一对多（多态）](#一对多多态)
  - [多态反向关联](#多态反向关联)
- [关联加载](#关联加载)
  - [即时加载](#即时加载)
  - [预加载](#预加载)
  - [嵌套预加载](#嵌套预加载)
- [插入和更新关联模型](#插入和更新关联模型)
  - [建立关联模型](#建立关联模型)
  - [创建关联模型](#创建关联模型)
  - [更新关联模型](#更新关联模型)
  - [多对多关联操作](#多对多关联操作)
- [模型事件](#模型事件)
  - [事件类型](#事件类型)
  - [监听模型事件](#监听模型事件)
  - [事件中的变更追踪](#事件中的变更追踪)
  - [通用心脏事件](#通用心脏事件)
- [路由绑定](#路由绑定)
- [贡献](#贡献)
- [License](#license)

## 简介

数据库表通常相互关联。例如，一篇帖子可能有许多评论，或者一个订单可能与下单的用户相关联。jmodel 使这些关联的管理和处理变得简单，并支持多种不同类型的关联。

与其他 ORM 框架仅专注于单实体模型的增删改查不同，jmodel 更加注重**业务中模型之间的关联处理**。你可以在模型之间建立直观的联系，然后用自然、语义化的方式操作这些关系——就像在写业务逻辑的母语一样。

jmodel 的所有功能特性都参照 **Laravel Eloquent** 实现，包括：

- 🎯 优雅的模型关联（HasOne、HasMany、BelongsTo、BelongsToMany、多态关联等）
- 🔄 自动 Dirty Tracking（自动追踪变更，只更新修改过的字段）
- 🚀 零配置启动（默认配置开箱即用）
- 🔗 Spring MVC 路由绑定（支持 Model 参数直接注入控制器）
- 📦 模块化设计（核心模块 + 驱动扩展，支持自定义驱动）

## 安装

jmodel 已通过 Maven Central 发布，使用 Maven 的项目只需在 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.biiiiiigmonster</groupId>
    <artifactId>jmodel-all</artifactId>
    <version>1.0.0</version>
</dependency>
```

`jmodel-all` 是一个聚合依赖，会自动引入以下模块：

| 模块 | 说明 |
|------|------|
| jmodel-core | 核心 ORM 功能，模型基类、关联注解、事件系统等 |
| jmodel-processor | 注解处理器，编译时生成必要的元数据 |
| jmodel-driver-mybatis-plus | MyBatis-Plus 驱动实现（一等支持的数据库驱动） |
| jmodel-router | Spring MVC 路由绑定支持 |

> **注意**：当前版本仅支持 Maven。Gradle 项目请使用 Maven 兼容方式引用。

## 快速开始

### 定义模型

```java
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.annotation.TableName;
import io.github.biiiiiigmonster.annotation.PrimaryKey;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;

@TableName("users")
public class User extends Model<User> {

    @PrimaryKey
    private Long id;

    private String name;

    private String email;

    @HasMany
    private List<Post> posts;

    // Getter/Setter（或使用 Lombok）
}

@TableName("posts")
public class Post extends Model<Post> {

    @PrimaryKey
    private Long id;

    private Long userId;

    private String title;

    private String content;

    @BelongsTo
    private User author;
}
```

### 基本查询

```java
// 根据 ID 查询
User user = userMapper.selectById(1L);

// 查询所有用户
List<User> users = userMapper.selectList(null);

// 条件查询
List<User> activeUsers = userMapper.selectList(
    Wrappers.<User>lambdaQuery().eq(User::getStatus, "active")
);

// 保存模型（新增或更新）
user.setName("New Name");
user.save();  // 自动检测变更，只更新修改过的字段

// 删除模型
user.delete();
```

### 模型关联

```java
// 访问关联（即时加载）
User user = userMapper.selectById(1L);
List<Post> posts = user.get(User::getPosts);

// 预加载关联（避免 N+1 查询）
List<User> users = userMapper.selectBatchIds(Arrays.asList(1L, 2L, 3L));
RelationUtils.load(users, User::getPosts);

// 嵌套预加载
user.load("posts.comments.author");
```

## 核心特性

### 零配置

jmodel 采用**约定优于配置**的原则。在大多数情况下，你不需要任何额外配置：

- 表名自动从类名推断（`User` → `users`）
- 主键默认为 `id`
- 外键自动从关联关系推断（`User` 的 `HasMany<Post>` → `userId`）

如需自定义，可通过注解覆盖：

```java
@TableName("custom_table")
@PrimaryKey("custom_id")
@HasMany(foreignKey = "custom_user_id", localKey = "custom_id")
```

### Dirty Tracking

jmodel 内置了**自动变更追踪**功能（用户无感知）：

```java
User user = userMapper.selectById(1L);
user.setName("New Name");
user.save();  // 只更新 name 字段，而非整行
```

底层通过字节码增强自动记录 setter 调用，对比原始值，确保只更新真正变更的字段。这带来：

- 更好的性能（减少不必要的数据库写入）
- 更精确的并发控制
- 更清晰的审计日志

### 路由绑定

jmodel-router 模块支持将 Model 直接注入 Spring MVC 控制器参数：

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{user}")
    public User getUser(@PathModel User user) {
        // 自动根据路径参数加载用户模型
        return user;
    }
}
```

详细用法见 [路由绑定](#路由绑定) 章节。

## 关联

### 一对一

一对一是最基本的关联类型。例如，一个 `User` 模型可能与一个 `Phone` 模型相关联。要定义这种关联，我们在 `User` 类中添加一个 `phone` 字段：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class User extends Model<User> {
    /**
     * 获取与用户关联的电话
     */
    @HasOne
    private Phone phone;
}
```

jmodel 会假定 `Phone` 模型有一个 `userId` 外键属性。如果您希望覆盖这个约定，可以传递自定义外键名称：

```java
@HasOne(foreignKey = "userId", localKey = "id")
private Phone phone;
```

一旦定义了关联，就可以使用 jmodel 的关联方法访问相关记录：

```java
User user = userMapper.selectById(1L);
Phone phone = user.get(User::getPhone);
```

您也可以使用 `load` 方法预加载关联数据：

```java
User user = userMapper.selectById(1L);
user.load(User::getPhone);
Phone phone = user.getPhone();
```

### 一对多

一对多关联用于定义单个模型拥有任意数量的其他模型的情况。例如，一个用户可能有无限数量的帖子。一旦定义了关联，就可以使用 `posts` 属性访问帖子集合：

```java
@HasMany
private List<Post> posts;
```

jmodel 会自动确定 `Post` 模型上的正确外键。按照约定，将使用父模型的"驼峰命名"加上 `Id` 后缀作为外键。因此，在这个例子中，jmodel 会假定 `Post` 模型上的外键是 `userId`。

如果您希望覆盖这个约定，可以在定义关联时传递自定义外键：

```java
@HasMany(foreignKey = "userId", localKey = "id")
private List<Post> posts;
```

一旦定义了关联，就可以访问帖子集合：

```java
User user = userMapper.selectById(1L);
List<Post> posts = user.get(User::getPosts);
```

您也可以使用 `RelationUtils.load` 方法预加载关联数据：

```java
List<User> users = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
RelationUtils.load(users, User::getPosts);

// 现在可以直接访问已加载的关联
for(User user : users) {
    List<Post> posts = user.getPosts();
}
```

#### 自动为子级添加父级模型（Chaperone）

当您使用 `HasMany` 或 `HasOne` 关联时，有时需要在子模型中访问父模型。jmodel 通过 `chaperone` 参数提供了这种能力：

```java
@HasMany(chaperone = true)
private List<Post> posts;

// ...

@TableName
class Post extends Model<Post> {
    /**
     * 获取这个帖子所属的用户
     */
    @BelongsTo
    private User user;
}
```

这样，每个 `Post` 对象都会有一个指向其父 `User` 的引用，避免创建额外的查询：

```java
User user = userMapper.selectById(1L);
user.load(User::getPosts);

List<Post> posts = user.getPosts();
// posts 中的 Post 对象有 User 引用
assertEquals(posts.get(0).getUser(), user);
```

### 一对一（反向）

我们已经探讨了如何访问子模型的记录，现在让我们定义一个从子模型访问父模型的关联。要定义这种关联，请在子模型上使用 `@BelongsTo` 注解：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class Phone extends Model<Phone> {
    /**
     * 获取这个电话所属的用户
     */
    @BelongsTo
    private User user;
}
```

在上面的例子中，jmodel 将尝试匹配 `Phone` 模型上的 `userId` 与 `User` 模型上的 `id`。jmodel 通过检查关联方法的名称并使用 `Id` 后缀来确定外键的默认名称。但是，如果 `Phone` 模型上的外键不是 `userId`，您可以传递自定义键名：

```java
@BelongsTo(foreignKey = "userId", ownerKey = "id")
private User user;
```

一旦定义了关联，就可以访问父模型：

```java
Phone phone = phoneMapper.selectById(1L);
User user = phone.get(Phone::getUser);
```

### 远程一对一

远程一对一关联类似于远程一对多关联；但是，最终关联的结果是单个模型实例而不是集合。例如，一个 `Mechanic` 模型可能通过中间的 `Car` 模型与一个 `Owner` 模型相关联：

```java

import io.github.biiiiiigmonster.Model;

@TableName
class Mechanic extends Model<Mechanic> {
    /**
     * 获取汽车的主人
     */
    @HasOneThrough(through = Car.class)
    private Owner carOwner;
}
```

### 远程一对多

"远程"一对多关联通过中间关联提供了方便的快捷方式。例如，如果一个 `Country` 模型通过中间的 `User` 模型拥有多个 `Post` 模型，我们可以直接访问一个国家的所有帖子：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class Country extends Model<Country> {
    /**
     * 获取国家下所有的帖子
     */
    @HasManyThrough(through = User.class)
    private List<Post> posts;
}
```

### 多对多

多对多关联比 `HasOne` 和 `HasMany` 关联更复杂。例如，一个用户可能有多个角色，而一个角色可能被多个用户共享。例如，许多用户可能具有"管理员"角色。要定义这种关联，需要三个数据库表：`users`、`roles` 和 `role_user`。`role_user` 表是根据相关模型的名称按字母顺序命名的，包含 `userId` 和 `roleId` 列。

多对多关联使用 `@BelongsToMany` 注解定义：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class User extends Model<User> {
    /**
     * 属于用户的角色
     */
    @BelongsToMany(using = UserRole.class)
    private List<Role> roles;
}
```

一旦定义了关联，就可以使用 `roles` 属性访问用户的角色：

```java
User user = userMapper.selectById(1L);
List<Role> roles = user.get(User::getRoles);
```

#### 自定义中间表列名

如果您需要自定义连接表上的列名，可以使用 `foreignPivotKey` 和 `relatedPivotKey` 参数：

```java
@BelongsToMany(
    using = UserRole.class,
    foreignPivotKey = "userId",
    relatedPivotKey = "roleId"
)
private List<Role> roles;
```

#### 检索中间表列

使用多对多关联时，您可能需要访问中间表的属性。要做到这一点，请使用 `withPivot` 参数：

```java
@BelongsToMany(
    using = UserRole.class,
    withPivot = true
)
private List<Role> roles;
```

这样，每个 `Role` 模型都会有一个 `pivot` 属性，包含中间表的信息：

```java
User user = userMapper.selectById(1L);
List<Role> roles = user.get(User::getRoles);
// 访问中间表数据
for (Role role : roles) {
    UserRole pivot = role.getPivot();
}
```

### 一对一（多态）

多态一对一关联类似于典型的一对一关联；但是，目标模型可以属于多种类型的模型。例如，`Post` 模型和 `User` 模型可能共享与 `Image` 模型的关联：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class User extends Model<User> {
    /**
     * 获取用户的图片
     */
    @MorphOne
    private Image image;
}

@TableName
class Post extends Model<Post> {
    /**
     * 获取帖子的图片
     */
    @MorphOne
    private Image image;
}

@TableName
class Image extends Model<Image> {
    /**
     * 获取图片所属的用户
     */
    @MorphTo
    private User user;

    /**
     * 获取图片所属的帖子
     */
    @MorphTo
    private Post post;
}
```

### 一对多（多态）

多态关联允许目标模型属于多种类型的模型。例如，假设您的应用中有 `Post` 和 `Video` 模型，并且每个都可以有多个 `Comment` 模型。使用多态关联，您可以使用单个 `comments` 关联访问两种模型的所有评论：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class Post extends Model<Post> {
    /**
     * 获取所有帖子的评论
     */
    @MorphMany
    private List<Comment> comments;
}

@TableName
class Video extends Model<Video> {
    /**
     * 获取所有视频的评论
     */
    @MorphMany
    private List<Comment> comments;
}

@TableName
class Comment extends Model<Comment> {
    /**
     * 获取评论所属的帖子
     */
    @MorphTo
    private Post post;

    /**
     * 获取评论所属的视频
     */
    @MorphTo
    private Video video;
}
```

您可以自定义多态关联的类型和 ID 字段：

```java
@MorphMany(type = "commentableType", id = "commentableId")
private List<Comment> comments;
```

#### 自定义多态类型

默认情况下，jmodel 将使用完全限定的类名作为多态关联的"类型"值。例如，给定上面的 `Post` 和 `Video` 模型的例子，默认情况下存储在 `commentableType` 列中的值将是 `com.example.Post` 或 `com.example.Video`。

如果您希望使用自定义值，可以使用 `@MorphAlias` 注解：

```java
@MorphAlias("post")
public class Post extends Model<Post> {
    // ...
}
```
> `@MorphAlias` 支持默认值，为当前类的 simpleName

### 多态反向关联

#### 多对多（多态）

多态多对多关联表示如标签系统等复杂关联。例如，您的应用可能允许用户标记帖子和视频。使用多态多对多关联，您可以使用单个 `tags` 关联访问这两种模型的所有标签：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class Post extends Model<Post> {
    /**
     * 获取该帖子的所有标签
     */
    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}

@TableName
class Video extends Model<Video> {
    /**
     * 获取该视频的所有标签
     */
    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}
```

#### 反向多对多（多态）

要定义多态多对多关联的反向，请在相关模型上使用 `@MorphedByMany` 注解：

```java
import io.github.biiiiiigmonster.Model;

@TableName
class Tag extends Model<Tag> {
    /**
     * 获取分配了该标签的所有帖子
     */
    @MorphedByMany(using = Taggable.class)
    private List<Post> posts;

    /**
     * 获取分配了该标签的所有视频
     */
    @MorphedByMany(using = Taggable.class)
    private List<Video> videos;
}
```

## 关联加载

jmodel 提供了几种不同的方法来加载关联数据：

### 即时加载

您可以使用 `get` 方法即时加载关联数据：

```java
User user = userMapper.selectById(1L);
List<Post> posts = user.get(User::getPosts);
```

### 预加载

您也可以使用 `load` 方法预加载关联：

```java
User user = userMapper.selectById(1L);
user.load(User::getPosts);
List<Post> posts = user.getPosts();
```

对于集合，为了避免 `N+1` 查询问题，您可以使用 `RelationUtils.load` 方法：

```java
List<User> users = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
RelationUtils.load(users, User::getPosts);
```

### 嵌套预加载

您还可以预加载嵌套关联：

```java
User user = userMapper.selectById(1L);
user.load("posts.comments");
```

这将加载用户的所有帖子及其评论数据。

## 插入和更新关联模型

jmodel 提供了强大的关联模型操作功能，支持建立、创建、更新和多对多关联操作。

### 建立关联模型

您可以使用 `associate` 方法建立并保存关联模型。这适用于一对一和一对多关联：

```java
// 建立一对一关联
User user = userMapper.selectById(1L);
Phone phone = new Phone();
phone.setNumber("1234567890");
user.associate(User::getPhone, phone);

// 建立一对多关联
User user = userMapper.selectById(1L);
List<Post> posts = Arrays.asList(
    new Post() {{ setTitle("First Post"); }},
    new Post() {{ setTitle("Second Post"); }}
);
user.associate(User::getPosts, posts);

// 使用字符串方式
user.associate("phone", phone);
user.associate("posts", posts);
```

### 多对多关联操作

对于多对多关联，jmodel 提供了 `attach`、`detach` 和 `sync` 方法：

#### 附加关联

```java
// 附加角色到用户
User user = userMapper.selectById(1L);
Role adminRole = roleMapper.selectById(1L);
Role userRole = roleMapper.selectById(2L);

user.attach(User::getRoles, adminRole, userRole);

// 使用字符串方式
user.attach("roles", adminRole, userRole);
```

#### 分离关联

```java
// 分离指定角色
user.detach(User::getRoles, adminRole);

// 分离所有角色
user.detach(User::getRoles);
```

#### 同步关联

```java
// 同步角色（先删除所有现有关联，再添加新关联）
user.sync(User::getRoles, userRole, guestRole);
```

#### 同步关联（不移除现有关联）

```java
// 同步角色（只添加新关联，不移除现有关联）
user.syncWithoutDetaching(User::getRoles, userRole, guestRole);
```

#### 切换关联

```java
// 切换角色（如果已存在则移除，如果不存在则添加）
user.toggle(User::getRoles, adminRole);

// 切换多个角色
user.toggle(User::getRoles, userRole, guestRole);

// 使用字符串方式
user.toggle("roles", adminRole);

// 切换列表中的角色
List<Role> roles = Arrays.asList(adminRole, userRole, guestRole);
user.toggle(User::getRoles, roles);
```

这些方法会自动处理中间表的创建和删除操作，确保数据一致性。

## 模型事件

jmodel 提供了完善的事件系统，让你能够在模型生命周期中执行自定义逻辑。这与 Laravel Eloquent 的事件系统类似，通过 Spring 的事件机制实现。

### 事件类型

jmodel 为模型的各种操作提供了以下事件：

| 事件 | 触发时机 | 说明 |
|------|----------|------|
| `ModelCreating` | 创建前 | 模型即将被插入数据库 |
| `ModelCreated` | 创建后 | 模型已成功插入数据库 |
| `ModelUpdating` | 更新前 | 模型即将被更新 |
| `ModelUpdated` | 更新后 | 模型已成功更新 |
| `ModelSaving` | 保存前 | 模型即将被保存（创建或更新） |
| `ModelSaved` | 保存后 | 模型已成功保存（创建或更新） |
| `ModelDeleting` | 删除前 | 模型即将被删除 |
| `ModelDeleted` | 删除后 | 模型已成功删除 |

### 监听模型事件

要监听模型事件，你需要创建一个 Spring 事件监听器。这与 Laravel 的 Observer 类似，但采用 Spring 的事件机制：

```java
import io.github.biiiiiigmonster.event.ModelCreatedEvent;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    /**
     * 监听用户创建事件
     */
    @EventListener
    public void handleModelCreated(ModelCreatedEvent<User> event) {
        User user = event.getModel();
        // 发送欢迎邮件
        sendWelcomeEmail(user.getEmail());
    }

    /**
     * 监听用户保存事件
     */
    @EventListener
    public void handleModelSaved(ModelSavedEvent<User> event) {
        User user = event.getModel();
        // 记录审计日志
        logAudit("User saved: " + user.getId());
    }

    /**
     * 监听用户删除事件
     */
    @EventListener
    public void handleModelDeleted(ModelDeletedEvent<User> event) {
        User user = event.getModel();
        // 清理相关缓存
        clearUserCache(user.getId());
    }

    private void sendWelcomeEmail(String email) {
        // 发送邮件逻辑
    }

    private void logAudit(String message) {
        // 记录审计日志
    }

    private void clearUserCache(Long userId) {
        // 清理缓存逻辑
    }
}
```

### 事件触发顺序

当你调用 `save()` 方法时，事件按照以下顺序触发：

**对于新增操作：**
1. `ModelSaving`
2. `ModelCreating`
3. 执行数据库 INSERT
4. `ModelCreated`
5. `ModelSaved`

**对于更新操作：**
1. `ModelSaving`
2. `ModelUpdating`
3. 执行数据库 UPDATE
4. `ModelUpdated`
5. `ModelSaved`

当你调用 `delete()` 方法时：
1. `ModelDeleting`
2. 执行数据库 DELETE
3. `ModelDeleted`

### 事件中的变更追踪

jmodel 的事件系统支持变更追踪。你可以在 `ModelUpdating` 和 `ModelUpdating` 事件中访问脏数据和原始值：

```java
import io.github.biiiiiigmonster.event.ModelUpdatingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    @EventListener
    public void handleModelUpdating(ModelUpdatingEvent<User> event) {
        User user = event.getModel();

        // 检查是否有变更
        if (user.isDirty()) {
            // 获取所有变更字段
            Map<String, Object> dirtyFields = user.getDirty();

            // 获取特定字段的变更
            if (user.isDirty("email")) {
                String newEmail = user.getEmail();
                String oldEmail = (String) user.getOriginal("email");
                // 记录邮箱变更
                logEmailChange(oldEmail, newEmail);
            }
        }
    }

    @EventListener
    public void handleModelSaved(ModelSavedEvent<User> event) {
        User user = event.getModel();

        // 获取本次保存的变更
        if (user.wasChanged()) {
            Map<String, Object> changes = user.getChanges();
            // 记录审计日志
            logChanges(changes);
        }
    }

    private void logEmailChange(String oldEmail, String newEmail) {
        // 记录邮箱变更
    }

    private void logChanges(Map<String, Object> changes) {
        // 记录变更日志
    }
}
```

### 敏感字段变更检测

你可以利用事件系统检测敏感字段的变更：

```java
@Component
public class SecurityEventListener {

    @EventListener
    public void handleModelUpdating(ModelUpdatingEvent<User> event) {
        User user = event.getModel();

        // 检测敏感字段变更
        if (user.isDirty("password", "email", "role")) {
            // 发送安全警报
            sendSecurityAlert(user);
        }
    }

    private void sendSecurityAlert(User user) {
        // 发送安全警报
    }
}
```

### 通用心脏事件

`ModelSaving` 和 `ModelSaved` 是通用的"心脏事件"，无论创建还是更新都会触发。这让你可以在一个监听器中处理所有保存相关的逻辑：

```java
@Component
public class CacheEventListener {

    @EventListener
    public void handleModelSaved(ModelSavedEvent<?> event) {
        Object model = event.getModel();

        // 清除相关缓存
        if (model instanceof User) {
            clearUserCache(((User) model).getId());
        } else if (model instanceof Post) {
            clearPostCache(((Post) model).getId());
        }
    }
}
```

### 事件最佳实践

1. **保持监听器轻量**：事件监听器应该快速执行，避免耗时操作。对于耗时任务，建议使用异步事件或消息队列。

2. **异步处理**：对于发送邮件、记录日志等操作，可以使用 Spring 的 `@Async`：

```java
@Async
@EventListener
public void handleModelCreated(ModelCreatedEvent<User> event) {
    sendWelcomeEmail(event.getModel().getEmail());
}
```

3. **事务监听**：如果需要在事务提交后再执行（如发送通知），可以使用 `@TransactionalEventListener`：

```java
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleModelCreated(ModelCreatedEvent<User> event) {
    // 事务提交后执行
    sendNotification(event.getModel());
}
```

## 路由绑定

jmodel-router 模块提供了 Spring MVC 参数解析器支持，允许在控制器方法中直接使用 `@PathModel` 注解注入模型实例。

### 基本用法

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /users/1
     * 自动加载 ID 为 1 的 User 模型
     */
    @GetMapping("/{user}")
    public User show(@PathModel User user) {
        return user;
    }

    /**
     * PUT /users/1
     * 自动加载并绑定请求参数
     */
    @PutMapping("/{user}")
    public User update(@PathModel User user, @RequestBody User requestBody) {
        // 将请求参数绑定到已加载的模型
        user.setName(requestBody.getName());
        user.setEmail(requestBody.getEmail());
        user.save();
        return user;
    }
}
```

### 工作原理

`@PathModel` 注解的参数会触发以下流程：

1. 从请求路径中提取主键值（如 `/users/1` 中的 `1`）
2. 使用对应的 Mapper 加载模型
3. 将模型注入到控制器方法参数中

如果模型不存在，将返回 404 响应。

### 自定义路由键

默认情况下，使用路径参数中与参数同名的变量作为主键。如需自定义：

```java
@GetMapping("/{userId}")
public User show(@PathModel("userId") User user) {
    return user;
}
```

### 启用路由绑定

在 Spring Boot 应用中，只需将 jmodel-router 添加到依赖中，并启用组件扫描：

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example", "io.github.biiiiiigmonster.router"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

或者手动配置 `PathModelArgumentResolver`：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new PathModelArgumentResolver());
    }
}
```

## License

本项目采用 Apache 2.0 许可证 - 详见 [LICENSE](LICENSE) 文件
