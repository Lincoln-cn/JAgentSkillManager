# Agent Skill Manager Framework 架构重构文档

## 概述

本文档详细描述了 Agent Skill Manager Framework 的架构重构情况，包括重构的目标、实施的改进以及新的架构设计。

## 重构目标

本次架构重构旨在解决以下问题：

1. **安全性问题**：动态类加载存在安全隐患
2. **可扩展性问题**：原有架构难以扩展新功能
3. **性能问题**：缺少缓存机制和资源管理
4. **代码质量问题**：包结构混乱，职责不清
5. **配置管理问题**：配置验证和管理不够完善

## 重构后的新架构

### 1. 包结构优化

重构后的包结构遵循领域驱动设计（DDD），具体如下：

```
org.unreal.agent.skill/
├── core/                           # 核心接口和基础实现
│   ├── AgentSkill.java             # 核心技能接口
│   ├── AgentSkillResult.java       # 技能执行结果
│   └── exception/                  # 核心异常类
│       ├── SkillException.java
│       ├── SkillExecutionException.java
│       └── SkillValidationException.java
├── manager/                        # 技能管理器
│   ├── SkillManager.java           # 技能管理接口
│   ├── DefaultSkillManager.java    # 默认实现
│   ├── SpringBeanSkillManager.java # Spring Bean 管理器
│   ├── FolderSkillManager.java     # 文件夹技能管理器
│   └── registry/                   # 管理器注册和选择
│       ├── SkillManagerRegistry.java
│       └── SkillManagerSelector.java
├── loader/                         # 技能加载器
│   ├── SkillLoader.java            # 加载器接口
│   ├── SkillLoaderRegistry.java    # 加载器注册表
│   ├── impl/                       # 具体加载器实现
│   │   ├── JarSkillLoader.java
│   │   ├── ScriptSkillLoader.java
│   │   ├── ClassSkillLoader.java
│   │   └── FolderBasedSkillLoader.java
│   └── model/                      # 加载相关模型
│       ├── LoadedSkill.java
│       └── LoadContext.java
├── validator/                      # 验证器
│   ├── SkillValidator.java         # 验证器接口
│   └── DefaultSkillValidator.java
├── lifecycle/                      # 生命周期管理
│   ├── SkillLifecycleManager.java
│   └── event/                      # 生命周期事件
│       ├── SkillLoadedEvent.java
│       ├── SkillUnloadedEvent.java
│       └── SkillReloadedEvent.java
├── cache/                          # 缓存相关
│   ├── SkillCacheManager.java
│   ├── SkillMetadataCache.java
│   └── CacheConfig.java
├── config/                         # 配置相关
│   ├── AgentSkillProperties.java
│   ├── AgentSkillAutoConfiguration.java
│   └── AgentSkillConfiguration.java
├── migration/                      # 迁移工具
│   ├── SkillMigrationService.java
│   └── MigrationStrategy.java
├── web/                            # Web 层
│   ├── AgentSkillController.java
│   └── dto/                        # Web DTO
│       ├── SkillRegistrationRequest.java
│       └── SkillExecutionResponse.java
├── service/                        # 业务服务层
│   ├── SkillExecutionService.java
│   ├── SkillManagementService.java
│   └── SkillDiscoveryService.java
├── model/                          # 业务模型
│   ├── SkillDescriptor.java
│   ├── SkillMetadata.java
│   └── SkillParameter.java
└── util/                           # 工具类
    ├── SkillUtils.java
    ├── FileValidationUtils.java
    └── SecurityUtils.java
```

### 2. 核心接口和类

#### AgentSkill 接口

重构后的 `AgentSkill` 接口位于 `org.unreal.agent.skill.core` 包中，定义了技能的基本行为：

```java
public interface AgentSkill {
    String getName();
    String getDescription();
    String getVersion();
    boolean canHandle(String request);
    AgentSkillResult execute(String request, Map<String, Object> parameters);
    Map<String, String> getRequiredParameters();
    Map<String, String> getOptionalParameters();
    default String getInstructions() { return null; }
}
```

#### AgentSkillResult 类

重构后的 `AgentSkillResult` 类位于 `org.unreal.agent.skill.core` 包中，封装了技能执行结果：

```java
public class AgentSkillResult {
    private final boolean success;
    private final String message;
    private final Object data;
    private final String skillName;
    private final LocalDateTime executionTime;
    private final Map<String, Object> metadata;
    
    // Builder 模式实现
    public static Builder success();
    public static Builder failure();
}
```

### 3. 策略模式的技能管理器

重构后实现了策略模式的技能管理器体系：

#### SkillManager 接口

