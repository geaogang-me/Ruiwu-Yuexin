package com.gag.RuiwuYuexin.exception;

import com.gag.RuiwuYuexin.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常：{} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("500", "服务器异常：" + e.getMessage());
    }

    // 捕获自定义业务异常（可选）
    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("业务异常：{} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数校验失败（如 @Valid 注解失败）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败：{} - {}", request.getRequestURI(), msg);
        return Result.error("400", msg);
    }

    // 捕获所有其他异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常：{} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("500", "系统繁忙，请稍后重试");
    }
}
