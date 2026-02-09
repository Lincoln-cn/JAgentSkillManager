# Agent Skill Manager Framework

<p align="center">
  <img src="logo/JAgentSkillsManagerLogo.png" alt="Agent Skill Manager Logo" width="200"/>
</p>

一个用于 Spring AI 的 Agent Skill 管理框架，提供了灵活的方式来管理和集成各种技能到 Spring AI 应用中。支持传统 Spring Bean 技能和 agentskills.io 标准格式。

## 📚 文档导航

**Languages**: [中文](README.md) | [English](README_EN.md)

- **[Tools 集成指南](docs/use/tool-integration.md)** - 通过 Function Calling 集成技能
- **[Skill Prompt 集成指南](docs/use/skill-prompt-integration.md)** - 通过渐进式披露植入提示词

## 框架特性

- **多格式支持**: 同时支持 Spring Bean 技能和 agentskills.io 标准格式
- **模块化技能管理**: 通过统一的 `AgentSkill` 接口管理各种技能
- **Spring AI 集成**: 无缝集成到 Spring AI 框架，支持函数调用
- **自动配置**: 支持 Spring Boot 自动配置和属性配置
- **热重载**: 支持技能的热重载和动态加载/卸载
- **渐进式披露**: 按照 agentskills.io 规范实现高效的上下文管理
- **迁移工具**: 提供技能格式转换和迁移工具
- **事件监听**: 提供技能执行事件的监听机制
- **扩展性**: 易于添加新技能和自定义实现

## 核心组件

### 1. AgentSkill 接口
所有技能的基础接口，定义了技能的核心方法：
- `getName()`: 获取技能名称
- `getDescription()`: 获取技能描述  
- `canHandle()`: 判断是否能处理特定请求
- `execute()`: 执行技能逻辑
- `getRequiredParameters()` / `getOptionalParameters()`: 定义参数
- `getInstructions()`: 获取 SKILL.md 指令内容（agentskills.io 格式）

### 2. AgentSkillManager
技能管理服务，负责：
- 注册和管理技能
- 查找合适的技能处理请求
- 执行技能并返回结果
- 提供事件监听机制

### 3. SpringAIAgentSkillAdapter
Spring AI 集成适配器，提供：
- 将 AgentSkill 转换为 Spring AI 函数
- 生成函数定义和参数 schema
- 获取所有指令内容用于系统提示增强

### 4. FolderBasedSkillLoader
文件夹技能加载器，支持：
- 从目录加载技能
- 支持多种技能格式（Spring Bean、JAR、脚本）
- 动态类加载和管理

### 5. agentskills.io 支持

#### SkillMarkdownParser
- 解析 SKILL.md 文件中的 YAML Frontmatter
- 提取元数据和指令内容
- 验证名称和描述格式

#### SkillDescriptor (增强版)
- 支持 agentskills.io 规范的所有字段
- 兼容传统的 skill.json/yaml 格式
- 验证技能名称格式（小写字母、数字、连字符）

#### AgentSkillManager (agentskills.io 专用)
- 技能验证和元数据管理
- 按关键词搜索技能
- 生成技能文档

### 6. SkillLifecycleManager
技能生命周期管理，提供：
- 动态加载/卸载技能
- 文件监控和热重载
- 批量技能操作

### 7. 配置组件
- `AgentSkillAutoConfiguration`: 自动配置类
- `AgentSkillProperties`: 配置属性
- `AgentSkillConfiguration`: 配置类

### 8. 迁移工具
- `SkillMigrationUtils`: 技能格式转换
- 从 Spring Bean 迁移到文件夹格式
- 生成技能模板和结构

## 支持的技能格式

### 传统 Spring Bean 技能
```java
@Component
public class MySkill implements AgentSkill {
    @Override
    public String getName() { return "my-skill"; }
    // ... 其他方法实现
}
```

### agentskills.io 标准格式
```
skill-name/
├── SKILL.md          # 必需：包含 YAML Frontmatter 和 Markdown 指令
├── scripts/          # 可选：可执行脚本
├── references/       # 可选：文档和参考资料
└── assets/           # 可选：模板、图片等资源
```

#### SKILL.md 示例结构
```yaml
---
name: pdf-processing
description: Extract text and tables from PDF files...
license: Apache-2.0
metadata:
  author: agent-skill-team
  version: "1.0"
---

# PDF Processing Skill

## When to use this skill
Use this skill when...

## How to extract text
1. **Input**: Provide the PDF file path...
```

