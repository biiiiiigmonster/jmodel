# JModel Dirty-Tracking 实现计划

> **版本**: v1.0  
> **创建时间**: 2026-01-30  
> **状态**: 待实施

---

## 1. 需求概述

### 1.1 背景

JModel 是一个类似 Laravel Eloquent 的 Java ORM 框架。当前框架支持实体的 CRUD 操作和事件分发，但缺少 **dirty-tracking（脏数据追踪）** 功能。用户在事件监听器中无法知道实体的哪些字段被修改了。

### 1.2 目标

为 Model 基类添加 dirty-tracking 功能，使用户能够：

1. 知道实体哪些字段被修改了（`isDirty()`, `getDirty()`）
2. 获取字段的原始值（`getOriginal()`）
3. 在 save 后查询本次保存涉及的变更（`wasChanged()`, `getChanges()`）

### 1.3 核心要求

| 要求 | 说明 |
|------|------|
| **追踪时机** | 从数据库加载后的任何修改 |
| **追踪方式** | 字节码增强（主要）+ 快照对比（兜底） |
| **支持场景** | Lombok setter、直接字段赋值、反射赋值 |
| **事件不变** | 事件只传递 model，不增加额外字段 |
| **对用户透明** | 用户无需修改现有实体代码 |

---

## 2. 技术方案

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         编译时增强流程                               │
│                                                                     │
│   User.java ──► Lombok ──► User.class ──► ByteBuddy ──► User.class  │
│   (源码)        (生成setter)  (原始字节码)   (增强setter)   (最终字节码) │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         运行时数据流                                 │
│                                                                     │
│   实体创建（ORM/new）──► setter调用 ──► trackChange() ──► 忽略       │
│           │                                  (original=null)        │
│           │                                                         │
│           ▼                                                         │
│   syncOriginal() ──► 建立快照 ──► original Map                      │
│           │                                                         │
│           ▼                                                         │
│   setter调用 ──► trackChange() ──► 记录到 changes Set               │
│           │                                                         │
│           ▼                                                         │
│       save() ──► 快照对比（兜底）──► 保存 changes ──► syncOriginal() │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 混合追踪策略

为确保**所有修改场景**都能被追踪，采用双重机制：

| 机制 | 覆盖场景 | 说明 |
|------|----------|------|
| **Setter 拦截** | `user.setName("xxx")` | 字节码增强，在 setter 开头注入追踪代码 |
| **快照对比** | `user.name = "xxx"`<br>`ReflectUtil.setFieldValue(...)` | 在 `save()` 前对比 original 与当前值 |

**优先级**：Setter 拦截的变更实时记录，快照对比在 save 前补充检测遗漏。

### 2.3 字节码增强详情

#### 2.3.1 增强目标

- **目标类**：所有直接或间接继承 `io.github.biiiiiigmonster.Model` 的类
- **目标方法**：所有 `setXxx(value)` 形式的 setter 方法
- **排除字段**：
  - 带 `@TableField(exist = false)` 注解的字段（关系字段）
  - 带 JModel 关系注解的字段：`@HasOne`, `@HasMany`, `@BelongsTo`, `@BelongsToMany`, `@HasOneThrough`, `@HasManyThrough`, `@MorphOne`, `@MorphMany`, `@MorphTo`, `@MorphToMany`, `@MorphedByMany`
  - `transient` 修饰的字段
  - `static` 修饰的字段

#### 2.3.2 增强后的代码逻辑

```java
// 原始 setter（Lombok 生成）
public void setName(String name) {
    this.name = name;
}

// 增强后的 setter
public void setName(String name) {
    // 注入的代码 ↓
    this.$jmodel$trackChange("name", this.name, name);
    // 原始代码 ↓
    this.name = name;
}
```

#### 2.3.3 增强时机

**编译时增强**（通过 Maven 插件），在以下阶段执行：

```
compile → (Lombok 处理) → process-classes → (ByteBuddy 增强) → package
```

