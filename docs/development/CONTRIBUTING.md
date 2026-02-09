# Contributing to Agent Skill Manager

First off, thank you for considering contributing to Agent Skill Manager! It's people like you that make this framework a great tool for the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Workflow](#development-workflow)
- [Style Guidelines](#style-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project and everyone participating in it is governed by our commitment to:

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Accept responsibility and apologize when mistakes are made

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Setup Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/JAgentSkillManager.git
   cd JAgentSkillManager
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run tests:
   ```bash
   mvn test
   ```

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates.

When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the issue
- **Expected behavior** vs actual behavior
- **Environment details**:
  - Java version
  - Spring Boot version
  - OS and version
- **Code samples** or test cases
- **Stack traces** if applicable

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- Use a clear and descriptive title
- Provide detailed description of the proposed feature
- Explain why this enhancement would be useful
- List some examples of how the feature would be used

### Contributing Code

#### Types of Contributions We're Looking For

- New skill examples
- Bug fixes
- Performance improvements
- Documentation improvements
- New features aligned with the project roadmap
- Test coverage improvements

#### Types of Contributions We're NOT Looking For

- Breaking changes without prior discussion
- Changes that violate the agentskills.io specification
- Code that doesn't follow our style guidelines

## Development Workflow

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation changes
- `refactor/` - Code refactoring
- `test/` - Test improvements

### 2. Make Your Changes

- Write clean, readable code
- Follow the existing code style
- Add/update tests as needed
- Update documentation if applicable

### 3. Test Your Changes

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn jacoco:report

# Check code style
mvn checkstyle:check
```

### 4. Commit Your Changes

See [Commit Messages](#commit-messages) section below.

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub.

## Style Guidelines

### Java Code Style

We follow standard Java conventions with these specifics:

#### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Braces**: K&R style (opening brace on same line)
- **Line endings**: LF (Unix-style)

#### Naming Conventions

- **Classes**: PascalCase (e.g., `AgentSkillManager`)
- **Methods**: camelCase (e.g., `executeSkill`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_SKILL_SIZE`)
- **Variables**: camelCase (e.g., `skillManager`)
- **Packages**: lowercase, no underscores (e.g., `org.unreal.agent.skill`)

#### Code Organization

```java
package org.unreal.agent.skill;

// 1. Imports (organized by group)
import java.util.*;
import org.springframework.*;

// 2. Class-level Javadoc
/**
 * Brief description of the class.
 * 
 * Detailed description if needed.
 */
public class ExampleClass {
    
    // 3. Constants
    private static final int DEFAULT_TIMEOUT = 30;
    
    // 4. Fields
    private final AgentSkillManager skillManager;
    
    // 5. Constructors
    public ExampleClass(AgentSkillManager skillManager) {
        this.skillManager = skillManager;
    }
    
    // 6. Public methods
    public void publicMethod() {
        // implementation
    }
    
    // 7. Private methods
    private void privateMethod() {
        // implementation
    }
}
```

#### Documentation

- All public classes and methods must have Javadoc
- Use `@param`, `@return`, `@throws` appropriately
- Keep descriptions clear and concise

```java
/**
 * Executes the skill with given request and parameters.
 *
 * @param request the input request
 * @param parameters additional parameters for skill execution
 * @return the result of skill execution
 * @throws IllegalArgumentException if request is null
 */
public AgentSkillResult execute(String request, Map<String, Object> parameters) {
    // implementation
}
```

### Project Structure

Follow the established package structure:

- `config/` - Configuration classes
- `dto/` - Data transfer objects
- `vo/` - Value objects
- `folder/model/` - Domain models
- `example/` - Example implementations
- `web/` - Web controllers

## Commit Messages

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, semicolons, etc.)
- **refactor**: Code refactoring
- **test**: Test additions or modifications
- **chore**: Build process or auxiliary tool changes

### Examples

```bash
# Feature
feat(skill): add support for async skill execution

# Bug fix
fix(loader): resolve issue with skill hot reload

# Documentation
docs(readme): update installation instructions

# Refactoring
refactor(manager): split large class into smaller services

# Tests
test(adapter): add unit tests for Spring AI adapter
```

### Guidelines

- Use present tense: "add feature" not "added feature"
- Use imperative mood: "move cursor to..." not "moves cursor to..."
- Don't capitalize first letter
- No period at the end

## Pull Request Process

### Before Submitting

1. **Update your branch** with the latest main:
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Run tests** and ensure they pass

3. **Update documentation** if needed

4. **Add to CHANGELOG.md** following the existing format

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix (non-breaking)
- [ ] New feature (non-breaking)
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing unit tests pass locally
- [ ] I have updated the documentation accordingly

## Related Issues
Fixes #123
```

### Review Process

1. **Automated checks** must pass (CI/CD)
2. **Code review** by at least one maintainer
3. **Approval** required before merging
4. **Squash merge** to keep history clean

### After Merge

- Your contribution will be included in the next release
- You'll be added to the contributors list
- Changes will be documented in CHANGELOG.md

## Recognition

Contributors will be:
- Listed in the README.md
- Mentioned in release notes
- Added to the contributors graph

## Questions?

- Open an issue for discussion
- Join our community discussions
- Contact maintainers directly

Thank you for contributing to Agent Skill Manager! 🎉
