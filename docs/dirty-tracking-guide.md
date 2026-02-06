# JModel Dirty-Tracking 使用指南

> **版本**: 1.0.0  
> **最后更新**: 2026-02-06

---

## 1. 概述

JModel Dirty-Tracking 提供了实体字段变更追踪能力，让你能够：

- 知道实体哪些字段被修改了（`isDirty()`, `getDirty()`）
- 获取字段的原始值（`getOriginal()`）
- 在 `save()` 后查询本次保存涉及的变更（`wasChanged()`, `getChanges()`）

---

## 2. 快速开始

### 2.1 Maven 配置

在使用 dirty-tracking 的模块中，添加 `jmodel-enhance-plugin` 到构建插件：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.biiiiiigmonster</groupId>
            <artifactId>jmodel-enhance-plugin</artifactId>
            <version>${jmodel.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>enhance</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

该插件会在编译后自动增强所有 `Model` 子类的 setter 方法，注入变更追踪代码。

### 2.2 基本用法

```java
// 从数据库加载实体
User user = userMapper.selectById(1L);

// 启用追踪（重要！）
user.syncOriginal();

// 修改字段
user.setName("新名字");
user.setEmail("new@example.com");

// 查询脏状态
user.isDirty();           // true
user.isDirty("name");     // true
user.isDirty("id");       // false

// 获取脏字段及当前值
Map<String, Object> dirty = user.getDirty();
// {name=新名字, email=new@example.com}

// 获取原始值
user.getOriginal("name"); // 旧名字

// 保存
user.save();

// 保存后检查变更
user.isDirty();        // false（已重置）
user.wasChanged();     // true
user.getChanges();     // {name=新名字, email=new@example.com}
```

---

## 3. API 参考

### 3.1 启用追踪

| 方法 | 说明 |
|------|------|
| `syncOriginal()` | 将当前所有可追踪字段值保存为原始快照，进入 TRACKING 状态 |
| `syncOriginal(String... fields)` | 只同步指定字段的原始值（如果尚未追踪，等同于全量 syncOriginal） |

### 3.2 查询脏状态

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `isDirty()` | `boolean` | 是否有任何字段被修改 |
| `isDirty(String... fields)` | `boolean` | 指定字段中是否有任何一个被修改 |
| `isDirty(SerializableFunction<T, R>... columns)` | `boolean` | 类型安全版本（如 `user.isDirty(User::getName)`） |

### 3.3 获取脏数据

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getDirty()` | `Map<String, Object>` | 获取所有脏字段及其当前值 |
| `getDirty(String... fields)` | `Map<String, Object>` | 只获取指定字段的脏数据 |

### 3.4 获取原始值

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getOriginal()` | `Map<String, Object>` | 获取所有原始值（不可变 Map） |
| `getOriginal(String field)` | `Object` | 获取指定字段原始值（未追踪时返回 null） |
| `getOriginal(String field, Object defaultValue)` | `Object` | 获取原始值，不存在时返回默认值 |
| `getOriginal(SerializableFunction<T, R> column)` | `R` | 类型安全版本 |

### 3.5 保存后查询变更

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `wasChanged()` | `boolean` | 最近一次 save 是否有任何变更 |
| `wasChanged(String... fields)` | `boolean` | 指定字段在最近 save 中是否变更 |
| `getChanges()` | `Map<String, Object>` | 获取最近 save 的所有变更（不可变 Map） |

---

## 4. 使用模式

### 4.1 查询后手动启用追踪

```java
User user = userMapper.selectById(1L);
user.syncOriginal();  // 显式启用追踪

user.setName("新名字");
user.isDirty("name");  // true
```

### 4.2 save 后自动追踪

```java
User user = new User();
user.setName("张三");
user.save();  // save 成功后自动启用追踪

user.setName("李四");
user.isDirty("name");       // true（相对于"张三"）
user.getOriginal("name");   // "张三"
```

### 4.3 在事件监听器中使用

```java
@Component
public class UserEventListener {

    @EventListener
    public void onUserUpdated(ModelUpdatedEvent<User> event) {
        User user = event.getModel();

        // 检查敏感字段是否变更
        if (user.wasChanged("email", "phone")) {
            securityService.notifyProfileChange(user);
        }

        // 获取所有变更用于审计
        Map<String, Object> changes = user.getChanges();
        auditService.logChanges(user.getId(), changes);
    }
}
```

### 4.4 部分字段同步

```java
User user = userMapper.selectById(1L);
user.syncOriginal();

user.setName("新名字");
user.setEmail("new@test.com");

// 只同步 name 字段（将 name 的原始值更新为当前值）
user.syncOriginal("name");

user.isDirty("name");   // false（原始值已更新）
user.isDirty("email");  // true（未同步，仍为 dirty）
```

---

## 5. 追踪机制

### 5.1 双重追踪策略

JModel 使用两种机制确保变更检测的完整性：

| 机制 | 覆盖场景 | 时机 |
|------|----------|------|
| **Setter 拦截** | `user.setName("xxx")` | 实时（setter 调用时） |
| **快照对比** | 直接字段赋值、反射赋值 | 延迟（save 前） |

Setter 拦截通过编译时字节码增强实现，在每个 setter 方法开头注入追踪代码。快照对比在 `save()` 前执行，作为兜底机制检测未通过 setter 发生的变更。

### 5.2 实体状态机

```
UNTRACKED（未追踪）
    │  new 创建 / ORM 查询填充
    │  setter 调用 → 不追踪
    │
    │  syncOriginal() 或 save() 成功后
    ▼
TRACKING（追踪中）
    │  setter 调用 → 记录到 changes
    │
    │  save() 成功后
    ▼
TRACKING（追踪中）→ 重置 original，清空 changes
```

---

## 6. 已知限制

### 6.1 必须显式启用追踪

ORM 框架在从数据库加载数据时也通过 setter 设置字段值。为避免将 ORM 填充误认为用户修改，dirty-tracking 默认处于 **UNTRACKED** 状态。

**查询后必须手动调用 `syncOriginal()` 才能开始追踪变更。**

```java
// ❌ 错误：未启用追踪
User user = userMapper.selectById(1L);
user.setName("新名字");
user.isDirty("name");  // false！变更未被追踪

// ✅ 正确：先启用追踪
User user = userMapper.selectById(1L);
user.syncOriginal();  // 启用追踪
user.setName("新名字");
user.isDirty("name");  // true
```

### 6.2 集合内部修改不追踪

dirty-tracking 只检测字段引用的变化，不检测集合内部元素的增删改。

```java
user.syncOriginal();

// ❌ 不会被追踪（集合引用未变化）
user.getTags().add(newTag);

// ✅ 会被追踪（集合引用变化）
user.setTags(newTagList);
```

### 6.3 不追踪的字段类型

以下字段不参与 dirty-tracking：

| 字段类型 | 说明 |
|----------|------|
| 关系字段 | 带 `@HasOne`, `@HasMany`, `@BelongsTo`, `@BelongsToMany` 等关系注解的字段 |
| 计算属性 | 带 `@Attribute` 注解的字段 |
| `transient` 字段 | 非持久化字段 |
| `static` 字段 | 类级别字段 |

### 6.4 新实体首次 save 的 wasChanged

新创建的实体首次 `save()` 时，由于 `syncOriginal()` 在 `save()` 内部建立的快照与当前值一致，`wasChanged()` 可能返回 `false`。如需追踪首次 save 的变更，请在设置字段前手动调用 `syncOriginal()`。

### 6.5 线程安全

dirty-tracking 的内部状态（original、changes、savedChanges）**不是线程安全的**。假设单个实体实例在单线程中使用（符合常见 ORM 使用模式）。如需多线程共享实体，请自行同步。

---

## 7. 最佳实践

1. **查询后立即启用追踪**：在从数据库加载实体后，尽早调用 `syncOriginal()` 开始追踪。

2. **使用类型安全 API**：优先使用 Lambda 版本的 API（如 `isDirty(User::getName)`），避免字段名硬编码。

3. **在事件监听器中使用 `wasChanged`**：`save()` 后 `isDirty()` 已重置为 false，应使用 `wasChanged()` 和 `getChanges()` 查询本次保存的变更。

4. **避免在高并发场景中使用**：dirty-tracking 不是线程安全的，不要在多线程间共享被追踪的实体。

5. **关注增强插件配置**：确保 `jmodel-enhance-plugin` 在 Maven 构建中正确配置，否则 setter 拦截不会生效（只能依赖 save 前的快照对比兜底）。

---

## 8. FAQ

**Q: 为什么 `isDirty()` 一直返回 false？**

A: 检查是否调用了 `syncOriginal()`。未启用追踪时，所有 dirty 查询返回 false。

**Q: 为什么 save 后 `isDirty()` 返回 false？**

A: 这是正常行为。`save()` 成功后会重置追踪状态。使用 `wasChanged()` 查询本次 save 的变更。

**Q: 如何在没有 enhance 插件的情况下使用 dirty-tracking？**

A: 快照对比机制会在 `save()` 前兜底检测变更。但这意味着只有在 save 时才能检测到变更，实时的 `isDirty()` 查询可能不准确。建议始终配置 enhance 插件。

**Q: 修改后又恢复为原始值，会被标记为 dirty 吗？**

A: 不会。setter 拦截器会将新值与原始值对比，如果等于原始值会移除变更标记。