### 2.4 Model 基类扩展

#### 2.4.1 新增字段

```java
public abstract class Model<T extends Model<?>> {
    // === 新增字段 ===
    
    /**
     * 原始值快照（惰性初始化：首次 setter 调用时创建）
     * key: 字段名, value: 原始值
     * 初始值为 null，表示尚未建立快照
     */
    private transient Map<String, Object> $jmodel$original = null;
    
    /**
     * 已变更的字段名集合（setter 调用时记录）
     */
    private transient Set<String> $jmodel$changes = new HashSet<>();
    
    /**
     * 最近一次 save 涉及的变更（save 后保留，供 wasChanged 使用）
     */
    private transient Map<String, Object> $jmodel$savedChanges = new HashMap<>();
    
    // ... 现有字段 ...
}
```

> **命名约定**：内部字段使用 `$jmodel$` 前缀，避免与用户字段冲突。
> **惰性初始化**：`$jmodel$original` 初始为 `null`，在首次 setter 调用时创建快照。

#### 2.4.2 新增方法

| 方法签名 | 返回类型 | 说明 |
|----------|----------|------|
| `isDirty()` | `boolean` | 是否有任何字段被修改 |
| `isDirty(String... fields)` | `boolean` | 指定字段是否被修改 |
| `isDirty(SerializableFunction<T, ?>... columns)` | `boolean` | 类型安全版本 |
| `getDirty()` | `Map<String, Object>` | 获取所有脏字段及当前值 |
| `getDirty(String... fields)` | `Map<String, Object>` | 只获取指定字段的脏数据 |
| `getOriginal()` | `Map<String, Object>` | 获取所有原始值 |
| `getOriginal(String field)` | `Object` | 获取指定字段的原始值 |
| `getOriginal(String field, Object defaultValue)` | `Object` | 带默认值 |
| `getOriginal(SerializableFunction<T, R> column)` | `R` | 类型安全版本 |
| `wasChanged()` | `boolean` | 最近 save 是否有变更 |
| `wasChanged(String... fields)` | `boolean` | 指定字段在最近 save 中是否变更 |
| `getChanges()` | `Map<String, Object>` | 获取最近 save 的变更 |
| `syncOriginal()` | `void` | 将当前值同步为原始值 |
| `syncOriginal(String... fields)` | `void` | 只同步指定字段 |
| `$jmodel$trackChange(String field, Object oldVal, Object newVal)` | `void` | 内部方法，供增强后的 setter 调用 |

#### 2.4.3 快照对比逻辑

在 `save()` 方法中，处理追踪状态和变更检测：

```java
public Boolean save() {
    // 1. 记录是否处于未追踪状态
    boolean wasUntracked = (this.$jmodel$original == null);
    
    // 2. 如果未追踪，先建立快照（用于 wasChanged 检测）
    if (wasUntracked) {
        syncOriginal();
    }
    
    // 3. 快照对比（兜底检测：直接赋值和反射赋值）
    detectUntrackedChanges();
    
    // 4. 保存前的变更快照（供 wasChanged 使用）
    Map<String, Object> currentChanges = new HashMap<>(getDirty());
    
    // 5. 原有逻辑：发布事件、执行数据库操作
    // ...
    
    // 6. 保存成功后
    if (res > 0) {
        this.$jmodel$savedChanges = currentChanges;
        syncOriginal();  // 重置快照，开始追踪后续变更
    }
    
    return res > 0;
}

private void detectUntrackedChanges() {
    if (this.$jmodel$original == null) {
        return;
    }
    
    // 遍历所有持久化字段，对比 original 与当前值
    // 检测未通过 setter 追踪到的变更（如直接赋值、反射赋值）
    for (Field field : getTrackableFields()) {
        String name = field.getName();
        Object originalValue = this.$jmodel$original.get(name);
        Object currentValue = ReflectUtil.getFieldValue(this, name);
        
        if (!Objects.equals(originalValue, currentValue)) {
            this.$jmodel$changes.add(name);
        }
    }
}
```

