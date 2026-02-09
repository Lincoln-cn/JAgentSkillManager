# Tools (Function Calling) Integration Guide

**Languages**: [中文](tool-integration.md) | [English](tool-integration_EN.md)

This document describes how third-party Spring AI services can integrate the tools capability of Agent Skill Manager through the Function Calling mechanism.

## Table of Contents

1. [Core Concepts](#core-concepts)
2. [Architectural Design](#architectural-design)
3. [Integration Steps](#integration-steps)
4. [Full Code Example](#full-code-example)
5. [Advanced Configuration](#advanced-configuration)
6. [Best Practices](#best-practices)
7. [FAQ](#faq)

---

## Core Concepts

### What is Function Calling?

Function Calling is a capability supported by modern large models (GPT-4, Claude, etc.):

- **Model Doesn't Execute Directly**: The LLM only decides which function to call and generates the parameters.
- **External Execution**: The actual execution is performed by your service.
- **Result Feedback**: The execution result is returned to the LLM, which then generates a natural language response.

### Tools Integration Workflow

```
User Input → LLM Reasoning → Identify Tool Needed → Generate Function Call → 
External Execution of Tool → Return Result → LLM Generates Response → Return to User
```

---

## Architectural Design

### Component Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Third-party Spring AI Service                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Skill Manager Framework                      │  │
│  │  ┌──────────────────┐    ┌──────────────────┐           │  │
│  │  │ AgentSkillManager │───▶│ SpringAIAdapter  │           │  │
│  │  └──────────────────┘    └────────┬─────────┘           │  │
│  │                                    │                      │  │
│  │  ┌──────────────────┐    ┌────────▼─────────┐           │  │
│  │  │ Registered Skills│    │ Function Wrapper │           │  │
│  │  └──────────────────┘    └──────────────────┘           │  │
│  │  └──────────────────────────────────────────────────────────┘  │
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
│                    Your Chat Service                             │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  1. Receive Function Call Request                          │  │
│  │  2. Call adapter.executeFunction()                         │  │
│  │  3. Get Execution Result                                   │  │
│  │  4. Return result to LLM                                   │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

### Data Flow Detail

```
1. Initialization Phase
   └── Register Skills → Generate Function Definitions → Cache

2. Request Phase
   └── User Input + Function Definitions → LLM

3. Reasoning Phase
   └── LLM analyzes input → Matches Function Description → 
       Decides whether to call → Generates call parameters

4. Execution Phase
   └── Parse function name → Find Skill → Execute skill.execute() → 
       Return AgentSkillResult

5. Response Phase
   └── Execution Result → LLM → Generate Natural Language → Return to User
```

---

## Integration Steps

### Step 1: Add Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-core</artifactId>
    </dependency>
    
    <!-- OpenAI Starter (or other providers) -->
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

### Step 2: Configure Skill Manager

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

### Step 3: Create Skills (Optional)

If you need custom skills:

```java
@Component
public class WeatherSkill implements AgentSkill {
    
    @Override
    public String getName() {
        return "weather-query";
    }
    
    @Override
    public String getDescription() {
        // Critical: This description determines when LLM calls this skill
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
        
        // Call Weather API
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

### Step 4: Integrate into Chat Service

```java
@Service
public class SkillAwareChatService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    /**
     * Main chat method - automatically use Function Calling
     */
    public String chat(String userMessage) {
        // 1. Get JSON Schema definitions for all available functions
        List<Map<String, Object>> functions = skillAdapter.getFunctionDefinitions();
        
        // 2. Configure ChatClient to use these functions
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
            .user(userMessage)
            .functions(functions)  // Critical: pass function definitions
            .call();
        
        // 3. Spring AI automatically handles Function Calling
        //    - If LLM decides to call a function, it executes automatically and returns result
        //    - If no call needed, returns text response directly
        return responseSpec.content();
    }
}
```

### Step 5: Manual Function Calling Handling (Advanced)

If you need more granular control:

```java
@Service
public class AdvancedChatService {
    
    @Autowired
    private OpenAiChatModel chatModel;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    public String chatWithManualFunctionCalling(String userMessage) {
        // 1. Get function definitions
        List<FunctionCallback> callbacks = skillAdapter.getFunctionCallbacks().values()
            .stream()
            .collect(Collectors.toList());
        
        // 2. Create prompt
        Prompt prompt = new Prompt(
            new UserMessage(userMessage),
            OpenAiChatOptions.builder()
                .withFunctions(callbacks.stream()
                    .map(FunctionCallback::getName)
                    .collect(Collectors.toList()))
                .build()
        );
        
        // 3. First call to LLM
        ChatResponse response = chatModel.call(prompt);
        
        // 4. Check for function calls
        if (response.getResult().getOutput() instanceof AssistantMessage) {
            AssistantMessage assistantMessage = 
                (AssistantMessage) response.getResult().getOutput();
            
            // Check tool calls
            List<ToolCall> toolCalls = assistantMessage.getToolCalls();
            
            if (toolCalls != null && !toolCalls.isEmpty()) {
                // 5. Execute tool calls
                List<Message> messages = new ArrayList<>();
                messages.add(new UserMessage(userMessage));
                messages.add(assistantMessage);
                
                for (ToolCall toolCall : toolCalls) {
                    String functionName = toolCall.name();
                    String arguments = toolCall.arguments();
                    
                    // Parse arguments
                    Map<String, Object> params = parseArguments(arguments);
                    
                    // Execute skill
                    Object result = skillAdapter.executeFunction(functionName, params);
                    
                    // Add tool response to conversation history
                    messages.add(new ToolResponseMessage(
                        result.toString(),
                        functionName,
                        toolCall.id()
                    ));
                }
                
                // 6. Second call to LLM with tool execution results
                Prompt secondPrompt = new Prompt(messages);
                ChatResponse finalResponse = chatModel.call(secondPrompt);
                
                return finalResponse.getResult().getOutput().getContent();
            }
        }
        
        // No function call, return result directly
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

## Full Code Example

### Example 1: Full Chat Controller

```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private SkillAwareChatService chatService;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    /**
     * Standard chat endpoint - automatically uses Skills
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String response = chatService.chat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response));
    }
    
    /**
     * Get available function definitions - for frontend or debugging
     */
    @GetMapping("/functions")
    public ResponseEntity<List<Map<String, Object>>> getAvailableFunctions() {
        return ResponseEntity.ok(skillAdapter.getFunctionDefinitions());
    }
    
    /**
     * Directly execute a specific skill
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

### Example 2: Chat Service with Conversation History

```java
@Service
public class StatefulChatService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    // Store conversation history (use Redis in production)
    private Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
    
    public String chat(String sessionId, String userMessage) {
        // 1. Get or create conversation history
        List<Message> history = conversationHistory.computeIfAbsent(
            sessionId, k -> new ArrayList<>()
        );
        
        // 2. Add user message
        history.add(new UserMessage(userMessage));
        
        // 3. Get function definitions
        var functions = skillAdapter.getFunctionDefinitions();
        
        // 4. Call LLM
        String response = chatClient.prompt()
            .messages(history)
            .functions(functions)
            .call()
            .content();
        
        // 5. Add assistant response to history
        history.add(new AssistantMessage(response));
        
        // 6. Limit history length (prevent context limit exceed)
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

## Advanced Configuration

### Config 1: Function Calling Options

```java
@Configuration
public class FunctionCallingConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, 
                                  SpringAIAgentSkillAdapter adapter) {
        return builder
            .defaultOptions(OpenAiChatOptions.builder()
                // Force function calling (error if not used)
                .withToolChoice("auto")  // "auto", "none", or specific {"type": "function", "function": {"name": "my_function"}}
                
                // Limit parallel tool calls
                .withParallelToolCalls(true)
                
                // Set temperature
                .withTemperature(0.7)
                .build())
            .build();
    }
}
```

### Config 2: Dynamic Function Registration

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
     * Refresh function list when skills change
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

## Best Practices

### 1. Write Good Function Descriptions

```java
// ❌ Poor description
public String getDescription() {
    return "Weather skill";
}

// ✅ Good description
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

### 2. Clear Parameter Design

```java
@Override
public Map<String, String> getRequiredParameters() {
    Map<String, String> params = new HashMap<>();
    
    // Clear parameter names
    params.put("location", 
        "Required. City name (e.g., 'Beijing', 'New York') or " +
        "coordinates (e.g., '39.9042,116.4074'). " +
        "Must be a valid location string.");
    
    return params;
}

@Override
public Map<String, String> getOptionalParameters() {
    Map<String, String> params = new HashMap<>();
    
    // Provide explicit options
    params.put("unit", 
        "Optional. Temperature unit: 'celsius' (default) or 'fahrenheit'. " +
        "Use 'fahrenheit' for US locations if not specified.");
    
    params.put("days", 
        "Optional. Number of forecast days (1-7). " +
        "Default is 1 (current day only).");
    
    return params;
}
```

### 3. Error Handling

```java
@Override
public AgentSkillResult execute(String request, Map<String, Object> parameters) {
    try {
        String location = (String) parameters.get("location");
        
        // Parameter validation
        if (location == null || location.trim().isEmpty()) {
            return AgentSkillResult.failure()
                .message("Location parameter is required but was not provided.")
                .skillName(getName())
                .build();
        }
        
        // Execution logic
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

### 4. Monitoring and Logging

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

## FAQ

### Q1: What if LLM doesn't call my function?

**Checkpoints:**
1. **Clear Description**: Ensure description includes functionality and usage scenarios.
2. **Keyword Matching**: Description should contain keywords users might use.
3. **Explicit Parameters**: Define required/optional parameters clearly.
4. **Temperature Setting**: High temperature might cause instability.

**Debugging:**
```java
// Print function definitions to check if they are correct
System.out.println(adapter.getFunctionDefinitions());
```

### Q2: Function called but parameters are incorrect?

**Solutions:**
1. Provide examples in parameter descriptions.
2. Add parameter validation in `execute` method.
3. Return clear error messages so LLM knows how to correct.

### Q3: How to handle multiple parallel function calls?

Spring AI handles parallel calls automatically. If manually implementing:

```java
List<CompletableFuture<Object>> futures = toolCalls.stream()
    .map(toolCall -> CompletableFuture.supplyAsync(() -> {
        return skillAdapter.executeFunction(toolCall.name(), 
            parseArguments(toolCall.arguments()));
    }))
    .collect(Collectors.toList());

// Wait for all executions to complete
List<Object> results = futures.stream()
    .map(CompletableFuture::join)
    .collect(Collectors.toList());
```

### Q4: How to limit function access for specific users?

```java
@Service
public class SecureChatService {
    
    @Autowired
    private SpringAIAgentSkillAdapter adapter;
    
    public String chat(User user, String message) {
        // Filter available functions based on user role
        List<Map<String, Object>> allowedFunctions = adapter.getFunctionDefinitions()
            .stream()
            .filter(func -> isFunctionAllowed(user, (String) func.get("name")))
            .collect(Collectors.toList());
        
        return chatClient.prompt()
            .user(message)
            .functions(allowedFunctions)  // Pass only allowed functions
            .call()
            .content();
    }
    
    private boolean isFunctionAllowed(User user, String functionName) {
        // Implement permission check logic
        if (functionName.equals("admin-function")) {
            return user.hasRole("ADMIN");
        }
        return true;
    }
}
```

---

## Summary

Tools (Function Calling) integration is the most direct way to use Skills:

1. **Automatic Triggering**: LLM automatically decides whether to call based on description.
2. **Clear Structure**: Each Skill is an independent function.
3. **Easy Debugging**: Can test execution of each Skill individually.
4. **Production Ready**: Spring AI handles most of the complexity.

Key Success Factors:
- ✅ Write clear, detailed function descriptions
- ✅ Design reasonable parameter structures
- ✅ Robust error handling and validation
- ✅ Appropriate monitoring and logging

---

**Next Step**: Learn how to provide more detailed guidance for LLM through [Skill Prompt Progressive Disclosure](skill-prompt-integration_EN.md).
