package com.technology_application;

public class CodeMsg {

    private int code;
    private String message;
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
    /*服务端异常*/
    public static CodeMsg SUCCESS = new CodeMsg(20000,"SUCCESS");
    public static CodeMsg SERVER_ERROR = new CodeMsg(100,"系统异常：%s");
    public static CodeMsg BIND_ERROR = new CodeMsg(101,"(绑定异常)参数校验异常：%s"); /*用占位符 传入一个参数*/
    public static CodeMsg SESSION_ERROR = new CodeMsg(102,"没有SESSION！"); /*用占位符 传入一个参数*/
    public static CodeMsg REQUEST_ERROR = new CodeMsg(103,"非法请求！"); /*用占位符 传入一个参数*/
    public static CodeMsg REQUEST_OVER_LIMIT = new CodeMsg(104,"请求次数过多！"); /*用占位符 传入一个参数*/

    private CodeMsg( ) {
    }
    private CodeMsg( int code,String message ) {
        this.code = code;
        this.message = message;
    }
    //不定参的构造函数
    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.message, args);
        return new CodeMsg(code, message);
    }
    @Override
    public String toString() {
        return "CodeMessage [code=" + code + ", message=" + message + "]";
    }
}