### 2.5 显式启用追踪策略

#### 2.5.1 设计原则

**核心问题**：ORM 框架（如 MyBatis-Plus）在从数据库加载数据时，也是通过 setter 方法设置字段值。如果在 setter 中自动触发追踪，会把 ORM 填充误认为用户修改。

**解决方案**：采用显式启用追踪的策略：
- 默认不追踪：实体创建后处于 `UNTRACKED` 状态
- 显式启用：调用 `syncOriginal()` 后进入 `TRACKING` 状态
- save 后自动启用：`save()` 成功后自动进入 `TRACKING` 状态

#### 2.5.2 实体状态机

```
┌─────────────────────────────────────────────────────────────────┐
│                      实体状态机                                  │
│                                                                 │
│   UNTRACKED（未追踪）                                            │
│       │                                                         │
│       │  ORM 填充、new 创建 ──► setter 调用 ──► 不做任何追踪      │
│       │                                                         │
│       │  syncOriginal() 显式调用                                 │
│       │  或 save() 成功后                                        │
│       ▼                                                         │
│   TRACKING（追踪中）                                             │
│       │                                                         │
│       │  setter 调用 ──► 记录变更到 changes                      │
│       │                                                         │
│       │  save() 成功后                                          │
│       ▼                                                         │
│   TRACKING（追踪中）──► 重置 original，清空 changes              │
└─────────────────────────────────────────────────────────────────┘
```

#### 2.5.3 实现要点

**`$jmodel$trackChange` 方法**：

```java
public void $jmodel$trackChange(String field, Object oldValue, Object newValue) {
    // 关键：未追踪状态下不做任何事
    if (this.$jmodel$original == null) {
        return;
    }
    
    // 追踪状态下记录变更
    Object originalValue = this.$jmodel$original.get(field);
    if (!Objects.equals(originalValue, newValue)) {
        this.$jmodel$changes.add(field);
    } else {
        // 如果恢复为原始值，移除变更标记
        this.$jmodel$changes.remove(field);
    }
}
```

**`syncOriginal` 方法**：

```java
public void syncOriginal() {
    this.$jmodel$original = new HashMap<>();
    for (Field field : getTrackableFields()) {
        Object value = ReflectUtil.getFieldValue(this, field.getName());
        this.$jmodel$original.put(field.getName(), value);
    }
    this.$jmodel$changes.clear();
}
```

#### 2.5.4 场景分析

| 场景 | 行为 | 结果 |
|------|------|------|
| ORM 查询后直接修改 | setter 调用时 original=null | ❌ 不追踪（需先调用 syncOriginal） |
| ORM 查询 → syncOriginal → 修改 | 启用追踪后修改 | ✅ 正确追踪 |
| new User() → 设置字段 → save | save 时 original=null | ✅ save 后自动启用追踪 |
| save 后修改 | 已处于追踪状态 | ✅ 正确追踪 |

#### 2.5.5 使用模式

**模式1：查询后手动启用追踪**
```java
User user = userMapper.selectById(1L);
user.syncOriginal();  // 显式启用追踪
user.setName("新名字");
user.isDirty("name");  // true
```

**模式2：save 后自动追踪**
```java
User user = new User();
user.setName("张三");
user.save();  // save 成功后自动启用追踪

user.setName("李四");
user.isDirty("name");  // true（相对于"张三"）
```

#### 2.5.6 已知限制

| 场景 | 说明 |
|------|------|
| 查询后未调用 syncOriginal | 变更不被追踪，isDirty 返回 false |
| 首次 save 前的变更 | wasChanged 可用（通过 save 时快照对比） |

**最佳实践**：
- 查询后立即调用 `syncOriginal()` 开始追踪

---

## 3. 模块结构

### 3.1 模块变更

