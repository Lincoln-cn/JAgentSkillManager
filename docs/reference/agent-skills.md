### Agent Skills 概述

**Agent Skills** 是一种将专业知识和工作流程规范固化为可复用资产的工具，旨在提高 AI 工具（如 Claude、GitHub Copilot、Cursor 等）的效率和可靠性。它通过模块化的 Markdown 文件（`SKILL.md`）来指导 AI 执行特定任务，并支持自动触发、团队共享和工程化管理。

### 核心概念

- **Skill（技能）**：完成特定任务的完整套件，类似于一个独立的工具或食谱。
- **Instruction（指令）**：告诉 AI 具体要做什么、如何思考和输出什么格式的说明书或步骤。
- **Knowledge（知识）**：作为 Skill 知识库的背景资料，如产品手册、API 文档等。
- **Tool（工具）**：定义 Skill 可调用的外部 API 或函数，用于获取数据或执行操作。

### Skill 的结构

一个 Skill 是一个文件夹，必须包含一个 `SKILL.md` 文件，可选其他资源文件（如脚本、示例、参考文档）。`SKILL.md` 是核心文件，用于教 AI 在特定场景下按指定方式执行任务。

#### `SKILL.md` 基本模板

```markdown
---
name: your-skill-name
description: What it does and when Claude should use it
---

# Skill Title

## Instructions
Clear, concrete, actionable rules.

## Examples
- Example usage 1
- Example usage 2

## Guidelines
- Guideline 1
- Guideline 2
```

#### 元数据字段

- `name`：Skill 显示名称，默认使用目录名，仅支持小写字母、数字和短横线（最长 64 字符）。
- `description`：技能用途及使用场景，AI 根据它判断是否自动应用。
- `argument-hint`：自动补全时显示的参数提示。
- `disable-model-invocation`：设为 `true` 禁止 AI 自动触发，仅能手动调用。
- `user-invocable`：设为 `false` 从 `/` 菜单隐藏，作为后台增强能力使用。
- `allowed-tools`：Skill 激活时 AI 可无授权使用的工具。
- `model`：Skill 激活时使用的模型。
- `context`：设为 `fork` 时在子代理上下文中运行。
- `agent`：子代理类型（配合 `context: fork` 使用）。
- `hooks`：技能生命周期钩子配置。

### 工作原理

Agent Skills 的工作原理基于渐进式披露，分三层加载：

1. **技能发现**：AI 先读取所有技能的元数据（`name` 和 `description`），判断任务是否相关，这些元数据始终在系统提示中。
2. **加载核心指令**：如果相关，AI 自动读取 `SKILL.md` 的正文内容，获取详细指导。
3. **加载资源文件**：只在需要时读取额外文件（如脚本、示例），或通过工具执行脚本。

### 支持的工具和环境

目前主要支持以下工具和环境：

