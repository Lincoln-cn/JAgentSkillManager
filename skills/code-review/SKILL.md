---
name: code-review
description: Review code for quality, security, best practices, and potential improvements. Use when user asks for code review, quality assessment, or programming feedback.
license: MIT
metadata:
  author: agent-skill-team
  version: "1.0"
compatibility: Designed for code review workflows and development teams
---

# Code Review Skill

## When to use this skill
Use this skill when you need to review code quality, identify potential issues, suggest improvements, or perform comprehensive code analysis. Works with various programming languages and frameworks.

## How to perform code review

### Basic Code Review
1. **Input**: Provide code file or code snippet
2. **Analyze**: Check for syntax, style, and logic issues
3. **Score**: Provide quality metrics and recommendations
4. **Report**: Generate detailed review report

### Parameters
- `code` (required): The code to review
- `language` (optional): Programming language (auto-detected if not specified)
- `focus_areas` (optional): Array of areas to focus on - ["security", "performance", "style", "maintainability"]
- `severity_level` (optional): Minimum issue severity to report - ["low", "medium", "high", "critical"] (default: "medium")

### Example
```
Request: "Review this Python function for security and performance issues"
Parameters: {
  "code": "def process_user_data(data):\n    return eval(data)",
  "language": "python",
  "focus_areas": ["security", "performance"],
  "severity_level": "medium"
}
```

## Security Review Focus

For security-focused reviews, this skill checks:

- **Injection vulnerabilities**: SQL injection, command injection, XSS
- **Authentication flaws**: Weak authentication mechanisms
- **Data exposure**: Sensitive data leaks or logging
- **Cryptographic issues**: Weak encryption or hashing
- **Input validation**: Missing input sanitization

## Performance Review Focus

For performance-focused reviews:

- **Algorithm efficiency**: Big O complexity analysis
- **Memory usage**: Memory leaks and optimization opportunities  
- **Database queries**: Query optimization suggestions
- **Caching strategies**: Missing or inefficient caching
- **Concurrency issues**: Race conditions and deadlock potential

## Style and Best Practices

Code style and maintainability checks:

- **Naming conventions**: Consistent variable/function naming
- **Code structure**: Proper organization and modularity
- **Documentation**: Missing or inadequate comments/docs
- **Error handling**: Proper exception management
- **Testing coverage**: Missing test cases or test quality

## Output Format

The skill returns a structured review containing:

```json
{
  "overall_score": 8.5,
  "summary": "Code is well-structured but has security concerns",
  "issues": [
    {
      "severity": "high",
      "category": "security", 
      "line": 3,
      "description": "Use of eval() poses security risk",
      "suggestion": "Replace with safer parsing logic"
    }
  ],
  "metrics": {
    "complexity": "low",
    "maintainability": "good",
    "test_coverage": "missing"
  },
  "recommendations": [
    "Add input validation for data parameter",
    "Implement unit tests for edge cases",
    "Replace eval() with json.loads() for JSON data"
  ]
}
```

## Multi-language Support

This skill can review code in:

- **Python**: PEP 8 compliance, security best practices
- **JavaScript**: ES6+ standards, async/await patterns
- **Java**: Clean code principles, Spring Boot patterns
- **TypeScript**: Type safety and interface design
- **Go**: Idiomatic Go patterns and error handling
- **C++**: Modern C++ standards and memory management
- **C#**: .NET conventions and async patterns

## Integration with Development Tools

The skill can integrate with:

### `scripts/lint-runner.py`
Runs language-specific linters and formatters:
- pylint for Python
- ESLint for JavaScript
- SonarLint for Java
- golint for Go

### `scripts/test-generator.py`
Generates test cases based on code analysis:
- Unit test templates
- Integration test scenarios
- Edge case identification

### `scripts/security-scanner.py`
Static analysis security tools:
- Bandit for Python security
- Semgrep for pattern matching
- Custom security rules

## Review Templates

Pre-defined review templates for common scenarios:

### Web Application Review
Focus on web security, authentication, and API patterns.

### Data Processing Review  
Focus on data validation, performance, and error handling.

### Library/SDK Review
Focus on API design, backward compatibility, documentation.

## Configuration Options

Customize review criteria:

```yaml
review_config:
  min_score_threshold: 7.0
  ignore_patterns:
    - "TODO:"
    - "FIXME:"
  custom_rules:
    - name: "no_hardcoded_secrets"
      pattern: "(password|secret|key)"
      action: "flag"
```

## Best Practices Checklist

The skill checks for:

✅ **Security**: No hardcoded secrets, proper input validation  
✅ **Performance**: Efficient algorithms, proper resource usage  
✅ **Style**: Consistent formatting, clear naming  
✅ **Testing**: Adequate test coverage, edge cases  
✅ **Documentation**: Clear comments, API docs  
✅ **Error Handling**: Proper exceptions, logging  
✅ **Dependencies**: Up-to-date, secure versions  

Use this skill to ensure code quality and catch issues early in the development process.