```java
public interface SkillManager {
    void registerSkill(AgentSkill skill);
    void unregisterSkill(String skillName);
    Collection<AgentSkill> getAllSkills();
    AgentSkill getSkill(String name);
    AgentSkill findSkillForRequest(String request);
    AgentSkillResult executeSkill(String skillName, String request, Map<String, Object> parameters);
    AgentSkillResult executeSkill(String request, Map<String, Object> parameters);
}
```

#### 具体实现

- `DefaultSkillManager`: 默认的内存中技能管理器
- `SpringBeanSkillManager`: 专门管理 Spring Bean 技能的管理器
- `FolderBasedSkillManager`: 专门管理文件夹技能的管理器
- `UnifiedSkillManager`: 统一技能管理器，委托给其他管理器

### 4. 插件化技能加载器架构

实现了插件化的技能加载器架构，支持多种技能来源：

#### SkillLoader 接口

```java
public interface SkillLoader {
    FolderBasedSkillLoader.LoadedSkill loadSkill(Object source);
    boolean supports(Object source);
    default int getPriority() { return 0; }
}
```

#### 具体加载器实现

- `FolderSkillLoader`: 从文件夹加载技能
- `SpringBeanSkillLoader`: 从 Spring Bean 加载技能
- `JarSkillLoader`: 从 JAR 文件加载技能
- `ScriptSkillLoader`: 从脚本文件加载技能

### 5. 事件驱动架构

实现了事件驱动架构，包括以下事件：

- `SkillLoadedEvent`: 技能加载事件
- `SkillUnloadedEvent`: 技能卸载事件
- `SkillExecutedEvent`: 技能执行事件

### 6. 安全性增强

#### SecureClassLoader

实现了安全的类加载器，限制动态类加载的风险：

```java
public class SecureClassLoader extends URLClassLoader {
    // 限制类加载的安全策略
    // 验证类名是否在允许的包列表中
    // 限制文件路径访问
}
```

#### InputValidationUtils

提供了输入验证工具：

```java
public class InputValidationUtils {
    public static boolean isValidSkillName(String skillName);
    public static boolean isValidFilePath(String path);
    public static boolean isValidInputLength(String input);
    public static String sanitizeInput(String input);
}
```

### 7. 缓存机制

实现了缓存机制以优化性能：

#### SimpleCache

```java
public class SimpleCache<K, V> {
    private final Map<K, CachedValue<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final int maxSize;
    // 基于 TTL 和大小限制的缓存实现
}
```

#### SkillCacheManager

```java
@Component
public class SkillCacheManager {
    private final SimpleCache<String, Object> skillMetadataCache;
    private final SimpleCache<String, Object> skillExecutionCache;
    // 缓存管理和服务
}
```

### 8. 配置管理改进

#### AgentSkillProperties

使用 JSR-303 注解进行配置验证：

```java
@ConfigurationProperties(prefix = "agent.skill")
@Validated
public class AgentSkillProperties {
    @Min(value = 1000, message = "Execution timeout must be at least 1000ms")
    private long executionTimeout = 30000;

    @NotBlank(message = "Skills directory cannot be blank")
    private String skillsDirectory = "skills";

    @Min(value = 100, message = "Watch polling interval must be at least 100ms")
    private long watchPollingInterval = 1000;
    // 其他带验证注解的属性
}
```

## 重构带来的好处

### 1. 安全性提升

- 实现了安全的类加载器，防止恶意代码注入
- 增加了输入验证和清理机制
- 实现了路径遍历防护

### 2. 可扩展性增强

- 采用策略模式，易于扩展新的技能管理器
- 插件化架构，易于添加新的技能加载器
- 事件驱动架构，易于监听和响应系统事件

### 3. 性能优化

- 实现了缓存机制，减少重复计算
- 优化了资源管理，避免内存泄漏
- 改进了类加载性能

### 4. 代码质量提升

- 清晰的包结构和职责分离
- 统一的异常处理机制
- 标准化的配置管理

### 5. 维护性改善

- 模块化设计，便于单独维护
- 标准化的接口设计
- 完善的文档和注释

## 迁移指南

对于使用旧版本的开发者，需要注意以下迁移到新架构的要点：

### 1. 包路径变更

- `org.unreal.agent.skill.AgentSkill` → `org.unreal.agent.skill.core.AgentSkill`
- `org.unreal.agent.skill.AgentSkillResult` → `org.unreal.agent.skill.core.AgentSkillResult`

### 2. 接口变更

- 建议使用 `SkillManager` 接口的实现类而非直接使用 `AgentSkillManager`
- 事件监听机制已通过 Spring 事件系统实现

### 3. 配置变更

- 配置属性增加了验证注解，确保配置的有效性
- 缓存和性能相关配置已标准化

## 总结

本次架构重构成功解决了原有系统存在的安全、扩展性、性能和维护性问题。新的架构设计遵循了现代软件工程的最佳实践，为系统的长期发展奠定了坚实的基础。