```
jmodel/
├── jmodel-core/                    # 现有：核心模块
│   └── src/main/java/
│       └── io/github/biiiiiigmonster/
│           ├── Model.java          # 修改：添加 dirty-tracking 方法
│           └── tracking/           # 新增：追踪相关工具类
│               └── TrackingUtils.java
│
└── jmodel-enhance-plugin/          # 新增：Maven 插件模块
    ├── pom.xml
    └── src/main/java/
        └── io/github/biiiiiigmonster/enhance/
            ├── JModelEnhanceMojo.java      # Maven Mojo
            ├── ModelClassEnhancer.java     # 字节码增强逻辑
            └── SetterInterceptor.java      # Setter 拦截器
```

### 3.2 依赖关系

```
jmodel-enhance-plugin
    └── depends on: ByteBuddy, Maven Plugin API

jmodel-core
    └── no new dependencies (tracking logic is self-contained)
```

---

## 4. 实现任务清单

### Phase 1: Model 基类扩展

| 任务 ID | 任务描述 | 优先级 | 状态 |
|---------|----------|--------|------|
| P1-01 | 在 Model 中添加 `$jmodel$original`, `$jmodel$changes`, `$jmodel$savedChanges` 字段 | P0 | ✅ 已完成 |
| P1-02 | 实现 `$jmodel$trackChange(field, oldVal, newVal)` 方法 | P0 | ✅ 已完成 |
| P1-03 | 实现 `isDirty()` 系列方法 | P0 | ✅ 已完成 |
| P1-04 | 实现 `getDirty()` 系列方法（含 only 过滤） | P0 | ✅ 已完成 |
| P1-05 | 实现 `getOriginal()` 系列方法 | P0 | ✅ 已完成 |
| P1-06 | 实现 `wasChanged()` 和 `getChanges()` 方法 | P0 | ✅ 已完成 |
| P1-07 | 实现 `syncOriginal()` 系列方法 | P0 | ✅ 已完成 |
| P1-08 | 创建 `TrackingUtils` 工具类（获取可追踪字段列表等） | P1 | ✅ 已完成 |
| P1-09 | 实现快照对比逻辑 `detectUntrackedChanges()` | P0 | ✅ 已完成 |
| P1-10 | 修改 `save()` 方法，集成 dirty-tracking | P0 | ✅ 已完成 |

### Phase 2: ByteBuddy 增强插件

| 任务 ID | 任务描述 | 优先级 | 状态 |
|---------|----------|--------|------|
| P2-01 | 创建 `jmodel-enhance-plugin` Maven 模块 | P0 | ✅ 已完成 |
| P2-02 | 添加 ByteBuddy 和 Maven Plugin API 依赖 | P0 | ✅ 已完成 |
| P2-03 | 实现 `JModelEnhanceMojo`（Maven 插件入口） | P0 | ✅ 已完成 |
| P2-04 | 实现 Model 子类扫描逻辑 | P0 | ✅ 已完成 |
| P2-05 | 实现可追踪字段识别逻辑（排除关系字段等） | P0 | ✅ 已完成 |
| P2-06 | 实现 setter 方法增强逻辑 | P0 | ✅ 已完成 |
| P2-07 | 处理 Lombok 生成的 setter 兼容性 | P1 | ✅ 已完成 |
| P2-08 | 添加增强日志输出 | P2 | ✅ 已完成 |

### Phase 3: 集成验证

| 任务 ID | 任务描述 | 优先级 | 状态 |
|---------|----------|--------|------|
| P3-01 | 验证通过 ORM 查询的实体能正确追踪 | P0 | ✅ 已完成 |
| P3-02 | 验证直接 mapper 查询的实体能正确追踪 | P0 | ✅ 已完成 |
| P3-03 | 验证 `new` 创建的实体能正确追踪 | P0 | ✅ 已完成 |
| P3-04 | 文档说明已知限制和最佳实践 | P1 | ✅ 已完成 |

### Phase 4: 测试

