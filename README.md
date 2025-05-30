# jmodel

jmodel是一个为Java设计的ORM框架，提供了优雅的DSL查询语法和强大的关联管理功能。

## 目录

- [简介](#简介)
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

## 简介

数据库表通常相互关联。例如，一篇帖子可能有许多评论，或者一个订单可能与下单的用户相关联。jmodel使这些关联的管理和处理变得简单，并支持多种不同类型的关联：

## 关联

### 一对一

一对一是最基本的关联类型。例如，一个`User`模型可能与一个`Phone`模型相关联。要定义这种关联，我们在`User`类中添加一个`phone`字段：

```java
import com.github.biiiiiigmonster.Model;

@TableName
class User extends Model<User> {
    /**
     * 获取与用户关联的电话
     */
    @HasOne
    private Phone phone;
}
```

jmodel会假定`Phone`模型有一个`user_id`外键。如果您希望覆盖这个约定，可以传递自定义外键名称：

```java
@HasOne(foreignKey = "user_id", localKey = "id")
private Phone phone;
```

一旦定义了关联，就可以使用jmodel的关联方法访问相关记录：

```java
User user = userMapper.selectById(1L);
Phone phone = user.get(User::getPhone);
```

您也可以使用`load`方法预加载关联数据：

```java
User user = userMapper.selectById(1L);
user.load(User::getPhone);
Phone phone = user.getPhone();
```

### 一对多

一对多关联用于定义单个模型拥有任意数量的其他模型的情况。例如，一个用户可能有无限数量的帖子。一旦定义了关联，就可以使用`posts`属性访问帖子集合：

```java
@HasMany
private List<Post> posts;
```

jmodel会自动确定`Post`模型上的正确外键。按照约定，将使用父模型的"蛇形命名"加上`_id`后缀作为外键。因此，在这个例子中，jmodel会假定`Post`模型上的外键是`user_id`。

如果您希望覆盖这个约定，可以在定义关联时传递自定义外键：

```java
@HasMany(foreignKey = "user_id", localKey = "id")
private List<Post> posts;
```

一旦定义了关联，就可以访问帖子集合：

```java
User user = userMapper.selectById(1L);
List<Post> posts = user.get(User::getPosts);
```

您也可以使用`load`方法预加载关联数据：

```java
List<User> users = userMapper.selectBatchIds(Arrays.asList(1L, 2L));
RelationUtils.load(users, User::getPosts);

// 现在可以直接访问已加载的关联
User user = users.get(0);
List<Post> posts = user.getPosts();
```

#### 自动为子级添加父级模型（Chaperone）

当您使用`HasMany`或`HasOne`关联时，有时需要在子模型中访问父模型。jmodel通过`chaperone`参数提供了这种能力：

```java
@HasMany(chaperone = true)
private List<Post> postChaperones;
```

这样，每个`Post`对象都会有一个指向其父`User`的引用：

```java
User user = userMapper.selectById(1L);
user.load(User::getPostChaperones);

List<Post> postChaperones = user.getPostChaperones();
// postChaperones中的Post对象有User引用
assertEquals(postChaperones.get(0).getUser(), user);
```

### 一对一（反向）

我们已经探讨了如何访问子模型的记录，现在让我们定义一个从子模型访问父模型的关联。要定义这种关联，请在子模型上使用`@BelongsTo`注解：

```java
import com.github.biiiiiigmonster.Model;

@TableName
class Phone extends Model<Phone> {
    /**
     * 获取这个电话所属的用户 
     */
    @BelongsTo
    private User user;  
}
```

在上面的例子中，jmodel将尝试匹配`Phone`模型上的`user_id`与`User`模型上的`id`。jmodel通过检查关联方法的名称并使用`_id`后缀来确定外键的默认名称。但是，如果`Phone`模型上的外键不是`user_id`，您可以传递自定义键名：

```java
@BelongsTo(foreignKey = "user_id", ownerKey = "id")
private User user;
```

一旦定义了关联，就可以访问父模型：

```java
Phone phone = phoneMapper.selectById(1L);
User user = phone.get(Phone::getUser);
```

### 远程一对一

远程一对一关联类似于远程一对多关联；但是，最终关联的结果是单个模型实例而不是集合。例如，一个`Mechanic`模型可能通过中间的`Car`模型与一个`Owner`模型相关联：

```java

import com.github.biiiiiigmonster.Model;

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

"远程"一对多关联通过中间关联提供了方便的快捷方式。例如，如果一个`Country`模型通过中间的`User`模型拥有多个`Post`模型，我们可以直接访问一个国家的所有帖子：

```java
import com.github.biiiiiigmonster.Model;

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

多对多关联比`HasOne`和`HasMany`关联更复杂。例如，一个用户可能有多个角色，而一个角色可能被多个用户共享。例如，许多用户可能具有"管理员"角色。要定义这种关联，需要三个数据库表：`users`、`roles`和`role_user`。`role_user`表是根据相关模型的名称按字母顺序命名的，包含`user_id`和`role_id`列。

多对多关联使用`@BelongsToMany`注解定义：

```java
import com.github.biiiiiigmonster.Model;

@TableName
class User extends Model<User> {
    /**
     * 属于用户的角色
     */
    @BelongsToMany(using = UserRole.class)
    private List<Role> roles;  
}
```

一旦定义了关联，就可以使用`roles`属性访问用户的角色：

```java
User user = userMapper.selectById(1L);
List<Role> roles = user.get(User::getRoles);
```

#### 自定义中间表列名

如果您需要自定义连接表上的列名，可以使用`foreignPivotKey`和`relatedPivotKey`参数：

```java
@BelongsToMany(
    using = UserRole.class,
    foreignPivotKey = "user_id",
    relatedPivotKey = "role_id"
)
private List<Role> roles;
```

#### 检索中间表列

使用多对多关联时，您可能需要访问中间表的属性。要做到这一点，请使用`withPivot`参数：

```java
@BelongsToMany(
    using = UserRole.class,
    withPivot = true
)
private List<Role> roles;
```

这样，每个`Role`模型都会有一个`pivot`属性，包含中间表的信息：

```java
User user = userMapper.selectById(1L);
List<Role> roles = user.get(User::getRoles);
// 访问中间表数据
UserRole pivot = roles.get(0).getPivot();
```

### 一对一（多态）

多态一对一关联类似于典型的一对一关联；但是，目标模型可以属于多种类型的模型。例如，`Post`模型和`User`模型可能共享与`Image`模型的关联：

```java
import com.github.biiiiiigmonster.Model;

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

多态关联允许目标模型属于多种类型的模型。例如，假设您的应用中有`Post`和`Video`模型，并且每个都可以有多个`Comment`模型。使用多态关联，您可以使用单个`comments`关联访问两种模型的所有评论：

```java
import com.github.biiiiiigmonster.Model;

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

您可以自定义多态关联的类型和ID字段：

```java
@MorphMany(type = "commentable_type", id = "commentable_id")
private List<Comment> comments;
```

#### 自定义多态类型

默认情况下，jmodel将使用完全限定的类名作为多态关联的"类型"值。例如，给定上面的`Post`和`Video`模型的例子，默认情况下存储在`commentable_type`列中的值将是`com.example.Post`或`com.example.Video`。

如果您希望使用自定义值，可以使用`@MorphAlias`注解：

```java
@MorphAlias("post")
public class Post extends Model<Post> {
    // ...
}
```
> `@MorphAlias`支持默认值，为当前类的simpleName

### 多态反向关联

#### 多对多（多态）

多态多对多关联表示如标签系统等复杂关联。例如，您的应用可能允许用户标记帖子和视频。使用多态多对多关联，您可以使用单个`tags`关联访问这两种模型的所有标签：

```java
import com.github.biiiiiigmonster.Model;

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

要定义多态多对多关联的反向，请在相关模型上使用`@MorphedByMany`注解：

```java
import com.github.biiiiiigmonster.Model;

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

jmodel提供了几种不同的方法来加载关联数据：

### 即时加载

您可以使用`get`方法即时加载关联数据：

```java
User user = userMapper.selectById(1L);
List<Post> posts = user.get(User::getPosts);
```

### 预加载

为了避免N+1查询问题，您可以使用`load`方法预加载关联：

```java
User user = userMapper.selectById(1L);
user.load(User::getPosts);
List<Post> posts = user.getPosts();
```

对于集合，您可以使用`RelationUtils.load`方法：

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

这将加载用户的所有文章及其评论。