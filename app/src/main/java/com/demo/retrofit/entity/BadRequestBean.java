package com.demo.retrofit.entity;

import java.io.Serializable;

public class BadRequestBean implements Serializable {

    /**
     * status : BAD_REQUEST
     * code : 1003
     * message : Bad credentials
     * description : Bad credentials
     * timestamp : null
     */

    private String status;
    private int code;
    private String message;
    private String description;
    private Object timestamp;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
