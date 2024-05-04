package com.hc.mq.dashboard.entity;

import java.io.Serializable;

/**
 * @Author hc
 */
public class CommonResult<T> implements Serializable {

    public static final int COMMON_RESULT_SUCCESS_CODE = 200;
    public static final int COMMON_RESULT_FAIL_CODE = 500;

    public static final CommonResult<String> SUCCESS = new CommonResult<>(null);
    public static final CommonResult<String> FAIL = new CommonResult<>(COMMON_RESULT_FAIL_CODE, null);

    private int code;
    private String msg;
    private T data;

    public CommonResult() {
    }

    public CommonResult(T data) {
        this.code = COMMON_RESULT_SUCCESS_CODE;
        this.data = data;
    }

    public CommonResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
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
