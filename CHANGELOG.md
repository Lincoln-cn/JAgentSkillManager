# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release of Agent Skill Manager framework
- Support for Spring Bean skills
- Support for agentskills.io specification format
- Function calling integration with Spring AI
- Progressive disclosure for efficient context management
- Hot reload and dynamic skill loading/unloading
- REST API endpoints for skill management
- Skill validation according to agentskills.io spec
- Skill migration utilities
- Complete documentation and integration guides

### Features

#### Core Features
- ✅ Multi-format skill support (Spring Bean + agentskills.io)
- ✅ Spring AI integration with function calling
- ✅ Progressive disclosure (Discovery → Activation → Execution)
- ✅ Dynamic skill lifecycle management
- ✅ Folder-based skill loading with hot reload
- ✅ Comprehensive validation and error handling

#### Spring AI Integration
- Function definitions generation
- Parameter schema creation
- System prompt enhancement with skill instructions
- Execution and error handling

#### agentskills.io Support
- SKILL.md parser with YAML Frontmatter
- Skill validation (name format, required fields, etc.)
- Progressive disclosure implementation
- Metadata management and caching

#### REST API
- `GET /api/agent-skills/discovery` - Skill discovery
- `GET /api/agent-skills/all` - All skills information
- `POST /api/agent-skills/execute/{skillName}` - Execute skill
- `GET /api/agent-skills/spring-ai-functions` - Function definitions
- `GET /api/agent-skills/names` - Skill names list
- `GET /api/agent-skills/{skillName}` - Specific skill details

### Documentation
- Comprehensive README (Chinese and English)
- Tools integration guide
- Skill prompt integration guide
- Code structure documentation
- API documentation
- Contributing guidelines

### Examples
- PDF Processing Skill
- Code Review Skill
- Data Analysis Skill
- Email Automation Skill
- Weather Query Skill

### Project Structure
```
org.unreal.agent.skill/
├── AgentSkill.java (Core interface)
├── AgentSkillManager.java
├── AgentSkillResult.java
├── config/ (Configuration)
├── dto/ (Data Transfer Objects)
├── vo/ (Value Objects)
├── folder/ (Folder-based skills)
│   └── model/ (Domain models)
├── example/ (Example skills)
├── springai/ (Spring AI adapter)
└── web/ (REST controllers)
```

### Technical Details
- **Java Version**: 17+
- **Spring Boot**: 3.3.5
- **Spring AI**: 1.0.0-M1
- **Total Files**: 33 Java source files
- **Test Coverage**: Unit and integration tests included
- **Lines of Code**: ~4,000 lines

## [1.0.0] - 2026-02-05

### Initial Release 🎉

First stable release of Agent Skill Manager framework.

#### Added
- Complete framework implementation
- Full documentation in both Chinese and English
- Example skills for common use cases
- Maven build configuration
- GitHub repository setup

#### Features
- Spring AI integration
- agentskills.io specification support
- Hot reload capability
- REST API endpoints
- Validation and migration tools

#### Documentation
- README.md (中文)
- README_EN.md (English)
- Integration guides
- Code structure guide
- Contributing guidelines

---

## Release Notes Format

### Version Numbering
We follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Incompatible API changes
- **MINOR**: New functionality (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Categories
- **Added**: New features
- **Changed**: Changes to existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements

---

## Future Roadmap

### [1.1.0] - Planned
- [ ] Support for more LLM providers (Claude, Gemini)
- [ ] Enhanced caching strategies
- [ ] More example skills
- [ ] Performance optimizations
- [ ] Additional validation rules

### [1.2.0] - Planned
- [ ] Web UI for skill management
- [ ] Skill marketplace integration
- [ ] Advanced analytics
- [ ] Plugin system
- [ ] Multi-language support for skills

### [2.0.0] - Planned
- [ ] Breaking changes for API improvements
- [ ] Reactive programming support
- [ ] Cloud-native features
- [ ] Distributed skill management

---

## How to Update

### Maven
```xml
<dependency>
    <groupId>org.unreal</groupId>
    <artifactId>agent-skill-manager</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'org.unreal:agent-skill-manager:1.0.0'
```

---

## Contributors

Thanks to all contributors who helped make this project possible!

- Lincoln - Project creator and maintainer

Want to contribute? See [CONTRIBUTING.md](CONTRIBUTING.md)

---

For the latest updates, visit: https://github.com/Lincoln-cn/JAgentSkillManager
