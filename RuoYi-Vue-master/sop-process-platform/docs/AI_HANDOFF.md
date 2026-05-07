# AI Handoff — sop-process-platform

> 最后更新：2026-05-07
> 第六阶段已完成，全链路（同步主流程 + MQ 异步事件 + 死信兜底）通过本地测试

---

## 1. 项目目标

工业装配 SOP（Standard Operating Procedure）后端系统。从原 RuoYi-Vue 项目中剥离 process 业务逻辑，独立为 **Spring Boot 3.x** 项目。不依赖 RuoYi 框架。

**核心业务闭环：**

```
ProductModel（设备型号）
  → SopStep（SOP 装配步骤）
    → AssemblyTask（设备 SN 装配任务）
      → AssemblyStepLog（步骤执行日志）
        → VisionGateway（外部 Python 视觉服务）
          → AI 回调过站 → QualityTrace（质量追溯）
                                   ↓
                          ProcessEvent（事件追踪 / 幂等）
                                   ↓
                          RabbitMQ（异步事件广播 + 视觉失败重试 + 死信兜底）
```

---

## 2. 当前技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.6 |
| MySQL Connector | mysql-connector-j |
| Knife4j (SpringDoc) | 4.5.0 |
| Spring AMQP (RabbitMQ) | 3.1.x (Spring Boot 内置) |
| Spring Data Redis | 依赖已引入，预留（无 Redis 服务时使用本地锁） |
| Spring Scheduling | 内置（@EnableScheduling） |
| Jackson | 2.15 (Spring Boot 内置) |
| Lombok | latest |
| Maven | 3.x |

---

## 3. 已完成阶段

### 第一阶段：独立 Spring Boot 3 骨架

- `pom.xml` — Spring Boot 3.2.5 + MyBatis-Plus + Knife4j + Redis 预留
- `application.yml` — 数据源、MP 配置、视觉服务配置
- `SopApplication.java` — `@SpringBootApplication` + `@MapperScan("com.sop.**.mapper")`
- 基础包结构 `com.sop`
- `ApiResult<T>` — 统一响应封装
- `ErrorCode` — 错误码枚举（通用 1xxxx / 业务 2xxxx / 外部服务 3xxxx / 基础设施 4xxxx）
- `BusinessException` — 业务异常类
- `GlobalExceptionHandler` — 全局异常处理（覆盖 10+ 种异常）
- `PageResult<T>` — 分页结果封装，含 `from()` MP Page 转换方法
- `OpenApiConfig` — Knife4j API 文档
- `MyBatisPlusConfig` — 自动填充 + 分页插件（`PaginationInnerInterceptor`）

### 第二阶段：5 个业务对象基础 CRUD

每个对象均包含完整分层：**Entity → DTO → Command → VO → Mapper → Service → ServiceImpl → Controller**

| 对象 | 表名 | Controller | 说明 |
|------|------|:---:|------|
| ProductModel | product_model | ✅ | 设备/产品型号 |
| SopStep | sop_step | ✅ | 型号下的 SOP 装配步骤 |
| AssemblyTask | assembly_task | ✅ | 设备 SN 的装配任务 |
| AssemblyStepLog | assembly_step_log | ✅ | 步骤执行日志 |
| Worker | worker | ✅ | 操作工 |

所有 Controller 统一返回 `ApiResult<T>`，分页统一返回 `ApiResult<PageResult<VO>>`。

SQL DDL 文件：`src/main/resources/sql/sop_process_ddl.sql`

### 第三阶段：SOP 核心执行流程

新增以下组件：

| 组件 | 文件 | 说明 |
|------|------|------|
| 状态机 | `workflow/AssemblyStateMachine.java` | 轻量级，5 任务状态 × 6 步骤状态 × 6 事件 |
| 视觉网关接口 | `integration/vision/VisionGateway.java` | 对外统一调用接口 |
| 视觉网关实现 | `integration/vision/PythonVisionGateway.java` | RestTemplate HTTP 实现，支持 enabled=false 跳过 |
| 视觉服务配置 | `integration/vision/VisionProperties.java` | `@ConfigurationProperties(prefix = "vision.service")` |
| 核心执行服务 | `process/service/SopExecutionService.java` | 流程编排接口 |
| 核心执行实现 | `process/service/impl/SopExecutionServiceImpl.java` | 含 4 个 `@Transactional` 方法 |
| 质量追溯 | `process/domain/QualityTrace.java` + Mapper + Service | finishTask 时自动落库 |
| 流程 Command | StartTask / StartStep / AiNextStepCallback / FinishTask | 4 个 |
| 流程 VO | StartTaskResult / StartStepResult / AiNextStepResult / FinishTaskResult | 4 个 |

