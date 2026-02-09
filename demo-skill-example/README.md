# Demo Calculator Skill

这是一个示例 Agent Skill，展示了如何创建和打包 Skill。

## 文件结构

```
demo-calculator-skill/
├── skill.json          # Skill 配置文件（必需）
├── README.md           # 说明文档（可选但推荐）
└── examples/           # 示例文件目录（可选）
    └── example-usage.md
```

## skill.json 字段说明

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | string | ✓ | Skill 的唯一标识名 |
| `version` | string | ✓ | 版本号，如 "1.0.0" |
| `description` | string | ✓ | Skill 的功能描述 |
| `author` | string | ✗ | 作者信息 |
| `tags` | array | ✗ | 标签数组，用于分类 |
| `enabled` | boolean | ✓ | 是否启用 |
| `main` | string | ✗ | Java 主类全名（如果是代码型 Skill）|
| `parameters` | object | ✗ | 参数定义 |
| `entryPoints` | array | ✗ | 入口点配置 |
| `instructions` | string | ✗ | 给 AI 的指令 |
| `resources` | object | ✗ | 资源文件引用 |

## 打包说明

将 skill 文件夹打包成 ZIP 格式：

```bash
# Windows PowerShell
Compress-Archive -Path demo-calculator-skill/* -DestinationPath demo-calculator-skill.zip

# Linux/Mac
zip -r demo-calculator-skill.zip demo-calculator-skill/
```

**注意事项：**
1. ZIP 文件可以直接包含 skill 文件，也可以包含一个父文件夹
2. 系统会自动查找 `skill.json`、`skill.yaml` 或 `SKILL.md` 作为入口
3. 文件名区分大小写

## 部署方式

### 1. 通过 API 上传

```bash
curl -X POST http://localhost:8080/api/agent-skills/manage/upload \
  -F "file=@demo-calculator-skill.zip" \
  -F "skillName=demo-calculator-skill"
```

### 2. 直接放置到 skills 目录

将解压后的文件夹复制到项目的 `skills/` 目录下，系统会自动热加载。

## 支持的描述文件格式

### 1. JSON 格式 (skill.json)

如上所示，使用标准 JSON 格式定义。

### 2. YAML 格式 (skill.yaml / skill.yml)

```yaml
name: demo-calculator-skill
version: 1.0.0
description: A demo calculator skill
author: Demo Developer
enabled: true
main: com.example.CalculatorSkill
parameters:
  operation:
    type: string
    required: true
```

### 3. Markdown 格式 (SKILL.md)

遵循 [agentskills.io](https://agentskills.io) 规范：

```markdown
---
name: demo-calculator-skill
version: 1.0.0
---

# Description

A demo calculator skill...

## Instructions

You are a calculator assistant...
```

## 进阶：代码型 Skill

如果 Skill 需要执行代码，可以包含以下文件：

### JAR 文件方式

```
demo-skill/
├── skill.json
├── demo-skill.jar        # 包含编译后的类
└── lib/                  # 依赖库（可选）
    └── dependency.jar
```

skill.json 中指定：
```json
{
  "name": "demo-skill",
  "main": "com.example.MySkill",
  ...
}
```

### 类文件方式

```
demo-skill/
├── skill.json
└── classes/
    └── com/
        └── example/
            └── MySkill.class
```

## 更多示例

查看 `examples/` 目录获取更多使用示例。
