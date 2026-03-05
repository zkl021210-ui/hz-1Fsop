CREATE TABLE `biz_task_step_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL COMMENT '关联的任务ID',
  `step_order` int(11) NOT NULL COMMENT '步骤序号 (第几步)',
  `step_name` varchar(100) DEFAULT NULL COMMENT '步骤名称 (如：底座安装)',
  
  /* --- 核心：视频追溯接口字段 --- */
  `video_local_path` varchar(255) DEFAULT NULL COMMENT '本地临时路径 (D:/...)',
  `video_cloud_url` varchar(500) DEFAULT NULL COMMENT '云端地址 (预留给后面的人填)',
  `video_upload_status` char(1) DEFAULT '0' COMMENT '上传状态 (0=待上传 1=已上传 2=上传失败)',
  /* -------------------------- */
  
  `detect_result` varchar(50) DEFAULT 'PASS' COMMENT 'AI判定结果',
  `manual_intervention` char(1) DEFAULT 'N' COMMENT '是否人工干预 (Y/N)',
  `duration` int(11) DEFAULT 0 COMMENT '本步骤耗时(秒)',
  `create_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB COMMENT='步骤执行流水表';