## 使用方法

### 1. 添加依赖

在 `pom.xml` 中添加框架依赖：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
</dependency>
```

### 2. 配置属性

在 `application.yml` 中配置：

```yaml
agent:
  skill:
    enabled: true
    auto-register: true
    spring-ai-integration: true
    
    # 文件夹技能支持
    folder-based-skills: true
    skills-directory: "skills"
    hot-reload-enabled: true
    auto-load-skills: true
    
    # agentskills.io 支持
    agentskills-enabled: true
    strict-validation: true
    progressive-disclosure: true
    max-skill-md-size-kb: 50
```

### 3. 创建技能

#### Spring Bean 技能
```java
@Component
public class MyCustomSkill implements AgentSkill {
    
    @Override
    public String getName() {
        return "my-custom-skill";
    }
    
    @Override
    public String getDescription() {
        return "Custom skill for specific task";
    }
    
    @Override
    public boolean canHandle(String request) {
        return request.toLowerCase().contains("my task");
    }
    
    @Override
    public AgentSkillResult execute(String request, Map<String, Object> parameters) {
        // 实现技能逻辑
        return AgentSkillResult.success()
                .message("Task completed successfully")
                .data(result)
                .skillName(getName())
                .build();
    }
    
    // 其他方法...
}
```

#### agentskills.io 技能
创建 `skills/my-skill/SKILL.md`：

```yaml
---
name: my-skill
description: Custom skill for specific task processing
license: MIT
metadata:
  author: your-name
  version: "1.0"
---

# My Custom Skill

## When to use this skill
Use this skill when you need to...

## How to process requests
1. **Step one**: First processing step
2. **Step two**: Second processing step

### Parameters
- `input_data` (required): The data to process
- `mode` (optional): Processing mode - "fast" or "thorough"

## Scripts Reference

### `scripts/processor.py`
Main processing script with...

## Error Handling

Common errors and solutions...
```

### 4. 使用技能管理器

```java
@Service
public class MyService {
    
    @Autowired
    private AgentSkillManager skillManager;
    
    public void processRequest(String request) {
        // 自动查找合适的技能
        AgentSkillResult result = skillManager.executeSkill(request, Map.of());
        
        if (result.isSuccess()) {
            // 处理成功结果
        } else {
            // 处理失败情况
        }
    }
}
```

### 5. Spring AI 集成

```java
@RestController
public class SkillController {
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    public String getSystemInstructions() {
        // 获取所有技能的指令用于系统提示
        return skillAdapter.getAllInstructions();
    }
    
    public Object executeSkillFunction(String functionName, Map<String, Object> arguments) {
        // 执行技能函数
        return skillAdapter.executeFunction(functionName, arguments);
    }
}
```

## 项目结构

```
src/main/java/org/unreal/agent/skill/
├── AgentSkill.java                    # 核心接口
├── AgentSkillResult.java              # 结果类
├── AgentSkillManager.java             # 技能管理服务
├── springai/
│   └── SpringAIAgentSkillAdapter.java  # Spring AI 适配器
├── folder/
│   ├── SkillDescriptor.java         # 技能描述符
│   ├── SkillMarkdownParser.java      # agentskills.io 解析器
│   ├── FolderBasedSkillLoader.java  # 文件夹技能加载器
│   ├── MarkdownAgentSkill.java      # Markdown 指令技能
│   ├── SkillLifecycleManager.java   # 生命周期管理
│   └── AgentSkillManager.java       # agentskills.io 专用管理器
├── example/
│   ├── TextAnalysisSkill.java        # 示例技能
│   └── DateTimeSkill.java           # 示例技能
├── config/
│   ├── AgentSkillAutoConfiguration.java # 自动配置
│   ├── AgentSkillProperties.java     # 配置属性
│   └── AgentSkillConfiguration.java    # 配置类
└── migration/
    └── SkillMigrationUtils.java        # 迁移工具
```

## 技能目录结构示例

```
skills/
├── pdf-processing/
│   ├── SKILL.md                   # agentskills.io 格式技能
│   ├── scripts/                    # 处理脚本
│   │   ├── extract-pdf.py
│   │   └── fill-form.py
│   ├── references/                 # 参考文档
│   │   ├── REFERENCE.md
│   │   └── FORMS.md
│   └── assets/                     # 资源文件
│       ├── templates/
│       └── icons/
├── code-review/
│   └── SKILL.md
├── data-analysis/
│   └── SKILL.md
└── email-automation/
    └── SKILL.md
