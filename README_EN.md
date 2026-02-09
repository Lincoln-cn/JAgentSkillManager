# Agent Skill Manager Framework

<p align="center">
  <img src="logo/JAgentSkillsManagerLogo.png" alt="Agent Skill Manager Logo" width="400"/>
</p>

A comprehensive framework for managing and integrating agent skills into Spring AI applications. Supports both traditional Spring Bean skills and the agentskills.io specification format.

## 📚 Documentation Navigation

Language: [中文](README.md) | [English](README_EN.md)

Primary document index (for more details, visit `docs/README.md`):

- Documentation Index: [docs/README.md](docs/README.md)
- API Reference: [docs/api/API_DOCUMENTATION.md](docs/api/API_DOCUMENTATION.md)
- Guides: [docs/guides/use/tool-integration_EN.md](docs/guides/use/tool-integration_EN.md), [docs/guides/use/skill-prompt-integration_EN.md](docs/guides/use/skill-prompt-integration_EN.md)

For the complete documentation catalog, please visit `docs/README.md`.

## 🌟 Features

- **Multi-format Support**: Supports both Spring Bean skills and agentskills.io standard format
- **Modular Skill Management**: Manage all skills through the unified `AgentSkill` interface
- **Spring AI Integration**: Seamlessly integrate with Spring AI framework with function calling support
- **Auto-configuration**: Spring Boot auto-configuration and property-based configuration
- **Hot Reload**: Dynamic skill loading/unloading with hot reload capability
- **Progressive Disclosure**: Efficient context management following agentskills.io specification
- **Migration Tools**: Skill format conversion and migration utilities
- **Event Listeners**: Skill execution event listening mechanism
- **Extensibility**: Easy to add new skills and customize implementations

## 🏗️ Core Components

### 1. AgentSkill Interface
The foundation interface for all skills, defining core methods:
- `getName()`: Get skill name
- `getDescription()`: Get skill description  
- `canHandle()`: Determine if this skill can handle a specific request
- `execute()`: Execute skill logic
- `getRequiredParameters()` / `getOptionalParameters()`: Define parameters
- `getInstructions()`: Get SKILL.md instruction content (agentskills.io format)

### 2. AgentSkillManager
Skill management service responsible for:
- Registering and managing skills
- Finding appropriate skills to handle requests
- Executing skills and returning results
- Providing event listening mechanisms

### 3. SpringAIAgentSkillAdapter
Spring AI integration adapter providing:
- Converting AgentSkill to Spring AI functions
- Generating function definitions and parameter schemas
- Retrieving all instruction content for system prompt enhancement

### 4. FolderBasedSkillLoader
Folder-based skill loader supporting:
- Loading skills from directories
- Supporting multiple skill formats (Spring Bean, JAR, scripts)
- Dynamic class loading and management

### 5. agentskills.io Support

#### SkillMarkdownParser
- Parse YAML Frontmatter from SKILL.md files
- Extract metadata and instruction content
- Validate name and description formats

#### SkillDescriptor (Enhanced)
- Support all fields in agentskills.io specification
- Compatible with traditional skill.json/yaml formats
- Validate skill name format (lowercase letters, numbers, hyphens)

#### AgentSkillManager (agentskills.io specific)
- Skill validation and metadata management
- Search skills by keywords
- Generate skill documentation

### 6. SkillLifecycleManager
Skill lifecycle management providing:
- Dynamic skill loading/unloading
- File monitoring and hot reloading
- Batch skill operations

### 7. Configuration Components
- `AgentSkillAutoConfiguration`: Auto-configuration class
- `AgentSkillProperties`: Configuration properties
- `AgentSkillConfiguration`: Configuration class

### 8. Migration Tools
- `SkillMigrationUtils`: Skill format conversion
- Migration from Spring Bean to folder format
- Generate skill templates and structure

## 📋 Supported Skill Formats

### Traditional Spring Bean Skill
```java
@Component
public class MySkill implements AgentSkill {
    @Override
    public String getName() { return "my-skill"; }
    // ... other method implementations
}
```

### agentskills.io Standard Format
```
skill-name/
├── SKILL.md          # Required: Contains YAML Frontmatter and Markdown instructions
├── scripts/          # Optional: Executable scripts
├── references/       # Optional: Documentation and references
└── assets/           # Optional: Templates, images, and other resources
```

#### SKILL.md Example Structure
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

## 🚀 Quick Start

### 1. Add Dependencies (updated)

The project no longer depends on Spring AI directly. Keep the core runtime/parsing dependencies; example entries below — use versions from the project's `pom.xml`:

```xml
<!-- JSON/YAML parsing -->
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.15.2</version>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.dataformat</groupId>
  <artifactId>jackson-dataformat-yaml</artifactId>
  <version>2.15.2</version>
</dependency>

<!-- Logging (Logback) -->
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.4.11</version>
</dependency>

<!-- Remaining deps are in pom.xml -->
```

### 2. Configure Properties

Configure in `application.yml`:

```yaml
agent:
  skill:
    enabled: true
    auto-register: true
    spring-ai-integration: true
    
    # Folder-based skills support
    folder-based-skills: true
    skills-directory: "skills"
    hot-reload-enabled: true
    auto-load-skills: true
    
    # agentskills.io support
    agentskills-enabled: true
    strict-validation: true
    progressive-disclosure: true
    max-skill-md-size-kb: 50
```

### 3. Create Skills

#### Spring Bean Skill
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
        // Implement skill logic
        return AgentSkillResult.success()
                .message("Task completed successfully")
                .data(result)
                .skillName(getName())
                .build();
    }
    
    // Other methods...
}
```

#### agentskills.io Skill
Create `skills/my-skill/SKILL.md`:

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

### 4. Use Skill Manager

```java
@Service
public class MyService {
    
