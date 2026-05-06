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
