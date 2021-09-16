package com.demo.retrofit.other;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HttpResultArrayBean<T> {
 
    private int statusCode;
    @SerializedName(value = "message", alternate = "msg")
    private String msg;
    private List<T> items;
 
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
 
    public List<T> getItems() {
        if (items == null) {
            return new ArrayList<>();
        }
        return items;
    }
 
    public void setItems(List<T> items) {
        this.items = items;
    }
}