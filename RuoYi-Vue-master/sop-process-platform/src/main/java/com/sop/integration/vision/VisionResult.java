package com.sop.integration.vision;

import lombok.Data;

/**
 * 视觉服务通用返回结果
 *
 * @author SOP Team
 */
@Data
public class VisionResult {

    /** 是否成功 */
    private boolean success;

    /** 错误码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    /** 返回数据（可为空） */
    private Object data;

    public static VisionResult ok() {
        VisionResult result = new VisionResult();
        result.success = true;
        return result;
    }

    public static VisionResult ok(Object data) {
        VisionResult result = new VisionResult();
        result.success = true;
        result.data = data;
        return result;
    }

    public static VisionResult fail(String errorCode, String errorMessage) {
        VisionResult result = new VisionResult();
        result.success = false;
        result.errorCode = errorCode;
        result.errorMessage = errorMessage;
        return result;
    }
}