新增 Controller 接口（在 `AssemblyTaskController` 中）：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/process/assembly-task/start` | 开工 |
| POST | `/api/process/assembly-task/{taskId}/steps/{stepId}/start` | 启动步骤 |
| POST | `/api/process/assembly-task/ai-next-step` | AI 自动过站回调 |
| POST | `/api/process/assembly-task/{taskId}/finish` | 手动完成任务 |

### 第四阶段：可靠性与一致性增强

新增以下组件：

| 组件 | 文件 | 说明 |
|------|------|------|
| 流程事件实体 | `process/domain/ProcessEvent.java` | 对应 process_event 表 |
| 事件读服务 | `process/service/ProcessEventService.java` | 只读接口（getByEventId） |
| 事件读实现 | `process/service/impl/ProcessEventServiceImpl.java` | 只读实现 |
| 事件写服务 | `process/service/ProcessEventTxService.java` | 独立事务写接口（8 个方法） |
| 事件写实现 | `process/service/impl/ProcessEventTxServiceImpl.java` | `@Transactional(REQUIRES_NEW)` 确保事件不受主事务回滚影响 |

第四阶段核心改造：

- **process_event 事件表** — 追踪每个关键事件（AI_PASS_RECEIVED / STEP_FINISHED / TASK_COMPLETED / CALLBACK_REJECTED / VISION_SERVICE_ERROR），含 event_id 唯一索引
- **eventId 幂等** — handleAiNextStep 使用 effectiveEventId（优先用回调 eventId，旧格式自动生成 LEGACY 前缀 ID），SUCCESS 或 PROCESSING 状态幂等返回不重复执行，FAILED 状态自动重试
- **派生 eventId** — AI_PASS_RECEIVED / STEP_FINISHED / TASK_COMPLETED 三种事件通过不同后缀避免 unique key 冲突
- **上下文校验** — taskId-deviceSn 匹配校验、task.status 校验、stepLog 归属校验、stepId 匹配校验；不符合条件记录 CALLBACK_REJECTED 后抛异常
- **REQUIRES_NEW 事务隔离** — 所有事件写入通过 ProcessEventTxService 独立事务，主事务回滚不影响事件落库
- **VisionGateway 错误记录** — PythonVisionGateway 调用异常时通过 ProcessEventTxService.recordError() 记录 VISION_SERVICE_ERROR 事件

### 第五阶段：并发控制与异常补偿

新增以下组件：

| 组件 | 文件 | 说明 |
|------|------|------|
| 设备锁接口 | `infrastructure/lock/DeviceLock.java` | 设备维度锁接口，预留 Redis 实现 |
| 设备锁本地实现 | `infrastructure/lock/LocalDeviceLock.java` | ConcurrentHashMap + ReentrantLock 本地锁 |
| 视觉重试控制器 | `process/controller/VisionRetryController.java` | 失败事件查询 + 手动重试接口 |
| 步骤超时调度器 | `process/schedule/StepTimeoutCheckScheduler.java` | 定时扫描 RUNNING 步骤，超时记录事件 |

第五阶段核心改造：

- **设备维度并发锁** — `LocalDeviceLock` 保护同一 deviceSn 的 startTask / startStep / handleAiNextStep / finishTask 不被并发执行。锁逻辑在 `SopExecutionServiceImpl` 中（提取 doXxx 私有方法，加锁在 public 方法中），不在 Controller
- **AssemblyTask 乐观锁** — `AssemblyTask.version` 字段 + `@Version` 注解 + `OptimisticLockerInnerInterceptor`。每次 `updateById` 自动 WHERE version=? AND SET version+1，0 行更新抛 `MybatisPlusException` → 全局异常处理器返回 `OPTIMISTIC_LOCK_CONFLICT(20014)`
- **VisionGateway 失败事件查询** — `ProcessEventService` 新增 `listFailedVisionErrors(deviceSn)` 和 `listAllFailedVisionErrors()`，支持按设备查询最近 10 条 / 全局查询最近 20 条 `VISION_SERVICE_ERROR`
- **VisionGateway 手动重试** — `VisionGateway.retryLastFailed(deviceSn)` 读取最近一次失败事件的 payload，解析操作类型（step-config / start-record / stop-record），尝试重新调用。若 payload 缺失或无法解析则返回 `RETRY_NOT_POSSIBLE`。`VisionRetryController` 提供 `GET /api/process/vision/failed-events/{deviceSn}` 和 `POST /api/process/vision/retry/{deviceSn}`
- **步骤超时检测** — `@EnableScheduling` + `StepTimeoutCheckScheduler`，每隔 `sop.timeout.check-interval`（默认 60s）扫描 `status=RUNNING` 的 `AssemblyStepLog`，超过 `SopStep.standardDuration × sop.timeout.step-multiplier`（默认 2.0）后记录 `STEP_TIMEOUT` 事件。**不修改任务/步骤状态**。`eventId = STEP_TIMEOUT:{stepLogId}` 保证不重复写入

**锁机制说明：**

- 当前使用 `LocalDeviceLock`（`ConcurrentHashMap<String, ReentrantLock>`），**单机有效**，仅限本地开发/单实例部署
- `DeviceLock` 接口已预留 `tryLock(deviceSn)` / `unlock(deviceSn)` 方法签名，后续接入 Redis 时只需新增 `RedisDeviceLock` 实现
- 锁 key 格式：`lock:sop:device:{deviceSn}`
- startStep / finishTask 先从 DB 查询 task 获取 deviceSn 再加锁，避免锁范围不当

**乐观锁说明：**

- `assembly_task.version` 字段初始值为 0，每次 `updateById` 自动 +1
- MyBatis-Plus `OptimisticLockerInnerInterceptor` 自动在 UPDATE SET 中追加 `version = version + 1`，WHERE 中追加 `version = 原值`
- `SopExecutionServiceImpl` 中三个 `updateById(task)` 调用点均自动受保护，无需手动改 SQL

**视觉失败补偿说明：**

- `PythonVisionGateway` 调用异常时通过 `recordVisionError()` 保存 deviceSn + errorMsg + 操作类型（errorType）+ 请求载荷（payload JSON）
- 有载荷时可自动重试；无载荷时返回 `RETRY_NOT_POSSIBLE`
- `VisionRetryController` 仅提供手动触发入口，不做自动重试调度

### 第六阶段：RabbitMQ 事件驱动改造与异步补偿增强

新增以下组件：

| 组件 | 文件 | 说明 |
|------|------|------|
| RabbitMQ 拓扑配置 | `config/RabbitMqConfig.java` | Exchange / Queue / Binding + Jackson2JsonMessageConverter |
| 消息模型 | `mq/message/SopProcessEventMessage.java` | messageId / eventId / eventType / routingKey / taskId / deviceSn / payload / retryCount |
| 发布器接口 | `mq/publisher/SopEventPublisher.java` | 统一发布接口 |
| 发布器实现 | `mq/publisher/RabbitSopEventPublisher.java` | RabbitMQ 发布，enabled=false 跳过，失败记录 MQ_PUBLISH_FAILED |
| 消息日志实体 | `mq/domain/MqMessageLog.java` | messageId + consumerName 唯一索引 |
| 消息日志 Mapper | `mq/mapper/MqMessageLogMapper.java` | MyBatis-Plus Mapper |
| 消息日志服务 | `mq/service/MqMessageLogService.java` + impl | 消费幂等（PROCESSING / SUCCESS / FAILED） |
| 过程事件消费者 | `mq/consumer/SopProcessEventConsumer.java` | 消费 sop.process.event.queue，记录日志 |
| 视觉重试消费者 | `mq/consumer/VisionRetryConsumer.java` | 消费 sop.vision.retry.queue，调用 retryLastFailed |
| 死信消费者 | `mq/consumer/DeadLetterConsumer.java` | 消费 sop.dead.letter.queue，记录死信 |

第六阶段核心改造：

- **MQ 发布集成** — `ProcessEventTxServiceImpl` 在所有事件落库后调用 `publishEvent()`，发布 9 种事件类型到 RabbitMQ。发布失败仅记录 `MQ_PUBLISH_FAILED` 事件，不影响主流程
- **MQ 开关** — `sop.mq.enabled=true/false`。`false` 时所有 MQ Bean 通过 `@ConditionalOnProperty` 不创建，`SopEventPublisher` 注入使用 `@Autowired(required=false)`，publish 方法 null-safe
- **消费幂等** — `mq_message_log` 表 `UNIQUE KEY(message_id, consumer_name)`，消费前查表：SUCCESS → 直接 ack；不存在 → insert PROCESSING → 处理 → markSuccess
- **视觉异步重试** — `VISION_SERVICE_ERROR` 事件落库时自动发布 `routingKey=sop.vision.retry` 到 `sop.vision.retry.queue`，`VisionRetryConsumer` 异步消费后调用 `VisionGateway.retryLastFailed()`
- **死信队列** — 消费者处理失败 nack/requeue 超过 MAX_RETRY=3 次 → `basicReject(requeue=false)` → 投递到 `sop.dlx.exchange` → `sop.dead.letter.queue` → `DeadLetterConsumer` 记录日志

**RabbitMQ 拓扑设计：**

```
sop.process.exchange (topic)
  ├─ sop.event.# → sop.process.event.queue ──→ SopProcessEventConsumer
  └─ sop.vision.retry → sop.vision.retry.queue → VisionRetryConsumer
                              ↓ (失败3次)
                       sop.dlx.exchange (direct)
                         └─ # → sop.dead.letter.queue → DeadLetterConsumer
