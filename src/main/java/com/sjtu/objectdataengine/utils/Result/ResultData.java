package com.sjtu.objectdataengine.utils.Result;

// 包装了请求的返回结果
public class ResultData<T> implements ResultInterface {
    // 返回码
    private String code;
    // 说明信息
    private String msg;
    // 具体数据
    private T data;

    public ResultData(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> ResultData<T> build(String code, String msg, T data) {
        return new ResultData<T>(code, msg, data);
    }
    public static <T> ResultData<T> build(ResultCodeEnum resultCodeEnum, T data) {
        return new ResultData<T>(resultCodeEnum.getCode(), resultCodeEnum.getMsg(), data);
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public T getData() {
        return data;
    }
}