    @Autowired
    private AgentSkillManager skillManager;
    
    public void processRequest(String request) {
        // Automatically find appropriate skill
        AgentSkillResult result = skillManager.executeSkill(request, Map.of());
        
        if (result.isSuccess()) {
            // Handle successful result
        } else {
            // Handle failure case
        }
    }
}
```

### 5. Spring AI Integration

```java
@RestController
public class SkillController {
    
    @Autowired
    private SpringAIAgentSkillAdapter skillAdapter;
    
    public String getSystemInstructions() {
        // Get all skill instructions for system prompt
        return skillAdapter.getAllInstructions();
    }
    
    public Object executeSkillFunction(String functionName, Map<String, Object> arguments) {
        // Execute skill function
        return skillAdapter.executeFunction(functionName, arguments);
    }
}
```

## 📁 Project Structure

```
src/main/java/org/unreal/agent/skill/
├── AgentSkill.java                    # Core interface
├── AgentSkillResult.java              # Result class
├── AgentSkillManager.java             # Skill management service
├── springai/
│   └── SpringAIAgentSkillAdapter.java  # Spring AI adapter
├── folder/
│   ├── SkillDescriptor.java         # Skill descriptor
│   ├── SkillMarkdownParser.java      # agentskills.io parser
│   ├── FolderBasedSkillLoader.java  # Folder-based skill loader
│   ├── MarkdownAgentSkill.java      # Markdown instruction skill
│   ├── SkillLifecycleManager.java   # Lifecycle management
│   └── AgentSkillManager.java       # agentskills.io specific manager
├── example/
│   ├── TextAnalysisSkill.java        # Example skill
│   └── DateTimeSkill.java           # Example skill
├── config/
│   ├── AgentSkillAutoConfiguration.java # Auto-configuration
│   ├── AgentSkillProperties.java     # Configuration properties
│   └── AgentSkillConfiguration.java    # Configuration class
└── migration/
    └── SkillMigrationUtils.java        # Migration tools
```

## 📂 Example Skill Directory Structure

```
skills/
├── pdf-processing/
│   ├── SKILL.md                   # agentskills.io format skill
│   ├── scripts/                    # Processing scripts
│   │   ├── extract-pdf.py
│   │   └── fill-form.py
│   ├── references/                 # Reference documentation
│   │   ├── REFERENCE.md
│   │   └── FORMS.md
│   └── assets/                     # Resource files
│       ├── templates/
│       └── icons/
├── code-review/
│   └── SKILL.md
├── data-analysis/
│   └── SKILL.md
└── email-automation/
    └── SKILL.md
```

## ✅ Validation and Migration

### Validate Skills
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

### Migrate Skills
```java
@Autowired
private SkillMigrationUtils migrationUtils;

public void migrateSpringBeanSkill(AgentSkill skill, Path outputDir) {
    Path migratedSkill = migrationUtils.migrateSkillToFolder(skill, outputDir);
    System.out.println("Migrated skill to: " + migratedSkill);
}
```

## 🔌 REST API Integration

The framework provides REST API endpoints for third-party Spring AI service integration:

| Endpoint | Description |
|----------|-------------|
| `GET /api/agent-skills/discovery` | Get skill discovery information (lightweight) |
| `GET /api/agent-skills/all` | Get all skill information |
| `POST /api/agent-skills/execute/{skillName}` | Execute skill |
| `GET /api/agent-skills/spring-ai-functions` | Get Spring AI function definitions |
| `GET /api/agent-skills/names` | Get all skill names |
| `GET /api/agent-skills/{skillName}` | Get specific skill details |

### Integration with Third-Party Spring AI Services

#### Using Spring AI Adapter

```java
@Autowired
private SpringAIAgentSkillAdapter adapter;

// Get function definitions
List<Map<String, Object>> functions = adapter.getFunctionDefinitions();

// Execute skill function
Object result = adapter.executeFunction("datetime", parameters);

// Get system prompt enhancement
String instructions = adapter.getAllInstructions();
```

#### Progressive Disclosure Integration

```java
// Discovery phase: Get lightweight skill information
List<String> discoveryInfo = adapter.getSkillDiscoveryInfo();

// Get all skill information (organized by tier)
Map<String, Object> allSkills = adapter.getAllSkillsForAgentskillsIo();
```

## 🎯 Advanced Features

### Progressive Disclosure
Three-tier content management following agentskills.io specification:
1. **Discovery Phase**: Load only name and description (~100 tokens)
2. **Activation Phase**: Load full SKILL.md instructions (<5000 tokens)
3. **Execution Phase**: Load scripts, references, and resources on demand

### Performance Optimization
- Skill metadata caching
- Lazy loading of large skill content
- File size validation
- Concurrent execution control

### Monitoring and Analytics
- Skill execution metrics
- Error rates and response times
- Usage statistics and analysis

## 🔒 Security Features

- Skill validation and sandbox execution
- Dependency management and secure storage
- Input validation and sanitization
- Access control and audit logging

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Code Structure
For detailed information about the project structure and coding standards, see [Code Structure Guide](docs/code-structure.md).

## 📝 License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## 🙏 Acknowledgments

- [agentskills.io](https://agentskills.io) - For the skill specification standard
- [Spring AI](https://spring.io/projects/spring-ai) - For the AI framework
- [OpenAI](https://openai.com) - For the Function Calling API

## 📧 Contact

For questions and support, please open an issue on GitHub.

---

**This framework provides a complete, production-ready skill management solution for Spring AI applications while maintaining full compatibility with the agentskills.io standard.**
