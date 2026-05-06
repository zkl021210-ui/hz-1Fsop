-- ============================================================
-- SOP 流程平台 - 核心业务表 DDL
-- 数据库：sop_process（需先手动创建）
-- ============================================================

-- 1. 产品型号表
DROP TABLE IF EXISTS `product_model`;
CREATE TABLE `product_model` (
    `model_id`     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '型号ID',
    `model_code`   VARCHAR(64)   NOT NULL COMMENT '型号编码',
    `model_name`   VARCHAR(128)  NOT NULL COMMENT '型号名称',
    `description`  VARCHAR(512)  DEFAULT NULL COMMENT '型号描述/备注',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常 1-删除）',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`model_id`),
    UNIQUE KEY `uk_model_code` (`model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品型号表';

-- 2. SOP 步骤表
DROP TABLE IF EXISTS `sop_step`;
CREATE TABLE `sop_step` (
    `step_id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
    `model_code`      VARCHAR(64)   NOT NULL COMMENT '关联型号编码',
    `process_stage`   VARCHAR(64)   NOT NULL COMMENT '作业阶段',
    `step_order`      INT           NOT NULL COMMENT '步骤顺序',
    `step_title`      VARCHAR(256)  NOT NULL COMMENT '步骤标题',
    `step_desc`       TEXT          DEFAULT NULL COMMENT '操作指导描述',
    `image_url`       VARCHAR(512)  DEFAULT NULL COMMENT '示意图路径',
    `detect_target`   JSON          DEFAULT NULL COMMENT 'YOLO检测目标（JSON数组）',
    `roi_config`      JSON          DEFAULT NULL COMMENT 'ROI相对坐标配置（JSON）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常 1-删除）',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`step_id`),
    KEY `idx_model_code` (`model_code`),
    KEY `idx_process_stage` (`process_stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOP步骤表';

-- 3. 装配任务表
DROP TABLE IF EXISTS `assembly_task`;
CREATE TABLE `assembly_task` (
    `task_id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `model_code`         VARCHAR(64)   NOT NULL COMMENT '关联型号编码',
    `device_sn`          VARCHAR(64)   NOT NULL COMMENT '设备编码（转辙机编码）',
    `worker_id`          BIGINT        NOT NULL COMMENT '操作工ID',
    `worker_name`        VARCHAR(64)   DEFAULT NULL COMMENT '操作工姓名',
    `current_step_index` INT           DEFAULT 0 COMMENT '当前步骤索引（从0开始）',
    `status`             VARCHAR(32)   NOT NULL DEFAULT '0' COMMENT '任务状态（0-进行中 1-暂停 2-已完成 3-异常）',
    `assembly_round`     INT           DEFAULT 1 COMMENT '装配轮次',
    `deleted`            TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常 1-删除）',
    `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`task_id`),
    KEY `idx_model_code` (`model_code`),
    KEY `idx_device_sn` (`device_sn`),
    KEY `idx_worker_id` (`worker_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装配任务表';

-- 4. 装配步骤日志表
DROP TABLE IF EXISTS `assembly_step_log`;
CREATE TABLE `assembly_step_log` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `task_id`         BIGINT        NOT NULL COMMENT '关联任务ID',
    `step_id`         BIGINT        NOT NULL COMMENT '关联步骤ID',
    `serial_number`   VARCHAR(64)   NOT NULL COMMENT '设备编号',
    `model_code`      VARCHAR(64)   NOT NULL COMMENT '型号编码',
    `assembly_round`  INT           DEFAULT 1 COMMENT '装配轮次',
    `process_stage`   VARCHAR(64)   DEFAULT NULL COMMENT '作业阶段',
    `worker_name`     VARCHAR(64)   DEFAULT NULL COMMENT '工人姓名',
    `start_time`      DATETIME      DEFAULT NULL COMMENT '开始时间',
    `end_time`        DATETIME      DEFAULT NULL COMMENT '结束时间',
    `status`          VARCHAR(32)   NOT NULL DEFAULT '0' COMMENT '步骤状态（0-待执行 1-执行中 2-已完成 3-异常）',
    `video_url`       VARCHAR(512)  DEFAULT NULL COMMENT '视频录制地址',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常 1-删除）',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_step_id` (`step_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装配步骤日志表';

-- 5. 工人表
DROP TABLE IF EXISTS `worker`;
CREATE TABLE `worker` (
    `worker_id`   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '工人ID',
    `worker_name` VARCHAR(64)   NOT NULL COMMENT '姓名',
    `worker_code` VARCHAR(64)   NOT NULL COMMENT '工号',
    `team_name`   VARCHAR(128)  DEFAULT NULL COMMENT '班组',
    `status`      VARCHAR(32)   NOT NULL DEFAULT '0' COMMENT '状态（0-正常 1-停用）',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常 1-删除）',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`worker_id`),
    UNIQUE KEY `uk_worker_code` (`worker_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工人表';

-- 6. 质量追溯表
DROP TABLE IF EXISTS `quality_trace`;
CREATE TABLE `quality_trace` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '追溯ID',
    `task_id`         BIGINT        NOT NULL COMMENT '关联任务ID',
    `device_sn`       VARCHAR(64)   NOT NULL COMMENT '设备编码',
    `model_code`      VARCHAR(64)   NOT NULL COMMENT '型号编码',
    `worker_id`       BIGINT        DEFAULT NULL COMMENT '操作工ID',
    `total_steps`     INT           DEFAULT 0 COMMENT '总步骤数',
    `passed_steps`    INT           DEFAULT 0 COMMENT 'AI通过步骤数',
    `manual_steps`    INT           DEFAULT 0 COMMENT '手动通过步骤数',
    `exception_count` INT           DEFAULT 0 COMMENT '异常步骤数',
    `start_time`      DATETIME      DEFAULT NULL COMMENT '开始时间',
    `finish_time`     DATETIME      DEFAULT NULL COMMENT '完成时间',
    `total_duration`  BIGINT        DEFAULT 0 COMMENT '总耗时（秒）',
    `final_result`    VARCHAR(32)   DEFAULT NULL COMMENT '最终结果（PASS / FAIL / MANUAL）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常 1-删除）',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量追溯表';

-- ============================================================
-- 第三阶段新增字段（新库直接执行，旧库请手动逐条 ALTER 添加）
-- ============================================================
-- 注意：以下 ALTER 语句会忽略 MySQL 列重复错误，旧库可安全执行

ALTER TABLE `assembly_task`
    ADD COLUMN `start_time`  DATETIME DEFAULT NULL COMMENT '任务开始时间' AFTER `assembly_round`,
    ADD COLUMN `finish_time` DATETIME DEFAULT NULL COMMENT '任务完成时间' AFTER `start_time`;

ALTER TABLE `sop_step`
    ADD COLUMN `target_label`      VARCHAR(128) DEFAULT NULL COMMENT '检测目标标签' AFTER `roi_config`,
    ADD COLUMN `next_target_label` VARCHAR(128) DEFAULT NULL COMMENT '下一步检测目标标签' AFTER `target_label`,
    ADD COLUMN `standard_duration` INT          DEFAULT NULL COMMENT '标准工时（秒）' AFTER `next_target_label`;

ALTER TABLE `assembly_step_log`
    ADD COLUMN `pass_type` VARCHAR(32)  DEFAULT NULL COMMENT '通过方式（AI_PASS / MANUAL_PASS）' AFTER `status`,
    ADD COLUMN `duration`  BIGINT       DEFAULT NULL COMMENT '步骤耗时（秒）' AFTER `pass_type`,
    ADD COLUMN `step_no`   INT          DEFAULT NULL COMMENT '步骤序号' AFTER `duration`;

-- 7. 流程事件表（第四阶段）
DROP TABLE IF EXISTS `process_event`;
CREATE TABLE `process_event` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    `event_id`           VARCHAR(256)  DEFAULT NULL COMMENT '外部事件ID（唯一）',
    `task_id`            BIGINT        DEFAULT NULL COMMENT '关联任务ID',
    `device_sn`          VARCHAR(64)   DEFAULT NULL COMMENT '设备编码',
    `step_id`            BIGINT        DEFAULT NULL COMMENT '步骤ID',
    `step_run_id`        BIGINT        DEFAULT NULL COMMENT '步骤日志ID',
    `event_type`         VARCHAR(64)   NOT NULL COMMENT '事件类型',
    `status`             VARCHAR(32)   NOT NULL DEFAULT 'RECEIVED' COMMENT 'RECEIVED/PROCESSING/SUCCESS/FAILED/IGNORED',
    `payload`            TEXT          DEFAULT NULL COMMENT '原始载荷（JSON）',
    `result`             VARCHAR(512)  DEFAULT NULL COMMENT '处理结果简述',
    `error_message`      VARCHAR(1024) DEFAULT NULL COMMENT '异常信息',
    `processed_time`     DATETIME      DEFAULT NULL COMMENT '处理完成时间',
    `duplicate_count`    INT           DEFAULT 0 COMMENT '重复回调次数',
    `last_received_time` DATETIME      DEFAULT NULL COMMENT '最后一次收到重复回调的时间',
    `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程事件表';