```

**Routing Key 设计：**

| eventType | routingKey |
|-----------|------------|
| TASK_STARTED | `sop.event.task.started` |
| STEP_STARTED | `sop.event.step.started` |
| AI_PASS_RECEIVED | `sop.event.ai.pass` |
| STEP_FINISHED | `sop.event.step.finished` |
| TASK_COMPLETED | `sop.event.task.completed` |
| QUALITY_TRACE_CREATED | `sop.event.task.completed` |
| VISION_SERVICE_ERROR | `sop.event.vision.error` |
| STEP_TIMEOUT | `sop.event.step.timeout` |
| CALLBACK_REJECTED | `sop.event.ai.pass` |
| VISION_SERVICE_ERROR 重试 | `sop.vision.retry`（独立消息） |

**消息模型说明：**

`SopProcessEventMessage` 包含：messageId（`MSG:{eventId}` 或 `MSG:{eventType}:{sn}:{taskId}:{stepRunId}:{timestamp}`）、eventId、eventType、routingKey、taskId、deviceSn、stepId、stepRunId、payload（JSON）、occurredTime、retryCount。messageId 用于消费者幂等，由 `RabbitSopEventPublisher` 自动生成。

---

## 4. 当前核心包结构

```
com.sop
├── SopApplication.java              # 启动类（@EnableScheduling）
├── common
│   ├── exception
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   ├── page
│   │   └── PageResult.java
│   └── result
│       └── ApiResult.java
├── config
│   ├── MyBatisPlusConfig.java       # MP 自动填充 + 分页插件 + 乐观锁拦截器
│   ├── OpenApiConfig.java           # Knife4j 文档
│   └── RabbitMqConfig.java          # RabbitMQ 拓扑（Exchange/Queue/Binding）
├── infrastructure
│   └── lock
│       ├── DeviceLock.java          # 设备锁接口
│       └── LocalDeviceLock.java     # 本地 ConcurrentHashMap 锁实现
├── integration
│   └── vision
│       ├── VisionGateway.java       # 视觉服务接口（含 retryLastFailed）
│       ├── PythonVisionGateway.java # HTTP 实现（含 enabled 开关 + 错误事件 + 重试）
│       ├── VisionProperties.java    # 配置绑定
│       ├── VisionResult.java        # 通用返回
│       ├── VisionHealthStatus.java  # 健康检查
│       ├── VisionStepConfigCommand.java
│       ├── StartRecordCommand.java
│       └── StopRecordCommand.java
├── mq
│   ├── message
│   │   └── SopProcessEventMessage.java  # MQ 消息 DTO
│   ├── publisher
│   │   ├── SopEventPublisher.java       # 发布器接口
│   │   └── RabbitSopEventPublisher.java # RabbitMQ 发布器
│   ├── consumer
│   │   ├── SopProcessEventConsumer.java # 过程事件消费者
│   │   ├── VisionRetryConsumer.java     # 视觉重试消费者
│   │   └── DeadLetterConsumer.java      # 死信消费者
│   ├── domain
│   │   └── MqMessageLog.java            # 消息消费日志实体
│   ├── mapper
│   │   └── MqMessageLogMapper.java
│   └── service
│       ├── MqMessageLogService.java
│       └── impl/MqMessageLogServiceImpl.java
├── workflow
│   └── AssemblyStateMachine.java    # 状态机（状态常量 + 流转校验）
├── process
│   ├── command                      # 操作命令（10 个）
│   ├── controller                   # REST 接口（6 个，含 VisionRetryController）
│   ├── domain                       # 实体（7 个，含 QualityTrace + ProcessEvent）
│   ├── dto                          # CRUD 传输对象（5 个）
│   ├── mapper                       # MyBatis-Plus Mapper（7 个）
│   ├── schedule
│   │   └── StepTimeoutCheckScheduler.java  # 步骤超时检测定时任务
│   ├── service                      # 服务接口（11 个）
│   ├── service/impl                 # 服务实现（11 个）
│   └── vo                           # 视图对象（9 个）
└── event                            # 预留（空）
```

Java 文件总数：**83**

---

## 5. 核心业务对象

### 5.1 实体字段速查

**AssemblyTask（装配任务）**

| 字段 | 类型 | 说明 |
|------|------|------|
| taskId | Long (PK) | 自增主键 |
| modelCode | String | 型号编码 |
| deviceSn | String | 设备编码 |
| workerId | Long | 操作工 ID |
| workerName | String | 操作工姓名 |
| currentStepIndex | Integer | 当前步骤序号（对应 SopStep.stepOrder） |
| status | String | RUNNING / EXCEPTION / COMPLETED / WAREHOUSED |
| assemblyRound | Integer | 装配轮次 |
| startTime | LocalDateTime | 任务开始时间 |
| finishTime | LocalDateTime | 任务完成时间 |
| version | Integer | 乐观锁版本号（@Version，每次更新 +1） |
| deleted | Integer | 逻辑删除（@TableLogic） |
| createTime / updateTime | LocalDateTime | MP 自动填充 |

**AssemblyStepLog（装配步骤日志）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 自增主键 |
| taskId | Long | 关联任务 ID |
| stepId | Long | 关联步骤 ID |
| serialNumber | String | 设备编号（等同于 deviceSn） |
| modelCode | String | 型号编码 |
| stepNo | Integer | 步骤序号 |
| status | String | WAITING / RUNNING / AI_PASSED / MANUAL_PASSED / FAILED / SKIPPED |
| passType | String | AI_PASS / MANUAL_PASS |
| duration | Long | 步骤耗时秒 |
| startTime / endTime | LocalDateTime | |
| videoUrl | String | 录像路径 |
| processStage | String | 作业阶段 |
| workerName | String | 工人姓名 |
| assemblyRound | Integer | 装配轮次 |

**SopStep（SOP 步骤）**

| 字段 | 类型 | 说明 |
|------|------|------|
| stepId | Long (PK) | 自增主键 |
| modelCode | String | 型号编码 |
| stepOrder | Integer | 步骤顺序（与 currentStepIndex 直接等值比较） |
| processStage | String | 作业阶段 |
| stepTitle | String | 步骤标题 |
| stepDesc | String | 操作指导 |
| detectTarget | String | YOLO 检测目标 JSON |
| targetLabel | String | 检测目标标签 |
| nextTargetLabel | String | 下一步检测目标标签 |
| standardDuration | Integer | 标准工时秒 |
| imageUrl | String | 示意图 |
| roiConfig | String | ROI 配置 JSON |

**QualityTrace（质量追溯）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 自增主键 |
| taskId | Long | 关联任务 ID |
| deviceSn | String | 设备编码 |
| modelCode | String | 型号编码 |
| workerId | Long | 操作工 ID |
| totalSteps | Integer | 总步骤数 |
| passedSteps | Integer | AI 通过数 |
| manualSteps | Integer | 手动通过数 |
| exceptionCount | Integer | 异常步骤数 |
| startTime / finishTime | LocalDateTime | |
| totalDuration | Long | 总耗时秒 |
| finalResult | String | PASS / FAIL / MANUAL |

**ProcessEvent（流程事件）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 自增主键 |
| eventId | String (UNIQUE) | 外部事件ID（回调 eventId 或自动生成 LEGACY 前缀 ID） |
| taskId | Long | 关联任务 ID |
| deviceSn | String | 设备编码 |
| stepId | Long | 步骤 ID |
| stepRunId | Long | 步骤日志 ID |
| eventType | String | 事件类型（见 §6） |
| status | String | RECEIVED / PROCESSING / SUCCESS / FAILED / IGNORED |
| payload | String | 原始载荷（JSON） |
| result | String | 处理结果简述 |
| errorMessage | String | 异常信息（status=FAILED 时） |
| processedTime | LocalDateTime | 处理完成时间 |
| duplicateCount | Integer | 重复回调次数 |
| lastReceivedTime | LocalDateTime | 最后一次收到重复回调的时间 |
| retryCount | Integer | MQ 重试次数 |
| maxRetry | Integer | 最大重试次数（默认 3） |
| nextRetryTime | LocalDateTime | 下次重试时间 |
| createTime / updateTime | LocalDateTime | MP 自动填充 |

### 5.2 状态机常量（AssemblyStateMachine）

**任务状态：** `WAITING_START` → `RUNNING` → `COMPLETED` 或 `EXCEPTION` → `WAREHOUSED`

**步骤状态：** `WAITING` → `RUNNING` → `AI_PASSED` / `MANUAL_PASSED` / `FAILED`；另 `SKIPPED`

**事件：** `START_TASK` / `START_STEP` / `AI_PASS` / `MANUAL_PASS` / `STEP_EXCEPTION` / `FINISH_TASK`

所有状态变更必须经过 `AssemblyStateMachine` 校验，非法流转抛 `BusinessException`。

---

## 6. 核心事件类型

### 6.1 ProcessEvent 事件类型一览

| eventType | 触发时机 | eventId 来源 | status | 说明 |
|-----------|---------|-------------|--------|------|
| `AI_PASS_RECEIVED` | AI 回调进入 handleAiNextStep | 回调 eventId 或 `LEGACY_AI_PASS:{sn}:{taskId}:{runId}:AUTO_NEXT` | PROCESSING → SUCCESS/FAILED | 主幂等键 |
| `STEP_FINISHED` | 步骤 AI_PASSED 落库后 | `{effectiveEventId}:STEP_FINISHED:{stepLogId}` | SUCCESS | 派生事件 |
| `TASK_COMPLETED` | 最后一步过站后 | `{effectiveEventId}:TASK_COMPLETED:{taskId}` | SUCCESS | 派生事件 |
| `CALLBACK_REJECTED` | 校验失败（action/status 无效、上下文不匹配、无 RUNNING 步骤） | `CALLBACK_REJECTED:{sn}:{taskId}:{timestamp}` | IGNORED | 拒绝记录 |
| `VISION_SERVICE_ERROR` | PythonVisionGateway HTTP 调用异常 | `VISION_ERROR:{sn}:{timestamp}` | FAILED | 视觉服务错误 |
| `TASK_STARTED` | 开工成功落库后 | `TASK_STARTED:{taskId}:{sn}` | SUCCESS | 第五阶段新增，MQ 发布 |
| `STEP_STARTED` | 启动步骤成功后 | `STEP_STARTED:{stepRunId}:{taskId}` | SUCCESS | 第五阶段新增，MQ 发布 |
| `QUALITY_TRACE_CREATED` | finishTask 质量追溯落库后 | `QUALITY_TRACE:{taskId}:{timestamp}` | SUCCESS | 第五阶段新增，MQ 发布 |
| `STEP_TIMEOUT` | 定时任务检测到步骤超时 | `STEP_TIMEOUT:{stepLogId}` | FAILED | 第五阶段新增，不修改步骤状态 |
| `MQ_PUBLISH_FAILED` | RabbitMQ 消息发布失败 | `MQ_PUBLISH_FAILED:{messageId}:{timestamp}` | FAILED | 第六阶段新增 |

### 6.2 eventId 设计

```
# 新格式（Python 回调自带 eventId）
eventId = command.eventId                          # AI_PASS_RECEIVED
eventId + ":STEP_FINISHED:" + stepLog.getId()      # STEP_FINISHED
eventId + ":TASK_COMPLETED:" + task.getTaskId()     # TASK_COMPLETED

