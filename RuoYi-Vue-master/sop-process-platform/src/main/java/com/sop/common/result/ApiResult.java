package com.sop.common.result;

import com.sop.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 * <p>
 * 所有接口统一返回该类型，包含状态码、消息和数据。
 *
 * @author SOP Team
 */
@Data
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    private ApiResult() {
    }

    private ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 成功 ====================

    /**
     * 成功 - 无返回数据
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功 - 带返回数据
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功 - 带自定义消息和数据
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), message, data);
    }

    // ==================== 失败 ====================

    /**
     * 失败 - 使用 ErrorCode
     */
    public static <T> ApiResult<T> error(ErrorCode errorCode) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败 - 使用 ErrorCode 和自定义消息
     */
    public static <T> ApiResult<T> error(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.getCode(), message, null);
    }

    /**
     * 失败 - 使用自定义状态码和消息
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    /**
     * 失败 - 带数据
     */
    public static <T> ApiResult<T> error(ErrorCode errorCode, T data) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    // ==================== 便捷判断 ====================

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }
}
