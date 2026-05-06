package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品型号 VO
 * <p>
 * 用于返回给前端的产品型号视图数据。
 *
 * @author SOP Team
 */
@Data
public class ProductModelVO {

    private Long modelId;
    private String modelCode;
    private String modelName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
