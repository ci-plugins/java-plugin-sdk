package com.tencent.bk.devops.atom.pojo;

public class Result<T> {
    private int status; //状态码，0代表成功
    private String message; //描述信息
    private T  data; //数据对象

    public Result() {
    }

    public Result(T data) {
        this.status = 0;
        this.data = data;
    }

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isNotOk() {
        return this.status != 0;
    }

    public boolean isOk() {
        return this.status == 0;
    }
}
