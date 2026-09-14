package com.xqk.campusfind.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 🎯 优雅拦截实体参数绑定校验异常，提取出精准提示返回给页面
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String bindingMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error(bindingMessage != null ? bindingMessage : "输入参数格式不合规");
    }
}