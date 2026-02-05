# Agent Skill Manager - 项目结构规范

本文档描述 Agent Skill Manager 框架的代码结构规范和包组织方式。

## 目录结构

```
src/main/java/org/unreal/agent/skill/
├── config/                           # 配置类
│   ├── AgentSkillAutoConfiguration.java
│   ├── AgentSkillConfiguration.java
│   └── AgentSkillProperties.java
│
├── dto/                              # 数据传输对象（DTO）
│   ├── SkillExecuteResultDto.java
│   ├── SkillMetadataDto.java
│   ├── SkillValidationDto.java
│   └── SkillValidationResult.java
│
├── example/                          # 示例技能
│   ├── DateTimeSkill.java
│   └── TextAnalysisSkill.java
│
├── folder/                           # 文件夹技能支持
│   ├── model/                        # 领域模型
│   │   ├── SkillEntryPoint.java
│   │   ├── SkillParameters.java
│   │   └── SkillResources.java
│   │
│   ├── AgentSkillManager.java
│   ├── AgentskillsManager.java
│   ├── FolderBasedSkillLoader.java
│   ├── MarkdownAgentSkill.java
│   ├── ProgressiveDisclosureService.java
│   ├── SkillDescriptor.java
│   ├── SkillLifecycleManager.java
│   ├── SkillMarkdownParser.java
│   ├── SkillMetadataService.java
│   ├── SkillMigrationUtils.java
│   ├── SkillValidator.java
│   └── YamlPropertiesReader.java
│
├── vo/                               # 值对象（VO）
│   ├── SkillDiscoveryVo.java
│   ├── SkillFunctionVo.java
│   └── SkillMetadataVo.java
│
├── springai/                         # Spring AI 集成
│   └── SpringAIAgentSkillAdapter.java
│
├── web/                              # Web 控制器
│   └── AgentSkillController.java
│
├── AgentSkill.java                   # 核心接口
├── AgentSkillManager.java            # 技能管理器
└── AgentSkillResult.java             # 技能执行结果
```

## 包规范说明

### 1. config 包
**用途**: 存放所有配置相关的类
**包含**:
- Spring Boot 自动配置类
- 配置属性类
- 配置条件类

**原则**: 
- 配置类应该与业务逻辑分离
- 使用 `@ConfigurationProperties` 绑定配置属性

### 2. dto 包
**用途**: 数据传输对象（Data Transfer Object）
**包含**:
- API 请求/响应对象
- 层与层之间的数据传输对象
- 贫血对象（只有 getter/setter，无业务逻辑）

**命名规范**: `*Dto` 或 `*Result` 后缀

### 3. model 包（子包）
**用途**: 领域模型对象
**包含**:
- 核心业务实体
- 值对象
- 复杂对象的组成部分

**示例**:
- `SkillDescriptor` 包含 `SkillParameters`、`SkillEntryPoint` 等模型

### 4. vo 包
**用途**: 值对象（Value Object）
**包含**:
- 不可变对象
- 用于传递只读数据
- 轻量级数据传输

**特点**:
- 通常字段为 `final`
- 通过构造函数初始化
- 无 setter 方法

### 5. common 包（可选）
**用途**: 通用工具类和常量
**包含**:
- 通用工具类
- 常量定义
- 枚举类型

**建议**:
- 如果项目规模不大，可以放在各自包内
- 避免过度设计

## 代码长度规范

### 推荐限制
- **类长度**: 不超过 400 行（合理范围 200-400 行）
- **方法长度**: 不超过 50 行
- **参数数量**: 不超过 5 个
- **嵌套层级**: 不超过 3 层

### 重构策略

#### 当类超过 400 行时：
1. **提取内部类**: 将 public static class 提取为独立的类
2. **按职责拆分**: 根据单一职责原则拆分为多个服务类
3. **使用组合**: 将复杂功能委托给其他类

