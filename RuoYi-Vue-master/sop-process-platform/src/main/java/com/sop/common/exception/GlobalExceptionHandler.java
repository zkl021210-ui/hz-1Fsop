package com.sop.common.exception;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.sop.common.result.ApiResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理所有异常，返回标准 {@link ApiResult} 格式。
 *
 * @author SOP Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理 @Valid 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ApiResult.error(ErrorCode.VALIDATION_FAILED, message);
    }

    /**
     * 处理 @Validated 参数校验失败（表单绑定）
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定校验失败: {}", message);
        return ApiResult.error(ErrorCode.VALIDATION_FAILED, message);
    }

    /**
     * 处理单个参数校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("单个参数校验失败: {}", message);
        return ApiResult.error(ErrorCode.VALIDATION_FAILED, message);
    }

    /**
     * 处理缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        String message = "缺少必要参数: " + e.getParameterName();
        log.warn("缺少请求参数: {}", e.getParameterName());
        return ApiResult.error(ErrorCode.BAD_REQUEST, message);
    }

    /**
     * 处理参数类型转换错误
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResult<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "参数 '" + e.getName() + "' 类型错误，期望类型: " +
                (e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型转换错误: {}", message);
        return ApiResult.error(ErrorCode.BAD_REQUEST, message);
    }

    // ==================== HTTP 相关异常 ====================

    /**
     * 处理请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResult<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String message = "请求方法 '" + e.getMethod() + "' 不支持，支持的方法: " +
                String.join(", ", e.getSupportedMethods());
        log.warn("请求方法不支持: {}", message);
        return ApiResult.error(ErrorCode.METHOD_NOT_ALLOWED, message);
    }

    /**
     * 处理媒体类型不支持
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiResult<Void> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        String message = "不支持的媒体类型: " + e.getContentType();
        log.warn("媒体类型不支持: {}", message);
        return ApiResult.error(ErrorCode.UNSUPPORTED_MEDIA_TYPE, message);
    }

    /**
     * 处理请求体不可读
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体不可读: {}", e.getMessage());
        return ApiResult.error(ErrorCode.BAD_REQUEST, "请求体格式错误或不可读");
    }

    /**
     * 处理资源未找到
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResult<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return ApiResult.error(ErrorCode.NOT_FOUND, "请求的资源不存在");
    }

    // ==================== 数据层异常 ====================

    /**
     * 处理 MyBatis-Plus 乐观锁冲突
     */
    @ExceptionHandler(MybatisPlusException.class)
    public ApiResult<Void> handleMybatisPlusException(MybatisPlusException e) {
        if (e.getMessage() != null && e.getMessage().contains("version")) {
            log.warn("乐观锁冲突: {}", e.getMessage());
            return ApiResult.error(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
        log.error("MyBatis-Plus 异常: ", e);
        return ApiResult.error(ErrorCode.DATABASE_ERROR, "数据库操作异常");
    }

    // ==================== 系统异常 ====================

    /**
     * 处理未知异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Void> handleException(Exception e) {
        log.error("系统内部错误: ", e);
        return ApiResult.error(ErrorCode.INTERNAL_ERROR, "系统繁忙，请稍后重试");
    }
}
