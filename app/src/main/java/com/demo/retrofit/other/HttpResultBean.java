package com.demo.retrofit.other;

import com.google.gson.annotations.SerializedName;

public class HttpResultBean<T> {
 
    private int statusCode;
    @SerializedName(value = "message", alternate = "msg")
    private String msg;
    private T items;
 
    public int getStatusCode() {
        return statusCode;
    }
 
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
 
    public String getMsg() {
        return msg == null ? "" : msg;
    }
 
    public void setMsg(String msg) {
        this.msg = msg;
    }
 
    public T getItems() {
        return items;
    }
 
    public void setItems(T items) {
        this.items = items;
    }
 
 
    public HttpResultBean toResponse() {
        HttpResultBean httpResultBean = new HttpResultBean();
        httpResultBean.setStatusCode(statusCode);
        httpResultBean.setMsg(msg);
        return httpResultBean;
    }
}