| 任务 ID | 任务描述 | 优先级 | 状态 |
|---------|----------|--------|------|
| P4-01 | 编写 Model dirty-tracking 单元测试 | P0 | ✅ 已完成 |
| P4-02 | 编写 setter 拦截测试 | P0 | ✅ 已完成 |
| P4-03 | 编写直接字段赋值测试 | P0 | ✅ 已完成 |
| P4-04 | 编写反射赋值测试 | P0 | ✅ 已完成 |
| P4-05 | 编写 save 后状态重置测试 | P0 | ✅ 已完成 |
| P4-06 | 编写事件监听器中使用 dirty-tracking 的集成测试 | P1 | ✅ 已完成 |

---

## 5. API 规范

### 5.1 公开 API

```java
public abstract class Model<T extends Model<?>> {
    
    // ==================== Dirty Checking ====================
    
    /**
     * 判断是否有任何字段被修改
     */
    public boolean isDirty();
    
    /**
     * 判断指定字段是否被修改
     * @param fields 字段名
     */
    public boolean isDirty(String... fields);
    
    /**
     * 判断指定字段是否被修改（类型安全）
     * @param columns 字段引用
     */
    @SafeVarargs
    public final <R> boolean isDirty(SerializableFunction<T, R>... columns);
    
    /**
     * 获取所有脏字段及其当前值
     * @return Map<字段名, 当前值>
     */
    public Map<String, Object> getDirty();
    
    /**
     * 获取指定字段的脏数据（only 过滤）
     * @param fields 要获取的字段名
     */
    public Map<String, Object> getDirty(String... fields);
    
    // ==================== Original Values ====================
    
    /**
     * 获取所有原始值
     */
    public Map<String, Object> getOriginal();
    
    /**
     * 获取指定字段的原始值
     */
    public Object getOriginal(String field);
    
    /**
     * 获取指定字段的原始值，如果不存在则返回默认值
     */
    public Object getOriginal(String field, Object defaultValue);
    
    /**
     * 获取指定字段的原始值（类型安全）
     */
    public <R> R getOriginal(SerializableFunction<T, R> column);
    
    // ==================== Was Changed (After Save) ====================
    
    /**
     * 判断最近一次 save 是否有任何变更
     */
    public boolean wasChanged();
    
    /**
     * 判断指定字段在最近一次 save 中是否变更
     */
    public boolean wasChanged(String... fields);
    
    /**
     * 获取最近一次 save 的所有变更
     */
    public Map<String, Object> getChanges();
    
    // ==================== Sync Original ====================
    
    /**
     * 将当前所有字段值同步为原始值，并清空变更记录
     */
    public void syncOriginal();
    
    /**
     * 将指定字段的当前值同步为原始值
     */
    public void syncOriginal(String... fields);
}
```

### 5.2 内部 API（供字节码增强调用）

```java
/**
 * 记录字段变更（由增强后的 setter 调用）
 * 方法名使用 $jmodel$ 前缀避免与用户方法冲突
 * 
 * @param field 字段名
 * @param oldValue 旧值
 * @param newValue 新值
 */
public void $jmodel$trackChange(String field, Object oldValue, Object newValue);
```

---

## 6. 使用示例

### 6.1 Maven 配置

用户需要在 `pom.xml` 中添加增强插件：

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

### 6.2 基本使用

```java
// 从数据库加载用户
User user = userMapper.selectById(1L);

// 启用追踪（重要！）
user.syncOriginal();

// 修改字段
user.setName("新名字");
user.setEmail("new@example.com");

// 查询脏状态
System.out.println("isDirty", user.isDirty());           // true
System.out.println("isDirty name", user.isDirty("name"));     // true
System.out.println("isDirty id", user.isDirty("id"));       // false

// 获取脏字段
Map<String, Object> dirty = user.getDirty();
System.out.println("dirty", dirty);  // {name=新名字, email=new@example.com}

// 获取原始值
System.out.println("original name", user.getOriginal("name"));  // 旧名字

// 只获取部分脏字段
Map<String, Object> partial = user.getDirty("name");
System.out.println("partial", partial);  // {name=新名字}
```

