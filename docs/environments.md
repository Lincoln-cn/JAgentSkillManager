# 环境与配置差异（dev / prod）

本项目通过 Spring Profiles 管理环境差异。可用配置文件：

- `application.yml` — 基础配置
- `application-dev.yml` — 开发环境覆盖
- `application-prod.yml` — 生产环境覆盖

启动方式：

- 开发： `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- 生产： `mvn spring-boot:run -Dspring-boot.run.profiles=prod` 或 `java -jar -Dspring.profiles.active=prod target/*.jar`

主要差异：

- 日志级别：dev 下 `org.unreal=DEBUG`，prod 下 `org.unreal=INFO`。
- 热重载：dev 下 `agent.skill.hot-reload-enabled=true`（方便本地调整 skills 内容即时生效），prod 下默认关闭以保证稳定性。
- 监控与外部集成：建议在 prod 中开启更多监控与指标采集（未在模板中内置，按需添加）。

示例启动（生产，设置日志目录）：

```
export LOG_HOME=/var/log/agent-skill-manager
java -jar -Dspring.profiles.active=prod target/agent-skill-manager-1.0-SNAPSHOT.jar
```
