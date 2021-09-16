package com.demo.retrofit.entity;

public class S3TmpSecurityToken {
    //安全令牌
    public String sessionToken;
    //临时访问密钥
    public String accessKeyId;
    //临时访问密钥
    public String secretAccessKey;
    //过期
    public String expiration;

    @Override
    public String toString() {
        return "S3TmpSecurityToken{" +
                "sessionToken='" + sessionToken + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", accessKeySecret='" + secretAccessKey + '\'' +
                ", expiration='" + expiration + '\'' +
                '}';
    }
}