# 旧格式（Python 回调无 eventId 时自动生成）
LEGACY_AI_PASS:{deviceSn}:{taskId}:{stepRunId}:AUTO_NEXT
```

三种事件使用不同后缀，避免 `uk_event_id` 唯一索引冲突。

---

## 7. AI 回调幂等处理流程

```
handleAiNextStep(command)

  ↓
  ═══ 阶段1：校验 + 定位 ═══
  ├─ action != "AUTO_NEXT" → recordRejected + throw BAD_REQUEST
  ├─ status != "0"         → recordRejected + throw BAD_REQUEST
  ├─ 定位任务（taskId 精确查 或 deviceSn+RUNNING 模糊查）
  ├─ 上下文校验：taskId-deviceSn 匹配、task.status==RUNNING
  ├─ 定位步骤日志（stepRunId 精确查 或 taskId+RUNNING 模糊查）
  └─ 上下文校验：stepLog.status==RUNNING, stepLog.taskId==task.taskId, stepId 匹配

  ↓
  ═══ 阶段2：幂等检查 ═══
  ├─ 生成 effectiveEventId（优先 command.eventId，否则 LEGACY 格式）
  ├─ getByEventId(effectiveEventId) 查询已有事件
  │
  ├─ null（新事件）
  │   └─ tryCreateProcessing() → 继续阶段3
  │
  ├─ status == SUCCESS（已成功）
  │   └─ updateDuplicate() → 幂等返回当前任务状态（不推进步骤）
  │
  ├─ status == PROCESSING（处理中）
  │   └─ updateDuplicate() → 幂等返回当前任务状态（不推进步骤）
  │
  └─ status == FAILED（上次失败，重试）
      ├─ markProcessing()  // 重置为 PROCESSING
      └─ updateDuplicate()  // duplicateCount++

  ↓
  ═══ 阶段3：执行过站逻辑 ═══
  ├─ stateMachine.checkAiPassAllowed + checkStepTransition
  ├─ stepLog → AI_PASSED, passType=AI_PASS, endTime=now, duration=计算
  ├─ recordEvent(STEP_FINISHED)  // 派生 eventId, REQUIRES_NEW
  ├─ visionGateway.stopRecord
  ├─ findNextStep(stepOrder > currentStepOrder)
  │
  ├─ 有下一步 → currentStepIndex=nextStep.stepOrder → startNextStep
  └─ 无下一步 → checkTaskTransition(RUNNING, FINISH_TASK) → task=COMPLETED
                 → recordEvent(TASK_COMPLETED) // 派生 eventId, REQUIRES_NEW

  ↓
  ═══ 阶段4：markSuccess ═══
  └─ processEventTxService.markSuccess(effectiveEventId, result)

  ↓
  catch BusinessException → markFailed(effectiveEventId) + throw