```

## 验证和迁移

### 验证技能
```java
@Autowired
private AgentSkillManager agentskillsManager;

public void validateSkill(Path skillPath) {
    AgentSkillManager.ValidationResult result = 
        agentskillsManager.validateSkill(skillPath);
    
    if (!result.isValid()) {
        System.out.println("Validation errors:");
        result.getErrors().forEach(System.out::println);
    }
}
```

### 迁移技能
```java
@Autowired
private SkillMigrationUtils migrationUtils;

public void migrateSpringBeanSkill(AgentSkill skill, Path outputDir) {
    Path migratedSkill = migrationUtils.migrateSkillToFolder(skill, outputDir);
    System.out.println("Migrated skill to: " + migratedSkill);
}
```

## 高级特性

### 渐进式披露
按照 agentskills.io 规范实现三层内容管理：
1. **发现阶段**：仅加载名称和描述（~100 tokens）
2. **激活阶段**：加载完整 SKILL.md 指令（<5000 tokens）
3. **执行阶段**：按需加载脚本、参考资料和资源

### 性能优化
- 技能元数据缓存
- 延迟加载大型技能内容
- 文件大小验证
- 并发执行控制

### 监控和分析
- 技能执行指标
- 错误率和响应时间
- 使用统计和分析

## REST API 集成

框架提供 REST API 端点供第三方 Spring AI 服务集成：

| 端点 | 描述 |
|------|------|
| `GET /api/agent-skills/discovery` | 获取技能发现信息（轻量级） |
| `GET /api/agent-skills/all` | 获取所有技能信息 |
| `POST /api/agent-skills/execute/{skillName}` | 执行技能 |
| `GET /api/agent-skills/spring-ai-functions` | 获取 Spring AI 函数定义 |
| `GET /api/agent-skills/names` | 获取所有技能名称 |
| `GET /api/agent-skills/{skillName}` | 获取特定技能详情 |

### 与第三方 Spring AI 服务集成

#### 使用 Spring AI 适配器

```java
@Autowired
private SpringAIAgentSkillAdapter adapter;

// 获取函数定义
List<Map<String, Object>> functions = adapter.getFunctionDefinitions();

// 执行技能函数
Object result = adapter.executeFunction("datetime", parameters);

// 获取系统提示增强
String instructions = adapter.getAllInstructions();
```

#### 渐进式披露集成

```java
// 发现阶段：获取轻量级技能信息
List<String> discoveryInfo = adapter.getSkillDiscoveryInfo();

// 获取所有技能信息（按层组织）
Map<String, Object> allSkills = adapter.getAllSkillsForAgentskillsIo();
```

## 安全特性

- 技能验证和沙箱执行
- 依赖管理和安全存储
- 输入验证和清理
- 访问控制和审计日志

## 最佳实践

1. **渐进式披露**：使用三层模型最小化上下文开销
2. **验证**：实现 agentskills.io 合规性验证
3. **缓存**：启用元数据缓存提高性能
4. **热重载**：开发工作流中利用热重载
5. **参数 Schema**：为函数调用定义清晰的参数模式

## 核心组件索引

- `[AgentSkill](src/main/java/org/unreal/agent/skill/AgentSkill.java)` - 技能接口定义
- `[AgentSkillManager](src/main/java/org/unreal/agent/skill/AgentSkillManager.java)` - 技能管理
- `[SpringAIAgentSkillAdapter](src/main/java/org/unreal/agent/skill/springai/SpringAIAgentSkillAdapter.java)` - Spring AI 集成适配器
- `[SkillMarkdownParser](src/main/java/org/unreal/agent/skill/folder/SkillMarkdownParser.java)` - SKILL.md 解析器
- `[FolderBasedSkillLoader](src/main/java/org/unreal/agent/skill/folder/FolderBasedSkillLoader.java)` - 文件夹技能加载器
- `[SkillLifecycleManager](src/main/java/org/unreal/agent/skill/folder/SkillLifecycleManager.java)` - 生命周期管理

这个框架为 Spring AI 应用提供了一个完整、生产就绪的技能管理解决方案，同时保持与 agentskills.io 标准的完全兼容性。