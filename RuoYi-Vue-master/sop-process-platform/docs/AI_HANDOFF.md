# AI Handoff — sop-process-platform

> 最后更新：2026-05-02
> 第三阶段已通过本地接口测试

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
| Spring Data Redis | 预留（未使用） |
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

---

## 4. 当前核心包结构

```
com.sop
├── SopApplication.java              # 启动类
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
│   ├── MyBatisPlusConfig.java       # MP 自动填充 + 分页插件
│   └── OpenApiConfig.java           # Knife4j 文档
├── integration
│   └── vision
│       ├── VisionGateway.java       # 视觉服务接口
│       ├── PythonVisionGateway.java # HTTP 实现
│       ├── VisionProperties.java    # 配置绑定
│       ├── VisionResult.java        # 通用返回
│       ├── VisionHealthStatus.java  # 健康检查
│       ├── VisionStepConfigCommand.java
│       ├── StartRecordCommand.java
│       └── StopRecordCommand.java
├── workflow
│   └── AssemblyStateMachine.java    # 状态机（状态常量 + 流转校验）
├── process
│   ├── command                      # 操作命令（9 个，含 CRUD 5 + 流程 4）
│   ├── controller                   # REST 接口（5 个）
│   ├── domain                       # 实体（6 个，含 QualityTrace）
│   ├── dto                          # CRUD 传输对象（5 个）
│   ├── mapper                       # MyBatis-Plus Mapper（6 个）
│   ├── service                      # 服务接口（7 个，含 SopExecution + QualityTrace）
│   ├── service/impl                 # 服务实现（7 个）
│   └── vo                           # 视图对象（9 个，含 CRUD 5 + 流程 4）
├── event                            # 预留（空）
└── infrastructure                   # 预留（空）
```

Java 文件总数：**65**

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
| startTime | LocalDateTime | 任务开始时间（第三阶段新增） |
| finishTime | LocalDateTime | 任务完成时间（第三阶段新增） |
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
| stepNo | Integer | 步骤序号（第三阶段新增） |
| status | String | WAITING / RUNNING / AI_PASSED / MANUAL_PASSED / FAILED / SKIPPED |
| passType | String | AI_PASS / MANUAL_PASS（第三阶段新增） |
| duration | Long | 步骤耗时秒（第三阶段新增） |
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
| targetLabel | String | 检测目标标签（第三阶段新增） |
| nextTargetLabel | String | 下一步检测目标标签（第三阶段新增） |
| standardDuration | Integer | 标准工时秒（第三阶段新增） |
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

### 5.2 状态机常量（AssemblyStateMachine）

**任务状态：** `WAITING_START` → `RUNNING` → `COMPLETED` 或 `EXCEPTION` → `WAREHOUSED`

**步骤状态：** `WAITING` → `RUNNING` → `AI_PASSED` / `MANUAL_PASSED` / `FAILED`；另 `SKIPPED`

**事件：** `START_TASK` / `START_STEP` / `AI_PASS` / `MANUAL_PASS` / `STEP_EXCEPTION` / `FINISH_TASK`

所有状态变更必须经过 `AssemblyStateMachine` 校验，非法流转抛 `BusinessException`。

---

## 6. 核心流程接口

### 6.1 开工 — POST /api/process/assembly-task/start

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

### 6.2 启动步骤 — POST /api/process/assembly-task/{taskId}/steps/{stepId}/start

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

### 6.3 AI 过站回调 — POST /api/process/assembly-task/ai-next-step

```
请求: AiNextStepCallbackCommand { deviceSn, taskId?, stepId?, stepRunId?, action, status }
返回: AiNextStepResultVO { taskId, deviceSn, taskStatus, currentStepNo, hasNextStep, nextStepId, nextStepName }

流程:
  0. 校验 action=="AUTO_NEXT" && status=="0" → 否则抛 BAD_REQUEST
  1. 定位任务（taskId 或 deviceSn+RUNNING）
  2. 定位 RUNNING 步骤日志（stepRunId 或 taskId+RUNNING）
  3. checkAiPassAllowed + checkStepTransition(RUNNING, AI_PASS)
  4. 步骤 → AI_PASSED, passType=AI_PASS, endTime=now, duration=计算
  5. VisionGateway.stopRecord
  6. 查下一步骤（findNextStep: stepOrder > currentStepOrder）
  7. 有下一步 → 更新 currentStepIndex → 自动启动下一步（创建新日志 + 下发配置 + 启动录像）
  8. 无下一步 → checkTaskTransition(RUNNING, FINISH_TASK) → task=COMPLETED, finishTime=now
  9. 返回 VO
```

