# sop-process-platform

工业装配 SOP（Standard Operating Procedure）后端系统，从 RuoYi-Vue 中剥离并独立为 **Spring Boot 3.2.5** 项目。

面向工业装配线的标准化流程管理，支持 AI 视觉自动过站、过程事件追踪、RabbitMQ 异步事件广播、质量追溯。

---

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| Java | 17 | 运行环境 |
| MyBatis-Plus | 3.5.6 | ORM（BaseMapper + 分页 + 自动填充 + 乐观锁） |
| MySQL | 8.0 | 数据库 |
| Spring AMQP | 3.1.x | RabbitMQ 客户端 |
| Spring Data Redis | 3.2.x | 依赖已引入（当前使用本地锁） |
| Spring Scheduling | 内置 | 定时任务（@EnableScheduling） |
| Knife4j (SpringDoc) | 4.5.0 | API 文档 |
| Jackson | 2.15 | JSON 序列化 |
| Lombok | latest | 代码简化 |
| Maven | 3.x | 构建工具 |

---

## 核心能力

| 能力 | 说明 | 阶段 |
|------|------|:---:|
| SOP 流程建模 | ProductModel → SopStep 层级定义 | 2 |
| 装配任务管理 | startTask → startStep → ai-next-step → finishTask | 3 |
| 轻量状态机 | 5 任务状态 × 6 步骤状态 × 6 事件，Map 驱动 | 3 |
| 外部视觉服务集成 | VisionGateway 接口 + RestTemplate HTTP 实现 + enabled 开关 | 3 |
| AI 回调幂等 | eventId 去重 + 派生 eventId + 重复回调幂等返回 | 4 |
| 过程事件追踪 | process_event 表 + ProcessEventTxService (REQUIRES_NEW) | 4 |
| 设备维度并发锁 | LocalDeviceLock（DeviceLock 接口预留 Redis） | 5 |
| AssemblyTask 乐观锁 | @Version + OptimisticLockerInnerInterceptor | 5 |
| 视觉失败事件查询 | 按设备/全局查询 VISION_SERVICE_ERROR | 5 |
| 视觉手动重试 | retryLastFailed → 根据 payload 重建请求 | 5 |
| 步骤超时检测 | @Scheduled 扫描 RUNNING 步骤 → STEP_TIMEOUT 事件 | 5 |
| RabbitMQ 异步事件 | process_event 落库后发布 MQ，过程事件异步消费 | 6 |
| 消费幂等 | mq_message_log 表 messageId + consumerName 唯一 | 6 |
| 视觉异步重试 | VisionRetryConsumer 消费 sop.vision.retry.queue | 6 |
| 死信队列 | 失败 3 次 → sop.dead.letter.queue → DeadLetterConsumer | 6 |

---

## 核心接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/process/assembly-task/start` | 开工 |
| POST | `/api/process/assembly-task/{taskId}/steps/{stepId}/start` | 启动步骤 |
| POST | `/api/process/assembly-task/ai-next-step` | AI 过站回调（含 eventId 幂等） |
| POST | `/api/process/assembly-task/{taskId}/finish` | 手动完成任务 |
| GET | `/api/process/vision/failed-events/{deviceSn}` | 查询设备视觉失败事件 |
| GET | `/api/process/vision/failed-events` | 查询所有视觉失败事件 |
| POST | `/api/process/vision/retry/{deviceSn}` | 手动重试视觉服务 |

另有 5 个业务对象 CRUD 接口（ProductModel / SopStep / AssemblyTask / AssemblyStepLog / Worker）。

启动后访问 API 文档：http://localhost:8088/doc.html

---

## 项目结构

```
com.sop
├── common           # ApiResult, BusinessException, ErrorCode, PageResult
├── config           # MyBatisPlusConfig, OpenApiConfig, RabbitMqConfig
├── infrastructure   # DeviceLock, LocalDeviceLock
├── integration      # VisionGateway, PythonVisionGateway
├── mq               # SopProcessEventMessage, Publisher, Consumers, MqMessageLog
├── workflow         # AssemblyStateMachine
└── process
    ├── command      # 输入 Command
    ├── controller   # REST 接口
    ├── domain       # 实体（ProcessEvent, AssemblyTask 等）
    ├── dto          # CRUD 传输对象
    ├── mapper       # MyBatis-Plus Mapper
    ├── schedule     # StepTimeoutCheckScheduler
    ├── service      # 服务接口
    │   └── impl     # SopExecutionServiceImpl 为核心编排
    └── vo           # 视图对象
```

Java 文件总数：**83**

---

## 当前阶段完成度

| 阶段 | 主题 | 状态 |
|:---:|------|:---:|
| 1 | 独立 Spring Boot 3 骨架 | ✅ |
| 2 | 5 个业务对象基础 CRUD | ✅ |
| 3 | SOP 核心执行流程（状态机 + 视觉网关 + 质量追溯） | ✅ |
| 4 | 可靠性与一致性增强（process_event + eventId 幂等 + 上下文校验） | ✅ |
| 5 | 并发控制与异常补偿（设备锁 + 乐观锁 + 视觉重试 + 超时检测） | ✅ |
| 6 | RabbitMQ 事件驱动改造与异步补偿增强 | ✅ |
| 7 | 操作审计 + Docker Compose + RedisDeviceLock | 🔲 计划中 |

---

## 本地启动

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sop_process DEFAULT CHARSET utf8mb4"

# 2. 执行建表 SQL
mysql -u root -p sop_process < src/main/resources/sql/sop_process_ddl.sql

# 3. （可选）启动 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 4. 修改 application.yml
#    spring.datasource.password     → 数据库密码
#    vision.service.enabled         → false（本地测试不需 Python）
#    sop.mq.enabled                 → false（本地无 RabbitMQ 时）
#      若 sop.mq.enabled=false，需注释 spring.rabbitmq 配置段

# 5. 启动
cd sop-process-platform
mvn clean compile
mvn spring-boot:run

# 6. 访问
# API 文档：http://localhost:8088/doc.html
# RabbitMQ 管理：http://localhost:15672 (guest/guest)
```

---

## 关键配置参考

```yaml
# application.yml 关键配置项

vision:
  service:
    enabled: false             # false=跳过 Python HTTP 调用

sop:
  mq:
    enabled: false             # false=MQ Bean 不创建，主流程不受影响
  timeout:
    check-interval: 60000      # 超时检测间隔（毫秒）
    step-multiplier: 2.0       # 超时倍数（standardDuration * multiplier）
```

---

## 后续计划

- **RedisDeviceLock** — 基于 DeviceLock 接口实现 Redis 分布式锁
- **操作审计 AOP** — Controller 层记录调用者、参数、时间、结果
- **traceId 链路追踪** — Controller → Service → MQ → Consumer
- **Docker Compose** — 一键启动 MySQL + RabbitMQ + sop-process-platform
- **Actuator 健康检查** — MySQL / RabbitMQ / Vision 探针
- **表结构整理** — 合并 ALTER TABLE 到 CREATE TABLE，统一枚举值命名

---

## 文档

- [AI Handoff 文档](docs/AI_HANDOFF.md) — 完整项目上下文、核心流程、实体字段速查、事件类型、已知限制