### 6.3 保存后查询变更

```java
user.setName("新名字");
user.save();

// 保存后脏状态已重置
System.out.println("isDirty", user.isDirty());  // false

// 但可以查询本次保存的变更
System.out.println("wasChanged", user.wasChanged());        // true
System.out.println("wasChanged name", user.wasChanged("name")); // true
System.out.println("getChanges", user.getChanges());       // {name=新名字}
```

### 6.4 在事件监听器中使用

```java
@Component
public class UserEventListener {
    
    @EventListener
    public void onUserUpdated(ModelUpdatedEvent<User> event) {
        User user = event.getModel();
        
        // 检查是否修改了敏感字段
        if (user.wasChanged("email", "phone")) {
            // 发送安全通知
            securityService.notifyProfileChange(user);
        }
        
        // 获取所有变更用于审计
        Map<String, Object> changes = user.getChanges();
        auditService.logChanges(user.getId(), changes);
    }
}
```

### 6.5 新建实体的追踪

```java
// 新建实体，首次 save 前不追踪
User user = new User();
user.setName("张三");
user.setEmail("test@test.com");
user.isDirty("name");  // false（未启用追踪）

// save 成功后自动启用追踪
user.save();

// 后续修改可以被追踪
user.setName("李四");
user.isDirty("name");  // true
user.getOriginal("name");  // "张三"
```

---

## 7. 约束与边界条件

### 7.1 技术约束

| 约束 | 说明 |
|------|------|
| **Java 版本** | >= Java 8 |
| **依赖** | ByteBuddy 1.14.x（仅插件模块需要） |
| **编译顺序** | Lombok 必须在 ByteBuddy 增强之前执行 |
| **Lombok 版本** | 需兼容主流版本（1.18.x） |

### 7.2 功能边界

| 场景 | 是否支持 | 说明 |
|------|----------|------|
| syncOriginal 后 setter 修改 | ✅ | 实时追踪 |
| syncOriginal 前 setter 修改 | ❌ 不追踪 | 未启用追踪状态 |
| save 后 setter 修改 | ✅ | save 后自动启用追踪 |
| 直接字段赋值 | ✅ | 通过 save 前快照对比 |
| 反射赋值 | ✅ | 通过 save 前快照对比 |
| 关系字段修改 | ❌ 不追踪 | 关系字段有独立的事件机制 |
| transient 字段 | ❌ 不追踪 | 非持久化字段 |
| static 字段 | ❌ 不追踪 | 类级别字段 |
| 集合内部修改 | ⚠️ 有限支持 | 只检测引用变化，不检测集合内部元素变化 |

### 7.3 性能考虑

| 项目 | 影响 | 优化措施 |
|------|------|----------|
| 内存占用 | 每个实体增加 3 个 Map/Set | 使用 HashMap，懒初始化 |
| setter 性能 | 每次调用增加一次 Map 操作 | O(1) 操作，影响极小 |
| save 性能 | 快照对比需遍历所有字段 | 使用字段缓存，减少反射 |
| 编译时间 | 增加字节码增强阶段 | 只增强 Model 子类 |

### 7.4 线程安全

- `original`, `changes`, `savedChanges` 不是线程安全的
- 假设单个实体实例在单线程中使用（符合常见 ORM 使用模式）
- 如需多线程使用，用户需自行同步

---

## 8. 测试用例清单

### 8.1 单元测试

