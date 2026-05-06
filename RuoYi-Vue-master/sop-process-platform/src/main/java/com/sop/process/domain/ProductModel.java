package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品型号实体
 * <p>
 * 对应数据库表 product_model，存储转辙机/设备型号信息。
 *
 * @author SOP Team
 */
@Data
@TableName("product_model")
public class ProductModel {

    /** 型号ID */
    @TableId(type = IdType.AUTO)
    private Long modelId;

    /** 型号编码 */
    private String modelCode;

    /** 型号名称 */
    private String modelName;

    /** 型号描述/备注 */
    private String description;

    /** 是否删除（0-正常 1-删除） */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