#### 示例
重构前的 `AgentskillsManager` (431 行):
```java
@Component
public class AgentskillsManager {
    // 验证逻辑 (100 行)
    // 元数据逻辑 (150 行)
    // 内部类 SkillMetadata
    // 内部类 SkillValidationResult
}
```

重构后:
```java
// AgentskillsManager.java (约 80 行)
@Component
public class AgentskillsManager {
    @Autowired private SkillValidator validator;
    @Autowired private SkillMetadataService metadataService;
}

// SkillValidator.java (约 82 行)
@Component
public class SkillValidator {
    // 验证逻辑
}

// SkillMetadataService.java (约 152 行)
@Service
public class SkillMetadataService {
    // 元数据逻辑
}

// vo/SkillMetadataVo.java
public class SkillMetadataVo { }

// dto/SkillValidationResult.java
public class SkillValidationResult { }
```

## 贫血对象 vs 富对象

### 贫血对象（DTO/VO）
**特征**:
- 只有字段和 getter/setter
- 无业务逻辑
- 用于数据传输和存储

**位置**:
- `dto/` 包
- `vo/` 包
- `model/` 包（部分）

### 富对象（Domain Object）
**特征**:
- 包含业务逻辑
- 封装行为和状态
- 自包含的完整性

**位置**:
- `folder/` 包中的主要服务类
- 实现接口的核心类

## 重构前后对比

### 文件数量
- **重构前**: 21 个 Java 文件
- **重构后**: 33 个 Java 文件

### 代码分布
| 类别 | 重构前 | 重构后 | 变化 |
|------|--------|--------|------|
| 超长类 (>400行) | 5 个 | 3 个 | -2 |
| 长类 (300-400行) | 2 个 | 2 个 | 0 |
| 中等类 (100-300行) | 6 个 | 13 个 | +7 |
| 小类 (<100行) | 8 个 | 15 个 | +7 |

### 职责分离
- **重构前**: `AgentskillsManager` 负责验证、元数据、搜索等多个职责
- **重构后**: 
  - `SkillValidator` - 负责验证
  - `SkillMetadataService` - 负责元数据管理
  - `AgentskillsManager` - 负责协调

## 命名规范

### 类命名
- **Service**: 业务服务层，后缀 `*Service`
- **Manager**: 管理类，后缀 `*Manager`
- **Controller**: 控制器，后缀 `*Controller`
- **Configuration**: 配置类，后缀 `*Configuration` 或 `*Config`
- **Properties**: 配置属性，后缀 `*Properties`
- **Dto**: 数据传输对象，后缀 `*Dto`
- **Vo**: 值对象，后缀 `*Vo`
- **Model**: 领域模型，无特定后缀或 `*Model`

### 方法命名
- **查询**: `get*`, `find*`, `query*`
- **操作**: `create*`, `update*`, `delete*`, `execute*`
- **验证**: `validate*`, `check*`, `is*`
- **转换**: `to*`, `from*`, `convert*`

## 最佳实践

1. **单一职责**: 每个类只负责一项明确的职责
2. **依赖注入**: 使用 Spring 的依赖注入，避免硬编码
3. **接口隔离**: 依赖接口而非具体实现
4. **包内聚**: 同一包内的类应该高内聚
5. **最小暴露**: 只暴露必要的公共方法

## 测试建议

### 单元测试
- 针对 Service 和 Manager 类编写单元测试
- 测试覆盖率目标: 业务逻辑层 80%+

### 集成测试
- 测试 Spring Boot 上下文加载
- 测试配置属性绑定

## 后续维护

### 新增功能时
1. 先确定属于哪个包
2. 检查是否需要新建 DTO/VO
3. 避免类过长，及时拆分
4. 更新本文档

### 代码审查清单
- [ ] 类长度是否超过 400 行
- [ ] 是否遵循包结构规范
- [ ] DTO/VO 是否放在正确位置
- [ ] 命名是否符合规范
- [ ] 是否包含适当的 JavaDoc