```java
// 1. 追踪状态测试
@Test void testIsDirty_beforeSyncOriginal_returnsFalse()
@Test void testIsDirty_afterSyncOriginal_andSetterCall_returnsTrue()
@Test void testSetterCall_beforeSyncOriginal_noTracking()

// 2. getDirty 测试
@Test void testGetDirty_returnsAllChangedFields()
@Test void testGetDirty_withFieldFilter_returnsFilteredFields()
@Test void testGetDirty_noChanges_returnsEmptyMap()

// 3. getOriginal 测试
@Test void testGetOriginal_returnsSyncedValue()
@Test void testGetOriginal_withDefault_returnsDefaultWhenNull()

// 4. wasChanged 测试
@Test void testWasChanged_afterSave_returnsTrue()
@Test void testWasChanged_beforeSave_returnsFalse()

// 5. syncOriginal 测试
@Test void testSyncOriginal_enablesTracking()
@Test void testSyncOriginal_clearsChanges()
@Test void testSyncOriginal_updatesOriginalValues()

// 6. save 后自动追踪
@Test void testSave_enablesTrackingAutomatically()
@Test void testSave_afterSave_setterCallIsTracked()

// 7. 直接赋值测试
@Test void testDirectFieldAssignment_detectedOnSave()

// 8. 关系字段不追踪
@Test void testRelationField_notTracked()
```

### 8.2 集成测试

```java
// 1. 完整流程测试
@Test void testFullWorkflow_load_modify_save_checkChanges()

// 2. 事件监听器测试
@Test void testEventListener_canAccessDirtyFields()

// 3. 多次修改测试
@Test void testMultipleModifications_allTracked()

// 4. 修改后恢复原值
@Test void testRevertToOriginal_notMarkedAsDirty()
```

---

## 9. 风险与缓解

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| ByteBuddy 与其他插件冲突 | 中 | 编译失败 | 提供配置选项调整执行顺序 |
| Lombok 版本不兼容 | 低 | setter 增强失败 | 测试主流版本，文档说明 |
| 快照对比性能问题 | 低 | save 变慢 | 字段缓存，只对比可追踪字段 |
| 用户自定义 setter | 中 | 增强逻辑干扰 | 检测并跳过非标准 setter |

---

## 10. 验收标准

### 10.1 功能验收

- [ ] 调用 `syncOriginal()` 后，setter 修改字段能被追踪
- [ ] 未调用 `syncOriginal()` 时，setter 修改不触发追踪
- [ ] `save()` 成功后自动启用追踪
- [ ] 直接字段赋值后，`save()` 时能通过快照对比检测到变更
- [ ] `getOriginal()` 返回 `syncOriginal()` 调用时的值
- [ ] `save()` 后 `isDirty()` 返回 false
- [ ] `save()` 后 `wasChanged()` 返回正确结果
- [ ] 关系字段不被追踪
- [ ] 事件监听器中可以使用所有 dirty-tracking API

### 10.2 非功能验收

- [ ] 单元测试覆盖率 > 80%
- [ ] 无新增编译警告
- [ ] 文档完整（README、API 文档）
- [ ] Maven 插件可正确发布和使用

---

## 11. 附录

### 11.1 相关文件路径

```
jmodel-core/src/main/java/io/github/biiiiiigmonster/
├── Model.java                          # 需修改（添加 dirty-tracking 方法）
├── tracking/
│   └── TrackingUtils.java              # 新增（获取可追踪字段等工具方法）

jmodel-enhance-plugin/
├── pom.xml                             # 新增
└── src/main/java/io/github/biiiiiigmonster/enhance/
    ├── JModelEnhanceMojo.java          # 新增（Maven 插件入口）
    ├── ModelClassEnhancer.java         # 新增（字节码增强逻辑）
    └── SetterInterceptor.java          # 新增（Setter 拦截器）
```

### 11.2 参考资料

- [Hibernate Dirty Checking](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#pc-managed-state)
- [ByteBuddy Documentation](https://bytebuddy.net/#/tutorial)
- [Laravel Eloquent Dirty Attributes](https://laravel.com/docs/eloquent#determining-if-attributes-have-been-modified)

### 11.3 变更记录

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-01-30 | v1.0 | 初始版本 |

---

> **下一步**: 确认此计划后，按 Phase 顺序开始实施。
