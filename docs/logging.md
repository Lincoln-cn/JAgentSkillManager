# 日志配置说明

项目采用 Logback（`logback-spring.xml`）作为主日志框架，配置位于 `src/main/resources/logback-spring.xml`。

主要特性：

- 控制台输出（CONSOLE）用于交互式开发与容器标准输出。
- 异步文件输出（ASYNC_FILE）写入到 `${LOG_HOME}/${spring.application.name}.log`，默认 `LOG_HOME=logs`。
- 文件滚动策略：按日期滚动并支持基于大小的细分（每日 + 每文件最大 100MB），保留 30 天，最大总量上限 2GB。
- 不同 Spring Profile 定制：
  - dev: `org.unreal` DEBUG， `org.springframework` INFO，控制台 + 文件同时输出。
  - prod: `org.unreal` INFO， `org.springframework` WARN，文件优先，控制台输出较简洁。

如何覆盖日志目录：

在运行时设置环境变量 `LOG_HOME`，例如：

Linux/macOS:
```
export LOG_HOME=/var/logs/myapp
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Windows (PowerShell):
```
$env:LOG_HOME = 'C:\logs'
mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

建议：生产环境应把 `LOG_HOME` 指向集中日志目录，并由外部日志聚合/轮转（systemd、logrotate、ELK/Promtail 等）管理。
