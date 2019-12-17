package com.sjtu.objectdataengine.utils.Result;

// 包装了请求的返回结果
public class Result implements ResultInterface {
    // 返回码
    private String code;
    // 说明信息
    private String msg;

    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public static Result build(String code, String msg) {
        return new Result(code, msg);
    }
    public static Result build(ResultCodeEnum resultCodeEnum) {
        return new Result(resultCodeEnum.getCode(), resultCodeEnum.getMsg());
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}
