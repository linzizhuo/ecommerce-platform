package com.cloudmall.common.result;

/**
 * 统一返回体 — MVC架构核心改造
 * 相比MV架构的ModelAndView, 统一返回JSON格式
 */
public class R<T> {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> R<T> fail(String msg) {
        return fail(400, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.message = msg;
        return r;
    }

    public static <T> R<T> unauthorized() {
        return fail(401, "请先登录");
    }

    // getters
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    // setters
    public void setCode(int code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
}
