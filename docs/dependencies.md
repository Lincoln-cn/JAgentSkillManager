# 依赖清单

本项目基于 Spring Boot 3.x，以下是主要依赖与说明（详见 `pom.xml`）：

- Java: 17
- Spring Boot: 3.3.5
- Jackson: 用于 JSON/YAML 解析
- SLF4J / Logback: 日志记录
- Maven Wrapper: `./mvnw` 支持构建与运行

如果需要新增运行时脚本引擎（例如 GraalVM / JSR-223），请在这里添加对应依赖并在 loader 中实现加载逻辑。