```

**关键设计决策：**
- 幂等返回**不抛异常**，正常返回当前任务状态（含 hasNextStep 等），避免 Python 侧误认为回调失败
- FAILED 状态自动重试，而非直接拒绝，提高容错性
- 所有事件写入通过 ProcessEventTxService（REQUIRES_NEW），主事务回滚不影响事件落库
- CALLBACK_REJECTED 在阶段1校验失败时记录，使用独立 eventId（含时间戳）避免冲突

---

## 8. 核心流程接口

### 8.1 开工 — POST /api/process/assembly-task/start

```
请求: StartTaskCommand { deviceSn, workerId, modelCode }
返回: StartTaskResultVO { taskId, deviceSn, modelCode, currentStepNo, status, startTime }

流程:
  1. 查 SopStep 列表 → 空则抛 NO_SOP_STEPS
  2. 查 deviceSn 是否已有 RUNNING 任务 → 有则抛 TASK_RUNNING_EXISTS
  3. 查 Worker.getWorkerName()
  4. 创建 AssemblyTask(RUNNING, currentStepIndex=第一步.stepOrder, startTime=now)
  5. checkTaskTransition(WAITING_START, START_TASK)
  6. insert → 返回 VO
```

### 8.2 启动步骤 — POST /api/process/assembly-task/{taskId}/steps/{stepId}/start

```
请求: 路径参数 taskId + stepId
返回: StartStepResultVO { taskId, stepLogId, stepId, stepNo, stepName, status, targetLabel, nextTargetLabel, videoPath }

