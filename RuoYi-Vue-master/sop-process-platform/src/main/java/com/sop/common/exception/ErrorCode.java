package com.sop.common.exception;

/**
 * 错误码枚举
 * <p>
 * 定义系统中所有标准错误码，code 为数字状态码，message 为默认提示信息。
 *
 * @author SOP Team
 */
public enum ErrorCode {

    // ==================== 通用 (1xxxx) ====================

    /** 成功 */
    SUCCESS(0, "操作成功"),

    /** 系统内部错误 */
    INTERNAL_ERROR(10001, "系统内部错误"),

    /** 请求参数错误 */
    BAD_REQUEST(10002, "请求参数错误"),

    /** 资源未找到 */
    NOT_FOUND(10003, "资源不存在"),

    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(10004, "请求方法不支持"),

    /** 媒体类型不支持 */
    UNSUPPORTED_MEDIA_TYPE(10005, "不支持的媒体类型"),

    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(10006, "请求过于频繁，请稍后重试"),

    /** 服务不可用 */
    SERVICE_UNAVAILABLE(10007, "服务暂不可用，请稍后重试"),

    // ==================== 业务异常 (2xxxx) ====================

    /** 业务通用错误 */
    BUSINESS_ERROR(20001, "业务处理失败"),

    /** 数据校验失败 */
    VALIDATION_FAILED(20002, "数据校验失败"),

    /** 数据已存在 */
    DATA_ALREADY_EXISTS(20003, "数据已存在"),

    /** 数据不存在 */
    DATA_NOT_FOUND(20004, "数据不存在"),

    /** 操作不允许 */
    OPERATION_NOT_ALLOWED(20005, "操作不允许"),

    /** 状态冲突 */
    STATUS_CONFLICT(20006, "当前状态不允许此操作"),

    /** 任务不存在或已结束 */
    TASK_NOT_RUNNING(20007, "任务不存在或已结束"),

    /** 同一设备已有进行中的任务 */
    TASK_RUNNING_EXISTS(20008, "该设备已有进行中的装配任务"),

    /** 未配置SOP步骤 */
    NO_SOP_STEPS(20009, "该型号未配置SOP步骤"),

    /** 步骤不匹配 */
    STEP_NOT_MATCH(20010, "当前步骤与任务步骤不匹配"),

    /** 步骤不允许当前操作 */
    STEP_STATUS_DENIED(20011, "当前步骤状态不允许此操作"),

    // ==================== 外部服务 (3xxxx) ====================

    /** 视觉服务调用失败 */
    VISION_SERVICE_ERROR(30001, "视觉服务调用失败"),

    /** 视觉服务超时 */
    VISION_SERVICE_TIMEOUT(30002, "视觉服务超时"),

    /** 视觉服务返回异常结果 */
    VISION_SERVICE_INVALID_RESPONSE(30003, "视觉服务返回异常结果"),

    // ==================== 基础设施 (4xxxx) ====================

    /** 数据库操作失败 */
    DATABASE_ERROR(40001, "数据库操作失败"),

    /** Redis 操作失败 */
    REDIS_ERROR(40002, "缓存服务异常"),

    /** 文件操作失败 */
    FILE_ERROR(40003, "文件操作失败"),

    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
