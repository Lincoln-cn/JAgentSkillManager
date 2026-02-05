# Tools（函数调用）集成指南

本文档介绍第三方 Spring AI 服务如何通过 Function Calling（函数调用）机制集成 Agent Skill Manager 的 tools 能力。

## 目录

1. [核心概念](#核心概念)
2. [架构设计](#架构设计)
3. [集成步骤](#集成步骤)
4. [完整代码示例](#完整代码示例)
5. [高级配置](#高级配置)
6. [最佳实践](#最佳实践)
7. [常见问题](#常见问题)

---

## 核心概念

### 什么是 Function Calling？

Function Calling（函数调用）是现代大模型（GPT-4、Claude 等）支持的能力：

- **模型不直接执行**：LLM 只决定调用哪个函数，并生成参数
- **外部执行**：实际执行由您的服务完成
- **结果回传**：执行结果返回给 LLM，LLM 生成自然语言回复

### Tools 集成的工作流程

```
用户输入 → LLM 推理 → 识别需要 Tool → 生成函数调用 → 
外部执行 Tool → 返回结果 → LLM 生成回复 → 返回用户
```

---

## 架构设计

### 组件关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                    第三方 Spring AI 服务                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Skill Manager Framework                      │  │
│  │  ┌──────────────────┐    ┌──────────────────┐           │  │
│  │  │ AgentSkillManager │───▶│ SpringAIAdapter  │           │  │
│  │  └──────────────────┘    └────────┬─────────┘           │  │
│  │                                    │                      │  │
│  │  ┌──────────────────┐    ┌────────▼─────────┐           │  │
│  │  │ Registered Skills│    │ Function Wrapper │           │  │
│  │  └──────────────────┘    └──────────────────┘           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
└──────────────────────────────┼───────────────────────────────────┘
                               │
                        Function Definitions
                               │
┌──────────────────────────────▼───────────────────────────────────┐
│                          LLM (OpenAI/Claude)                     │
│                     ┌─────────────────────┐                      │
│                     │   Function Calling  │                      │
│                     └──────────┬──────────┘                      │
└────────────────────────────────┼──────────────────────────────────┘
                                 │ Function Call Request
                                 ▼
┌──────────────────────────────────────────────────────────────────┐
│                    您的 Chat Service                              │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  1. 接收函数调用请求                                         │  │
│  │  2. 调用 adapter.executeFunction()                           │  │
│  │  3. 获取执行结果                                             │  │
│  │  4. 将结果返回给 LLM                                         │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

### 数据流详解

```
1. 初始化阶段
   └── 注册 Skills → 生成 Function Definitions → 缓存

2. 请求阶段
   └── 用户输入 + Function Definitions → LLM

3. 推理阶段
   └── LLM 分析输入 → 匹配 Function Description → 
       决定是否调用 → 生成调用参数

4. 执行阶段
   └── 解析函数名 → 查找 Skill → 执行 skill.execute() → 
       返回 AgentSkillResult

5. 响应阶段
   └── 执行结果 → LLM → 生成自然语言 → 返回用户
```

---

## 集成步骤

### 步骤 1：添加依赖

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-core</artifactId>
    </dependency>
    
    <!-- OpenAI Starter (或其他提供商) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Agent Skill Manager -->
    <dependency>
        <groupId>org.unreal</groupId>
        <artifactId>agent-skill-manager</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 步骤 2：配置 Skill Manager

```yaml
# application.yml
agent:
  skill:
    enabled: true
    spring-ai-integration: true
    
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
```

### 步骤 3：创建 Skills（可选）

如果您需要自定义 skills：

```java
@Component
public class WeatherSkill implements AgentSkill {
    
    @Override
    public String getName() {
        return "weather-query";
    }
    
    @Override
    public String getDescription() {
        // 关键：这个描述决定 LLM 何时调用此技能
        return "Get weather information for any location. " +
               "Use when user asks about weather, temperature, forecast, " +
               "or mentions cities with weather-related questions.";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean canHandle(String request) {
        String lower = request.toLowerCase();
        return lower.contains("weather") || 
               lower.contains("temperature") ||
               lower.contains("forecast");
    }
    
    @Override
    public AgentSkillResult execute(String request, Map<String, Object> parameters) {
        String location = (String) parameters.get("location");
        
        // 调用天气 API
        WeatherData data = weatherApi.getWeather(location);
        
        return AgentSkillResult.success()
            .message("Weather retrieved successfully")
            .data(data)
            .skillName(getName())
            .build();
    }
    
    @Override
    public Map<String, String> getRequiredParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("location", "City name or coordinates");
        return params;
    }
    
    @Override
    public Map<String, String> getOptionalParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("unit", "Temperature unit: celsius or fahrenheit");
        return params;
    }
}
```

### 步骤 4：集成到 Chat Service

```java
@Service
public class SkillAwareChatService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    /**
     * 主要聊天方法 - 自动使用 Function Calling
     */
    public String chat(String userMessage) {
        // 1. 获取所有可用函数的 JSON Schema 定义
        List<Map<String, Object>> functions = skillAdapter.getFunctionDefinitions();
        
        // 2. 配置 ChatClient 使用这些函数
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
            .user(userMessage)
            .functions(functions)  // 关键：传入函数定义
            .call();
        
        // 3. Spring AI 自动处理 Function Calling
        //    - 如果 LLM 决定调用函数，会自动执行并返回结果
        //    - 如果不需要调用，直接返回文本回复
        return responseSpec.content();
    }
}
```

### 步骤 5：手动处理 Function Calling（高级）

如果您需要更细粒度的控制：

```java
@Service
public class AdvancedChatService {
    
    @Autowired
    private OpenAiChatModel chatModel;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    public String chatWithManualFunctionCalling(String userMessage) {
        // 1. 获取函数定义
        List<FunctionCallback> callbacks = skillAdapter.getFunctionCallbacks().values()
            .stream()
            .collect(Collectors.toList());
        
        // 2. 创建提示词
        Prompt prompt = new Prompt(
            new UserMessage(userMessage),
            OpenAiChatOptions.builder()
                .withFunctions(callbacks.stream()
                    .map(FunctionCallback::getName)
                    .collect(Collectors.toList()))
                .build()
        );
        
        // 3. 第一次调用 LLM
        ChatResponse response = chatModel.call(prompt);
        
        // 4. 检查是否有函数调用
        if (response.getResult().getOutput() instanceof AssistantMessage) {
            AssistantMessage assistantMessage = 
                (AssistantMessage) response.getResult().getOutput();
            
            // 检查工具调用
            List<ToolCall> toolCalls = assistantMessage.getToolCalls();
            
            if (toolCalls != null && !toolCalls.isEmpty()) {
                // 5. 执行工具调用
                List<Message> messages = new ArrayList<>();
                messages.add(new UserMessage(userMessage));
                messages.add(assistantMessage);
                
                for (ToolCall toolCall : toolCalls) {
                    String functionName = toolCall.name();
                    String arguments = toolCall.arguments();
                    
                    // 解析参数
                    Map<String, Object> params = parseArguments(arguments);
                    
                    // 执行 skill
                    Object result = skillAdapter.executeFunction(functionName, params);
                    
                    // 添加工具响应到对话历史
                    messages.add(new ToolResponseMessage(
                        result.toString(),
                        functionName,
                        toolCall.id()
                    ));
                }
                
                // 6. 第二次调用 LLM，传入工具执行结果
                Prompt secondPrompt = new Prompt(messages);
                ChatResponse finalResponse = chatModel.call(secondPrompt);
                
                return finalResponse.getResult().getOutput().getContent();
            }
        }
        
        // 没有函数调用，直接返回结果
        return response.getResult().getOutput().getContent();
    }
    
    private Map<String, Object> parseArguments(String arguments) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(arguments, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
```

---

## 完整代码示例

### 示例 1：完整的 Chat Controller

```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private SkillAwareChatService chatService;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    /**
     * 标准聊天端点 - 自动使用 Skills
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String response = chatService.chat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response));
    }
    
    /**
     * 获取可用函数定义 - 供前端或调试使用
     */
    @GetMapping("/functions")
    public ResponseEntity<List<Map<String, Object>>> getAvailableFunctions() {
        return ResponseEntity.ok(skillAdapter.getFunctionDefinitions());
    }
    
    /**
     * 直接执行特定 skill
     */
    @PostMapping("/execute/{skillName}")
    public ResponseEntity<Object> executeSkill(
            @PathVariable String skillName,
            @RequestBody Map<String, Object> parameters) {
        try {
            Object result = skillAdapter.executeFunction(skillName, parameters);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

// DTOs
@Data
public class ChatRequest {
    private String message;
}

@Data
@AllArgsConstructor
public class ChatResponse {
    private String content;
}
```

### 示例 2：带对话历史的 Chat Service

```java
@Service
public class StatefulChatService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    // 存储对话历史（生产环境应使用 Redis 等）
    private Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
    
    public String chat(String sessionId, String userMessage) {
        // 1. 获取或创建对话历史
        List<Message> history = conversationHistory.computeIfAbsent(
            sessionId, k -> new ArrayList<>()
        );
        
        // 2. 添加用户消息
        history.add(new UserMessage(userMessage));
        
        // 3. 获取函数定义
        var functions = skillAdapter.getFunctionDefinitions();
        
        // 4. 调用 LLM
        String response = chatClient.prompt()
            .messages(history)
            .functions(functions)
            .call()
            .content();
        
        // 5. 添加助手回复到历史
        history.add(new AssistantMessage(response));
        
        // 6. 限制历史长度（防止超出上下文限制）
        if (history.size() > 20) {
            history = history.subList(history.size() - 20, history.size());
            conversationHistory.put(sessionId, history);
        }
        
        return response;
    }
    
    public void clearHistory(String sessionId) {
        conversationHistory.remove(sessionId);
    }
}
```

---

## 高级配置

### 配置 1：函数调用选项

```java
@Configuration
public class FunctionCallingConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, 
                                  SpringAIAgentSkillAdapter adapter) {
        return builder
            .defaultOptions(OpenAiChatOptions.builder()
                // 强制使用函数调用（如果不使用会报错）
                .withToolChoice("auto")  // "auto", "none", 或指定 {"type": "function", "function": {"name": "my_function"}}
                
                // 限制并行函数调用数量
                .withParallelToolCalls(true)
                
                // 设置 temperature
                .withTemperature(0.7)
                .build())
            .build();
    }
}
```

### 配置 2：动态函数注册

```java
@Service
public class DynamicFunctionRegistrationService {
    
    @Autowired
    private SpringAIAgentSkillAdapter adapter;
    
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    
    private ChatClient dynamicChatClient;
    
    @PostConstruct
    public void init() {
        refreshFunctions();
    }
    
    /**
     * 当 skills 变化时刷新函数列表
     */
    public void refreshFunctions() {
        var functions = adapter.getFunctionDefinitions();
        
        this.dynamicChatClient = chatClientBuilder
            .defaultFunctions(functions)
            .build();
    }
    
    public String chat(String message) {
        return dynamicChatClient.prompt()
            .user(message)
            .call()
            .content();
    }
}
```

---

## 最佳实践

### 1. 写好 Function Description

```java
// ❌ 不好的描述
public String getDescription() {
    return "Weather skill";
}

// ✅ 好的描述
public String getDescription() {
    return "Retrieves current weather conditions, temperature, humidity, " +
           "and forecasts for any location worldwide. " +
           "Use this function when the user asks about: " +
           "- Current weather in a specific city " +
           "- Temperature or forecasts " +
           "- Weather conditions for travel planning " +
           "- Any weather-related queries with location mentions.";
}
```

### 2. 参数设计清晰

```java
@Override
public Map<String, String> getRequiredParameters() {
    Map<String, String> params = new HashMap<>();
    
    // 参数名要清晰
    params.put("location", 
        "Required. City name (e.g., 'Beijing', 'New York') or " +
        "coordinates (e.g., '39.9042,116.4074'). " +
        "Must be a valid location string.");
    
    return params;
}

@Override
public Map<String, String> getOptionalParameters() {
    Map<String, String> params = new HashMap<>();
    
    // 提供明确的选项
    params.put("unit", 
        "Optional. Temperature unit: 'celsius' (default) or 'fahrenheit'. " +
        "Use 'fahrenheit' for US locations if not specified.");
    
    params.put("days", 
        "Optional. Number of forecast days (1-7). " +
        "Default is 1 (current day only).");
    
    return params;
}
```

### 3. 错误处理

```java
@Override
public AgentSkillResult execute(String request, Map<String, Object> parameters) {
    try {
        String location = (String) parameters.get("location");
        
        // 参数验证
        if (location == null || location.trim().isEmpty()) {
            return AgentSkillResult.failure()
                .message("Location parameter is required but was not provided.")
                .skillName(getName())
                .build();
        }
        
        // 执行逻辑
        WeatherData data = fetchWeather(location);
        
        return AgentSkillResult.success()
            .message(String.format("Weather for %s retrieved successfully", location))
            .data(data)
            .skillName(getName())
            .build();
            
    } catch (WeatherApiException e) {
        return AgentSkillResult.failure()
            .message("Failed to fetch weather: " + e.getMessage())
            .skillName(getName())
            .metadata(Map.of("error_type", "api_error"))
            .build();
    } catch (Exception e) {
        return AgentSkillResult.failure()
            .message("Unexpected error occurred")
            .skillName(getName())
            .metadata(Map.of("error", e.getClass().getSimpleName()))
            .build();
    }
}
```

### 4. 监控和日志

```java
@Component
public class FunctionExecutionLogger implements AgentSkillManager.SkillExecutionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(FunctionExecutionLogger.class);
    
    @Override
    public void onExecutionStarted(AgentSkill skill, String request, Map<String, Object> parameters) {
        logger.info("Function calling started: skill={}, request={}", 
            skill.getName(), request);
    }
    
    @Override
    public void onExecutionCompleted(AgentSkill skill, String request, 
                                     Map<String, Object> parameters, AgentSkillResult result) {
        logger.info("Function calling completed: skill={}, success={}", 
            skill.getName(), result.isSuccess());
    }
    
    @Override
    public void onExecutionFailed(AgentSkill skill, String request, 
                                  Map<String, Object> parameters, Exception error) {
        logger.error("Function calling failed: skill={}, error={}", 
            skill.getName(), error.getMessage());
    }
}
```

---

## 常见问题

### Q1: LLM 不调用我的函数怎么办？

**检查点：**
1. **描述是否清晰**：确保描述包含功能和使用场景
2. **关键词匹配**：描述中应包含用户可能使用的关键词
3. **参数是否明确**：required/optional 参数要定义清楚
4. **temperature 设置**：过高的 temperature 可能导致不稳定性

**调试方法：**
```java
// 打印函数定义，检查是否正确
System.out.println(adapter.getFunctionDefinitions());
```

### Q2: 函数被调用了但参数不正确？

**解决方案：**
1. 在参数描述中提供示例
2. 在 execute 方法中添加参数验证
3. 返回清晰的错误信息，让 LLM 知道如何修正

### Q3: 如何处理多个并行函数调用？

Spring AI 会自动处理并行调用。如果手动实现：

```java
List<CompletableFuture<Object>> futures = toolCalls.stream()
    .map(toolCall -> CompletableFuture.supplyAsync(() -> {
        return skillAdapter.executeFunction(toolCall.name(), 
            parseArguments(toolCall.arguments()));
    }))
    .collect(Collectors.toList());

// 等待所有执行完成
List<Object> results = futures.stream()
    .map(CompletableFuture::join)
    .collect(Collectors.toList());
```

### Q4: 如何限制特定用户的函数访问？

```java
@Service
public class SecureChatService {
    
    @Autowired
    private SpringAIAgentSkillAdapter adapter;
    
    public String chat(User user, String message) {
        // 根据用户角色过滤可用函数
        List<Map<String, Object>> allowedFunctions = adapter.getFunctionDefinitions()
            .stream()
            .filter(func -> isFunctionAllowed(user, (String) func.get("name")))
            .collect(Collectors.toList());
        
        return chatClient.prompt()
            .user(message)
            .functions(allowedFunctions)  // 只传入允许的函数
            .call()
            .content();
    }
    
    private boolean isFunctionAllowed(User user, String functionName) {
        // 实现权限检查逻辑
        if (functionName.equals("admin-function")) {
            return user.hasRole("ADMIN");
        }
        return true;
    }
}
```

---

## 总结

Tools（函数调用）集成是最直接的 Skill 使用方式：

1. **自动触发**：LLM 根据描述自动决定是否调用
2. **结构清晰**：每个 Skill 都是一个独立的函数
3. **易于调试**：可以单独测试每个 Skill 的执行
4. **生产就绪**：Spring AI 处理了大部分复杂性

关键成功因素：
- ✅ 编写清晰、详细的函数描述
- ✅ 设计合理的参数结构
- ✅ 完善的错误处理和验证
- ✅ 适当的监控和日志记录

---

**下一步**：了解如何通过 [Skill Prompt 渐进式披露](skill-prompt-integration.md) 为 LLM 提供更详细的指导。