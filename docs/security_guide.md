# Agent Skill Manager Framework 安全性文档

## 概述

本文档详细描述了 Agent Skill Manager Framework 的安全特性、安全措施以及如何安全地使用该框架。

## 安全威胁模型

### 1. 动态类加载风险

- **威胁**: 恶意技能可能包含有害代码，通过动态加载执行
- **缓解措施**: 实现 `SecureClassLoader` 限制类加载范围

### 2. 路径遍历攻击

- **威胁**: 恶意技能可能通过路径遍历访问系统敏感文件
- **缓解措施**: 实现路径验证和规范化

### 3. 输入验证不足

- **威胁**: 恶意输入可能导致注入攻击或其他安全问题
- **缓解措施**: 实现输入验证和清理机制

### 4. 资源耗尽攻击

- **威胁**: 恶意技能可能消耗过多系统资源
- **缓解措施**: 实现资源限制和监控

## 安全措施

### 1. 安全类加载器

#### SecureClassLoader

```java
public class SecureClassLoader extends URLClassLoader {
    private final Path allowedBasePath;
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 验证类名是否在允许的包列表中
        if (!SecurityUtils.isAllowedClassName(name)) {
            throw new SecurityException("Class loading denied: " + name + 
                ". This class is not in the allowed packages list.");
        }
        
        // 继续父类加载逻辑
        return super.loadClass(name, resolve);
    }
    
    private boolean isPathAllowed(URL url) {
        try {
            Path path = Path.of(url.toURI());
            // 验证路径是否在允许的基路径内
            return path.normalize().startsWith(allowedBasePath.normalize());
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### 允许的包列表

```java
private static final Set<String> ALLOWED_PACKAGES = new HashSet<>(Arrays.asList(
    "org.unreal.agent.skill.",
    "java.lang.",
    "java.util.",
    "java.math.",
    "java.time."
));
```

### 2. 输入验证

#### InputValidationUtils

```java
public class InputValidationUtils {
    private static final Pattern SKILL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("\\.\\.(/|\\\\)");
    private static final int MAX_INPUT_LENGTH = 10000; // 10KB limit
    
    public static boolean isValidSkillName(String skillName) {
        if (skillName == null) {
            return false;
        }
        
        return SKILL_NAME_PATTERN.matcher(skillName).matches();
    }
    
    public static boolean isValidFilePath(String path) {
        if (path == null) {
            return false;
        }
        
        // 检查路径遍历尝试
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isValidInputLength(String input) {
        if (input == null) {
            return true; // Null is considered valid (will be handled elsewhere)
        }
        
        return input.length() <= MAX_INPUT_LENGTH;
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // 移除潜在的路径遍历序列
        input = PATH_TRAVERSAL_PATTERN.matcher(input).replaceAll("");
        
        return input;
    }
}
```

### 3. 路径安全

#### 路径规范化和验证

```java
public class SecurityUtils {
    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        
        // 替换反斜杠为正斜杠以进行一致处理
        path = path.replace('\\', '/');
        
        // 将路径分解为组件
        String[] parts = path.split("/");
        java.util.Stack<String> stack = new java.util.Stack<>();
        
        for (String part : parts) {
            if (part.equals("..")) {
                if (!stack.isEmpty() && !stack.peek().equals("..")) {
                    stack.pop();
                } else {
                    stack.push(part);
                }
            } else if (!part.isEmpty() && !part.equals(".")) {
                stack.push(part);
            }
        }
        
        StringBuilder normalized = new StringBuilder();
        for (String part : stack) {
            normalized.append("/").append(part);
        }
        
        return normalized.length() > 0 ? normalized.substring(1) : "";
    }
}
```

### 4. 文件操作安全

#### 文件操作验证

```java
public class SkillManagementService {
    public String readFile(String skillName, String filePath) throws IOException {
        // 驗证输入
        if (!InputValidationUtils.isValidSkillName(skillName)) {
            throw new IllegalArgumentException("Invalid skill name: " + skillName);
        }
        
        if (!InputValidationUtils.isValidFilePath(filePath)) {
            throw new SecurityException("Invalid file path: " + filePath);
        }
        
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
        if (loadedSkill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillName);
        }

        Path skillFolder = loadedSkill.getSkillFolder();
        Path targetFile = skillFolder.resolve(filePath).normalize();

        // 安全检查：确保文件在技能文件夹内
        if (!targetFile.startsWith(skillFolder)) {
            throw new SecurityException("Invalid file path: " + filePath);
        }

        if (!Files.exists(targetFile) || Files.isDirectory(targetFile)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        return Files.readString(targetFile);
    }
}
```

## 安全配置

### 1. 配置验证

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

### 2. 安全配置验证器

```java
@Component
public class ConfigurationValidator {
    
    @Autowired
    private AgentSkillProperties properties;
    
    @Autowired
    private Validator validator;
    
    @PostConstruct
    public void validateConfiguration() {
        Set<ConstraintViolation<AgentSkillProperties>> violations = validator.validate(properties);
        
        if (!violations.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Configuration validation failed:\n");
            for (ConstraintViolation<AgentSkillProperties> violation : violations) {
                errorMsg.append("  - ")
                        .append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append(" (was: ")
                        .append(violation.getInvalidValue())
                        .append(")\n");
            }
            
            throw new IllegalStateException(errorMsg.toString());
        }
    }
}
```

## 安全最佳实践

### 1. 技能开发者的安全建议

- **最小权限原则**: 技能应只请求必要的权限和资源
- **输入验证**: 在技能内部也要验证所有输入
- **安全编码**: 避免使用危险的 API 和函数
- **错误处理**: 正确处理错误而不泄露敏感信息

### 2. 系统管理员的安全建议

- **定期更新**: 保持框架和依赖库的最新版本
- **监控**: 监控技能执行和资源使用情况
- **备份**: 定期备份重要配置和技能
- **访问控制**: 限制对技能目录和配置的访问

### 3. 安全审计

- **代码审查**: 定期审查技能代码的安全性
- **渗透测试**: 对系统进行安全测试
- **日志分析**: 分析安全相关日志
- **合规性检查**: 确保符合安全标准

## 安全事件响应

### 1. 安全漏洞报告

如果发现安全漏洞，请通过以下方式报告：
- 邮箱: security@agentskillmanager.org
- 请提供详细的漏洞描述和复现步骤

### 2. 应急响应流程

1. **识别**: 识别安全事件的性质和范围
2. **遏制**: 采取措施防止事件扩大
3. **根因分析**: 分析事件的根本原因
4. **修复**: 实施永久性修复措施
5. **恢复**: 恢复系统正常运行
6. **总结**: 总结经验教训并改进安全措施

## 安全功能开关

### 1. 安全功能配置

```yaml
agent:
  skill:
    # 安全相关配置
    security:
      # 启用输入验证
      input-validation: true
      # 启用路径验证
      path-validation: true
      # 启用类加载限制
      class-loading-restriction: true
      # 最大文件大小限制（KB）
      max-file-size-kb: 2048
      # 最大技能名称长度
      max-skill-name-length: 64
```

### 2. 安全监控

- **性能监控**: 监控技能执行时间和资源使用
- **安全监控**: 监控安全事件和异常
- **审计日志**: 记录所有安全相关操作

## 总结

Agent Skill Manager Framework 通过多层次的安全措施保护系统免受各种安全威胁。开发者和管理员应遵循安全最佳实践，定期进行安全审计，以确保系统的持续安全。