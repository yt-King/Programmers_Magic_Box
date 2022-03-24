package com.technology_application;

/**
 * @description:封装json对象，所有返回结果都使用它
 **/
public class Result<T> {

    private int code;// 业务自定义状态码

    private String message;// 请求状态描述，调试用

    private T data;// 请求数据，对象或数组均可

    public Result() {
    }

    /**
     * 成功时候的调用
     * @param data data
     * @param <T> t
     * @return Result
     */
    public static <T> Result<T> success(T data){
        return new Result<T>(data);
    }

    /**
     * 失败时候的调用
     * @param codeMsg codeMsg
     * @param <T> t
     * @return Result
     */
    public static <T> Result<T> error(CodeMsg codeMsg){
        return new Result<T>(codeMsg);
    }

    /**
     * 成功的构造函数
     * @param data data
     */
    public Result(T data){
        this.code = 20000;//默认20000是成功
        this.message = "SUCCESS";
        this.data = data;
    }

    public Result(int code, String massage) {
        this.code = code;
        this.message = massage;
    }

    /**
     * 失败的构造函数
     * @param codeMsg codeMsg
     */
    private Result(CodeMsg codeMsg) {
        if(codeMsg != null) {
            this.code = codeMsg.getCode();
            this.message = codeMsg.getMsg();
        }
    }

    public static <T> Result<T> error(T data){
        return new Result<T>(data);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}