### 6.4 手动完成 — POST /api/process/assembly-task/{taskId}/finish

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

## 7. 本地测试通过的主流程

以 deviceSn=ZD6-001, workerId=1, modelCode=ZD6, 3 个步骤为例：

```
1. startTask
   → taskId=1, status=RUNNING, currentStepNo=0

2. startStep (taskId=1, stepId=1)
   → stepLogId=1, status=RUNNING, targetLabel=motor, nextTargetLabel=gear

3. ai-next-step × 1
   → currentStepNo=1, hasNextStep=true, nextStepName="安装传动齿轮"

4. ai-next-step × 2
   → currentStepNo=2, hasNextStep=true, nextStepName="紧固底座螺丝"

5. ai-next-step × 3
   → hasNextStep=false, taskStatus=COMPLETED

6. check DB: assembly_task.status=COMPLETED, 3×assembly_step_log.status=AI_PASSED
```

或手动完成路径：

```
1. startTask → 2. startStep → 3. finishTask
   → taskStatus=COMPLETED, quality_trace 落库, finalResult=MANUAL
```

---

## 8. 已知限制

| 限制 | 影响 | 计划阶段 |
|------|------|:---:|
| 无 eventId 幂等 | Python 重复回调可能导致步骤被多次 AI_PASSED | 后续 |
| 无 Redis 设备锁 | 并发 startTask 可能绕过同设备检查 | 后续 |
| 无 ProcessEvent 事件表 | 无法追踪完整操作历史 | 后续 |
| 无异常补偿 | VisionGateway 调用失败直接回滚事务，无重试 | 后续 |
| 无操作审计 | 无操作日志/审计日志 | 后续 |
| VisionGateway 不可用时 startStep 报错 | `enabled=false` 可跳过，但联调时需 Python 在线 | 当前已通过 enabled 开关缓解 |
| findHistoryPassedTargets N+1 查询 | 每个已完步骤做一次 sopStep 查询 | 后续优化 |
| `currentStepIndex` 命名含 "Index" 但实际存储 stepOrder | 注释已更新说明，但命名未改 | 后续统一重命名 |
| 状态值从数字("0","1")迁移到语义字符串("RUNNING") | CRUD 分页过滤仍用旧注释 | 后续统一 |
| `enabled=false` 时 VisionResult.getData() 为 null | startStep 返回 videoPath=null | 可接受，本地测试不需要录像路径 |
| ALTER TABLE 在已执行过的旧库上会失败 | 新库无问题 | 后续合并到 CREATE TABLE |

---

## 9. 下一阶段建议

### 9.1 eventId 幂等

在 `assembly_step_log` 或新表 `process_event` 中记录 Python 回调的 `eventId`。`handleAiNextStep` 处理前先检查 eventId 是否已处理，避免重复回调导致步骤重复完成。

### 9.2 Redis 设备锁

`startTask` 中同设备检查存在 race condition。引入 Redis 分布式锁（key=`sop:device:{deviceSn}`），锁内执行创建逻辑。

### 9.3 ProcessEvent 事件表

新建 `process_event` 表，记录每个状态变更事件（taskId, eventType, fromStatus, toStatus, operator, eventTime）。在 `AssemblyStateMachine` 的方法中统一发布事件。

### 9.4 异常补偿

VisionGateway 调用失败时，不直接回滚已持久化的步骤日志。改为：
- 步骤标记为 FAILED
- 记录异常事件
- 后续提供重试接口

### 9.5 操作审计

在 Controller 层或 AOP 层记录每个接口的调用者、参数、时间、结果。不阻塞业务流程。

### 9.6 其他优化

- `findHistoryPassedTargets` 批量查询优化
- CREATE TABLE 语句直接包含所有字段
- `currentStepIndex` 重命名为 `currentStepNo`
- status 枚举统一为字符串常量

---

## 10. 禁止事项

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

---

## 11. 本地启动

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sop_process DEFAULT CHARSET utf8mb4"

# 2. 执行建表 SQL
mysql -u root -p sop_process < src/main/resources/sql/sop_process_ddl.sql

# 3. application.yml 中确认数据库密码、vision.service.enabled=false

# 4. 启动
cd sop-process-platform
mvn clean compile
mvn spring-boot:run

# 5. API 文档
# http://localhost:8088/doc.html

# 6. 插入测试数据后调用接口
# 详见上一轮的测试清单
```
