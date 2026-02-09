# 项目文档目录

本文档目录包含了 Agent Skill Manager 框架的所有相关文档。文档按类别组织，便于查找和阅读。

## 📁 目录结构

```
docs/
├── api/                    # API 文档
├── development/            # 开发文档
├── guides/                 # 使用指南
│   └── use/               # 功能使用指南
├── reference/             # 参考文档
├── examples/              # 示例文档
├── code_summary/          # 代码总结
└── code-structure.md      # 项目代码结构
```

## 📖 文档分类

### 1. API 文档 (api/)

**[API_DOCUMENTATION.md](api/API_DOCUMENTATION.md)** - 完整的 RESTful API 文档

包含所有管理端点的详细说明：
- 技能部署 API (上传/删除/重载/导出)
- 文件管理 API (列表/读取/更新/删除)
- 查询 API (技能列表/详情/执行)
- 完整的请求/响应示例

### 2. 开发文档 (development/)

| 文档 | 说明 |
|------|------|
| **[CONTRIBUTING.md](development/CONTRIBUTING.md)** | 贡献指南 - 如何参与项目开发 |
| **[CODE_OF_CONDUCT.md](development/CODE_OF_CONDUCT.md)** | 行为准则 - 社区参与规范 |
| **[CHANGELOG.md](development/CHANGELOG.md)** | 变更日志 - 版本更新记录 |

### 3. 使用指南 (guides/use/)

| 文档 | 说明 |
|------|------|
| **[tool-integration.md](guides/use/tool-integration.md)** | Tools 集成指南 - 通过 Function Calling 集成技能 |
| **[tool-integration_EN.md](guides/use/tool-integration_EN.md)** | Tools Integration Guide (English) |
| **[skill-prompt-integration.md](guides/use/skill-prompt-integration.md)** | Skill Prompt 集成指南 - 通过渐进式披露植入提示词 |
| **[skill-prompt-integration_EN.md](guides/use/skill-prompt-integration_EN.md)** | Skill Prompt Integration Guide (English) |

### 4. 参考文档 (reference/)

**[agent-skills.md](reference/agent-skills.md)** - Agent Skills 概念和参考文档

### 5. 项目结构

**[code-structure.md](code-structure.md)** - 项目代码结构说明

### 6. 代码总结 (code_summary/)

包含代码重构和改动的总结文档。

### 7. 示例文档 (examples/)

用于存放示例项目和教程文档。

---

## 🚀 快速开始

如果你是第一次使用本框架，建议按以下顺序阅读：

1. **先阅读** [项目根目录 README](../README.md) - 了解框架概览
2. **再阅读** [Tools 集成指南](guides/use/tool-integration.md) - 学习如何集成技能
3. **参考** [API 文档](api/API_DOCUMENTATION.md) - 了解 RESTful API 使用方法
4. **开发时查阅** [代码结构](code-structure.md) - 了解项目组织结构

---

## 文档索引（英文）

- **[Tools Integration Guide](guides/use/tool-integration_EN.md)** - Tools integration via Function Calling
- **[Skill Prompt Integration Guide](guides/use/skill-prompt-integration_EN.md)** - Progressive disclosure for skill prompts
- **[API Documentation](api/API_DOCUMENTATION.md)** - RESTful API full reference (English & Chinese)
- **[Code Structure Guide](code-structure.md)** - Project structure and coding standards

## 📝 贡献文档

如果你想改进文档：

1. 遵循 [贡献指南](development/CONTRIBUTING.md) 中的规范
2. 保持中英文文档同步更新
3. 在修改后更新本文档目录索引
4. 提交 PR 前在本地预览文档效果

## 📞 获取帮助

- 查看 [CHANGELOG](development/CHANGELOG.md) 了解最新功能
- 阅读 [API 文档](api/API_DOCUMENTATION.md) 了解接口详情
- 参考示例项目了解实际用法

---

**提示**: 本文档目录会随项目发展持续更新，建议定期查看最新版本。
