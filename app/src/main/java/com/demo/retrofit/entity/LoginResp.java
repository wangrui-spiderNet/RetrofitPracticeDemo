package com.demo.retrofit.entity;

public class LoginResp {
    public String access_token;
    public String token_type;
    public String refresh_token;
    public String expires_in;
    public String scope;

    @Override
    public String toString() {
        return "LoginResp{" +
                "access_token='" + access_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", expires_in='" + expires_in + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}