流程:
  1. 查任务，校验 status=RUNNING → 否则抛 TASK_NOT_RUNNING
  2. 查步骤，校验 modelCode 匹配、stepOrder==currentStepIndex → 否则抛 STEP_NOT_MATCH
  3. 查找或创建 AssemblyStepLog
  4. checkStepTransition(WAITING/RUNNING, START_STEP)
  5. stepLog 更新为 RUNNING, startTime=now, 清空 passType/duration/endTime
  6. insert 或 updateById
  7. VisionGateway.updateStepConfig(deviceSn, stepId, target, nextTarget, modelCode, historyTargets, time1, time2)
  8. VisionGateway.startRecord(deviceSn, stepId, taskId, stepRunId)
  9. 返回 VO
```

### 8.3 AI 过站回调 — POST /api/process/assembly-task/ai-next-step

```
请求: AiNextStepCallbackCommand { deviceSn, taskId?, stepId?, stepRunId?, action, status, eventId? }
返回: AiNextStepResultVO { taskId, deviceSn, taskStatus, currentStepNo, hasNextStep, nextStepId, nextStepName, completedStepLogId }

流程: 详见 §7 四阶段流程

幂等特性:
  - 相同 effectiveEventId 的重复回调 → 幂等返回（不重复执行过站逻辑）
  - FAILED 事件 → 自动 markProcessing 重试
  - 校验失败 → 记录 CALLBACK_REJECTED 后抛 BusinessException
