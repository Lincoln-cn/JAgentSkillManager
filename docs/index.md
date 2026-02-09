# 文档索引

这是项目文档的集中索引页，包含中文与英文主要文档的快速入口与简短说明。目的是让阅读者能快速找到所需文档并了解每个文档的用途。

## 中文文档（Recommended entry points）

- [项目文档目录](README.md) - 本目录页，包含文档结构与快速阅读建议。
- API: [api/API_DOCUMENTATION.md](api/API_DOCUMENTATION.md) - REST API 的完整参考（请求/响应示例、端点说明）。
- 使用指南:
  - [guides/use/tool-integration.md](guides/use/tool-integration.md) - 通过 Function Calling 集成技能的使用方法与示例。
  - [guides/use/skill-prompt-integration.md](guides/use/skill-prompt-integration.md) - 使用渐进式披露将技能指令注入系统提示的实践说明。
- 参考文档（实用说明）：
  - [reference/dependencies.md](reference/dependencies.md) - 项目主要依赖与运行时要求（Java / Spring Boot / 核心库）。
  - [reference/logging.md](reference/logging.md) - 日志（Logback）配置细节与运行时覆盖方法。
  - [reference/environments.md](reference/environments.md) - dev/prod 配置差异与启动示例。
  - [reference/skills-disclosure.md](reference/skills-disclosure.md) - 渐进式披露策略与安全建议（脚本不自动执行，仅披露内容）。

## English Documents

- Tools Integration: [guides/use/tool-integration_EN.md](guides/use/tool-integration_EN.md) - Integrate skills via Function Calling (examples).
- Skill Prompt Integration: [guides/use/skill-prompt-integration_EN.md](guides/use/skill-prompt-integration_EN.md) - Progressive disclosure patterns for prompt injection.
- API: [api/API_DOCUMENTATION.md](api/API_DOCUMENTATION.md) - API reference (English sections included).

## 其他重要文档

- [code-structure.md](code-structure.md) - 项目代码结构与模块说明（开发者必读）。
- [development/CONTRIBUTING.md](development/CONTRIBUTING.md) - 贡献指南与流程。

如果你想要我为 `docs/index.md` 增加按页面更新时间自动生成的摘要或把每个文档的“上次更新”写进页面，我可以继续实现（需要读取文件元信息或使用 git log）。
