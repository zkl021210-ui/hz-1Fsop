CREATE TABLE `biz_assembly_task` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务主键',
  `device_type` varchar(50) DEFAULT NULL COMMENT '转辙机型号 (S700/ZDJ9)',
  `device_sn` varchar(100) NOT NULL COMMENT '转辙机唯一编码 (扫码录入)',
  `worker_id` bigint(20) DEFAULT NULL COMMENT '当前操作工ID',
  `worker_name` varchar(50) DEFAULT NULL COMMENT '当前操作工姓名',
  `current_step_index` int(11) DEFAULT 1 COMMENT '当前进行到第几步',
  `total_steps` int(11) DEFAULT 0 COMMENT '总步骤数',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0=进行中 1=暂停 2=已完成 3=异常中止)',
  `create_time` datetime DEFAULT NULL COMMENT '开工时间',
  `update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `uk_device_sn` (`device_sn`) COMMENT '防止同一台机器被重复装配'
) ENGINE=InnoDB COMMENT='装配任务主表';