```

### 8.4 手动完成 — POST /api/process/assembly-task/{taskId}/finish

```
请求: 路径参数 taskId, 可选 body FinishTaskCommand { taskId, reason }
返回: FinishTaskResultVO { taskId, deviceSn, status, finishTime, totalDuration }

流程:
  1. 查任务
  2. checkFinishTaskAllowed + checkTaskTransition(RUNNING, FINISH_TASK)
  3. 查 RUNNING 步骤日志 → 标记为 MANUAL_PASSED
  4. task=COMPLETED, finishTime=now
  5. generateQualityTrace → 统计 totalSteps/passedSteps/manualSteps/exceptionCount → 落库 quality_trace
  6. 返回 VO
```

---

## 9. 第五阶段测试通过项

| # | 测试项 | 验证点 | 状态 |
|---|--------|--------|:---:|
| 1 | 主流程回归（加锁后） | startTask → startStep → ai-next-step × N → COMPLETED | ✅ |
| 2 | 设备锁正常获取 | startTask deviceSn=ZD6-001 → 200 | ✅ |
| 3 | 设备锁并发冲突 | 同一 deviceSn 并发请求，第二个返回 SERVICE_UNAVAILABLE | ✅ |
| 4 | 设备锁释放后可用 | 第一次完成后，同一 deviceSn 可再次操作 | ✅ |
| 5 | AssemblyTask version 写库 | INSERT 后 version=0 | ✅ |
| 6 | updateById version 递增 | AI 过站后 version=1，每次 updateById 自动 +1 | ✅ |
| 7 | 乐观锁冲突 | version 不匹配时返回 OPTIMISTIC_LOCK_CONFLICT(20014) | ✅ |
| 8 | VISION_SERVICE_ERROR 含载荷 | recordVisionError 保存 errorType + payload JSON | ✅ |
| 9 | 按设备查询失败事件 | GET /api/process/vision/failed-events/{deviceSn} | ✅ |
| 10 | 查询所有失败事件 | GET /api/process/vision/failed-events | ✅ |
| 11 | 手动重试成功 | POST /api/process/vision/retry/{deviceSn}→ 重试成功 → markSuccess | ✅ |
| 12 | 不可重试（缺 payload） | payload 缺失 → RETRY_NOT_POSSIBLE | ✅ |
| 13 | 步骤超时检测触发 | standardDuration * 0.01 秒级触发 → STEP_TIMEOUT 记录 | ✅ |
| 14 | 超时事件不重复写入 | eventId 去重 → 第二次扫描无新记录 | ✅ |
| 15 | 超时检测不修改状态 | task/step 状态保持不变 | ✅ |
| 16 | 无 standardDuration 跳过 | step.standardDuration=null → 不触发 | ✅ |

## 10. 第六阶段测试通过项

| # | 测试项 | 验证点 | 状态 |
|---|--------|--------|:---:|
| 1 | sop.mq.enabled=false 主流程正常 | MQ Bean 不创建，主流程 200 | ✅ |
| 2 | sop.mq.enabled=true 事件发布消费 | TASK_STARTED / STEP_FINISHED 等进入队列并被消费 | ✅ |
| 3 | mq_message_log 消费记录 | consumer_name + status=SUCCESS | ✅ |
| 4 | 重复 messageId 幂等 | 第二条直接 ack，不重复处理 | ✅ |
| 5 | VISION_SERVICE_ERROR → vision.retry | recordVisionError 后自动发布 sop.vision.retry 消息 | ✅ |
| 6 | VisionRetryConsumer 消费 | 调用 retryLastFailed → 成功则 markSuccess | ✅ |
| 7 | 视觉重试失败 → 重入 | nack + requeue → retry_count 递增 | ✅ |
| 8 | 超过 MAX_RETRY → 死信 | basicReject(requeue=false) → sop.dead.letter.queue | ✅ |
| 9 | DeadLetterConsumer 记录 | 死信消息日志 + mq_message_log 标记 | ✅ |
| 10 | MQ 发布失败不影响主流程 | RabbitMQ 不可用时主流程仍 200，记录 MQ_PUBLISH_FAILED | ✅ |
| 11 | 主流程回归 | startTask → startStep → ai-next-step × N → COMPLETED + quality_trace | ✅ |

---

## 11. 已知限制

| 限制 | 影响 | 说明 |
|------|------|------|
| LocalDeviceLock 仅单机有效 | 多实例部署时并发锁不生效 | 已预留 DeviceLock 接口，后续接入 RedisDeviceLock |
| 视觉异步重试仅依赖 MQ | MQ 服务不可用时无降级方案 | 手动重试接口仍可用 |
| MQ 消息无过期清理 | `mq_message_log` 表持续增长 | 后续加入归档/清理策略 |
| 无操作审计 AOP | 无法追踪接口调用者/参数/时间 | 后续 |
| VisionGateway 不可用时 startStep 报错 | `enabled=false` 可跳过，但联调时需 Python 在线 | 当前已通过 enabled 开关缓解 |
| findHistoryPassedTargets N+1 查询 | 每个已完步骤做一次 sopStep 查询 | 后续优化 |
| `currentStepIndex` 命名含 "Index" 但实际存储 stepOrder | 注释已更新说明 | 后续统一重命名 |
| ALTER TABLE 在已执行过的旧库上会失败 | 新库无问题 | 后续合并到 CREATE TABLE |
| ProcessEvent / mq_message_log 无过期清理 | 事件表持续增长 | 后续 |
| `enabled=false` 时 VisionResult.getData() 为 null | startStep 返回 videoPath=null | 可接受 |

---

## 12. 下一阶段计划

### 12.1 RedisDeviceLock

基于 `DeviceLock` 接口实现 `RedisDeviceLock`，使用 Redis 分布式锁（key=`lock:sop:device:{deviceSn}`），支持多实例部署。

### 12.2 操作审计 AOP

在 Controller 层增加 AOP 切面，记录每个接口的调用者、参数、时间、结果。不阻塞业务流程。

### 12.3 链路追踪

引入 traceId，贯穿 Controller → Service → MQ → Consumer，便于跨系统日志关联。

### 12.4 Docker Compose 部署

提供 `docker-compose.yml`，包含 MySQL + RabbitMQ + sop-process-platform，支持一键启动本地开发环境。

### 12.5 后续优化

- `findHistoryPassedTargets` 批量查询优化
- CREATE TABLE 语句直接包含所有字段（合并 ALTER）
- `currentStepIndex` 重命名为 `currentStepNo`
- status 枚举统一为字符串常量
- ProcessEvent / mq_message_log 归档/清理策略
- Actuator 健康检查（MySQL / RabbitMQ / Vision）

---

## 13. 禁止事项

| 禁止 | 原因 |
|------|------|
| 引入 `com.ruoyi` 包 | 项目已独立，不依赖 RuoYi |
| 使用 `AjaxResult` / `BaseController` / `TableDataInfo` / `BaseEntity` / `SecurityUtils` / `startPage` / `getDataTable` | RuoYi 残留 |
| 重写 Python 视觉服务 | Python 是外部系统，Java 只通过 VisionGateway 调用 |
| 重写前端 | 不在当前项目范围 |
| 引入复杂微服务框架 | 当前是单体 Spring Boot，保持简单 |
| 引入第三方状态机框架 | 已有轻量 AssemblyStateMachine |
| 大范围重构第二阶段 CRUD | 只允许小范围补字段补方法 |
| 实现权限系统 | 不在当前范围 |
| 引入 MQ / 替代 RabbitMQ | 已选型 RabbitMQ |
| 引入 Spring Cloud / Nacos / Seata | 不在当前范围 |

---

## 14. 本地启动

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sop_process DEFAULT CHARSET utf8mb4"

# 2. 执行建表 SQL
mysql -u root -p sop_process < src/main/resources/sql/sop_process_ddl.sql

# 3. （可选）启动 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 4. application.yml 配置说明
#    - 数据库密码：修改 spring.datasource.password
#    - Python 视觉服务：vision.service.enabled=false（本地测试）
#    - MQ 开关：sop.mq.enabled=false（本地无 RabbitMQ 时）
#      若禁用 MQ，需同时注释 spring.rabbitmq 配置段
#    - 步骤超时：sop.timeout.check-interval=60000, step-multiplier=2.0

# 5. 启动
cd sop-process-platform
mvn clean compile
mvn spring-boot:run

# 6. API 文档
# http://localhost:8088/doc.html

# 7. RabbitMQ 管理界面（如已启动）
# http://localhost:15672 (guest/guest)
```
