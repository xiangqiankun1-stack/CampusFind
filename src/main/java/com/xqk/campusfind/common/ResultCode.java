package com.xqk.campusfind.common;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(400, "业务异常"),
    UNAUTHORIZED(401, "暂无权限"),
    SERVER_ERROR(500, "服务器未知错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}