- **Claude（Anthropic）**：包括 Claude.ai、Claude Code、Agent SDK。
- **VS Code + GitHub Copilot**：项目级（`.github/skills/`）或个人级技能。
- **Cursor**：项目级（`.cursor/skills/`）或全局技能，支持从 GitHub 安装。
- **其他**：正在扩展中，标准开源在 [GitHub](https://github.com/agentskills/agentskills)。

### 为什么需要 Skills？

普通 AI 代理在缺少特定上下文时容易出错，例如：

- 团队有自己的代码规范，但 AI 每次都要手动提醒。
- 需要处理复杂流程（如 PDF 表单、调试 GitHub Actions），AI 可能不知道最佳实践。

Agent Skills 解决这些问题：

- **自动触发**：AI 根据任务自动加载相关技能，无需手动输入长提示。
- **可复用 & 可共享**：一次创建，全团队或社区使用，支持 Git 版本控制。
- **高效利用上下文**：采用渐进式披露，只加载需要的部分，避免上下文窗口溢出。
- **跨平台**：同一个 Skill 可以在 Claude、VS Code Copilot、Cursor 等工具中使用。

### Skill 执行流程

从用户指令开始，先进行 Skill 意图识别，决定是否进入受控执行路径。命中 Skill 后，系统加载 `SKILL.md`，建立工具权限与行为边界，再结合上下文进行推理。只有在确实需要时才调用被允许的外部工具，否则在规则内完成逻辑。最终结果经过约束整合后输出，用户的下一次输入触发新一轮完整流程。

### 创建和使用 Skill

#### 创建 Skill 目录

Skills 存放在 `~/.claude/skills/`（个人全局）或项目目录下的 `.claude/skills/`（项目专用）。例如，创建一个项目目录 `claude-test`：

```bash
mkdir claude-test
cd claude-test
mkdir -p .claude/skills/python-naming-standard
```

#### 编写配置文件 `SKILL.md`

在目录下创建 `SKILL.md`，这是 Skill 的大脑，告诉 AI 什么时候用它：

```markdown
---
name: Python 内部命名规范技能
description: 当用户要求重构、审查或编写 Python 代码时，请参考此规范。
---

## 指令
1. 所有的内部辅助函数必须以 `_internal_` 前缀命名。
2. 如果发现不符合此规则的代码，请自动提出修改建议。
3. 在执行 `claude commit` 前，必须检查此规范。

## 参考示例
- 正确：`def _internal_calculate_risk():`
- 错误：`def _calculate_risk():`
```

#### 使用 Skill

启动 Claude Code：

```bash
claude
```

输入任务：

```
帮我写一个计算用户折扣的函数
```

Claude 会扫描已安装的 Skills，发现请求涉及 "Python 代码编写"，匹配 `python-naming-standard`，并生成符合规范的代码：

```python
def _internal_get_discount(user_score):
    # 计算逻辑...
    return discount
```

#### 添加资源文件（可选）

可以在 `.claude/skills/` 下添加以下目录：

- `examples/`：存放示例文件。
- `references/`：存放参考文档。
- `scripts/`：存放可执行脚本。

然后在 `SKILL.md` 中引用：

```markdown
查看示例 commit：./examples/good-commit.txt
运行脚本：使用工具执行 ./scripts/process.py
```

### 官方市场和插件

#### 官方市场

访问 [GitHub](https://github.com/anthropics/skills) 仓库下载预设的技能，如 React 优化器、SQL 调优工具。

#### Skill Creator

对 Claude 说："帮我把我刚才教你的关于 Docker 的配置逻辑总结成一个 Skill"，它会自动在相应目录生成文件。

#### 插件安装

将官方市场注册为 Claude Code 的插件市场：

```bash
/plugin marketplace add anthropics/skills
```

安装指定技能集：

```bash
/plugin install document-skills@anthropic-agent-skills
/plugin install example-skills@anthropic-agent-skills
```

插件安装完成后，需要重启 Claude Code。

### 相关资源

- **Skills 市场**：[https://skillsmp.com/zh](https://skillsmp.com/zh)
- **官方标准网站**：[https://agentskills.io](https://agentskills.io)
- **Anthropic 工程文章**：[https://www.anthropic.com/engineering/equipping-agents-for-the-real-world-with-agent-skills](https://www.anthropic.com/engineering/equipping-agents-for-the-real-world-with-agent-skills)
- **VS Code 文档**：[https://code.visualstudio.com/docs/copilot/customization/agent-skills](https://code.visualstudio.com/docs/copilot/customization/agent-skills)
- **Agent Skills GitHub**：[https://github.com/anthropics/skills](https://github.com/anthropics/skills)
- **Claude 技能列表**：[https://github.com/ComposioHQ/awesome-claude-skills](https://github.com/ComposioHQ/awesome-claude-skills)
- **软件开发工作流程 skills**：[https://github.com/obra/superpowers](https://github.com/obra/superpowers)
- **自动写 skill 的 skill**：[https://github.com/anthropics/skills/tree/main/skills/skill-creator](https://github.com/anthropics/skills/tree/main/skills/skill-creator)