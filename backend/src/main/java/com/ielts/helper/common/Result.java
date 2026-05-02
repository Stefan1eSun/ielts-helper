package com.ielts.helper.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private String error;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String error) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setError(error);
        return result;
    }

    public static <T> Result<T> error(String error) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setError(error);
        return result;
    }
}
