package com.sea.common.entity;

/**
 * 返回码
 */
public enum ResultCode {
    SUCCESS(true, 10000, "操作成功"),
    // 系统错误返回码
    FAIL(false, 10001, "操作失败"),
    UNAUTHENTICATED(false, 10002, "您还未登录"),
    UNAUTHORISE(false, 10002, "权限不足"),
    SERVER_ERROR(false, 99999, "系统繁忙，请稍后再试！"),
    MOBILEORPASSWORD_ERROR(false, 10002, "用户名或密码错误"),
    AUTH_FAIL(false, 10002, "校验令牌失败");

    // 操作是否成功
    boolean success;
    // 操作代码
    int code;
    // 提示信息
    String message;

    ResultCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
