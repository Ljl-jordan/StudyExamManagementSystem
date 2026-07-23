package com.ljl.studyexammanagementsystem.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer CODE_SUCCESS = 200;
    public static final Integer CODE_PARAM_ERROR = 400;
    public static final Integer CODE_UNAUTHORIZED = 401;
    public static final Integer CODE_FORBIDDEN = 403;
    public static final Integer CODE_BUSINESS_BLOCK = 405;
    public static final Integer CODE_SERVER_ERROR = 500;

    private Integer code;
    private String msg;
    private T data;

    private Result() {}

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(CODE_SUCCESS, msg, data);
    }

    public static <T> Result<T> paramError(String msg) {
        return new Result<>(CODE_PARAM_ERROR, msg, null);
    }

    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(CODE_UNAUTHORIZED, msg, null);
    }

    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(CODE_FORBIDDEN, msg, null);
    }

    public static <T> Result<T> businessBlock(String msg) {return new Result<>(CODE_BUSINESS_BLOCK, msg, null);}
    public static <T> Result<T> serverError(String msg) {
        return new Result<>(CODE_SERVER_ERROR, msg, null);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static Result<String> error(String msg) {
        return new Result(CODE_SERVER_ERROR, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
