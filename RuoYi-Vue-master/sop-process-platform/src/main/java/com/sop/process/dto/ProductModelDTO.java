package com.sop.process.dto;

import lombok.Data;

/**
 * 产品型号 DTO
 * <p>
 * 用于新增/修改产品型号时的请求参数传输。
 *
 * @author SOP Team
 */
@Data
public class ProductModelDTO {

    /** 型号编码 */
    private String modelCode;

    /** 型号名称 */
    private String modelName;

    /** 型号描述 */
    private String description;
}
