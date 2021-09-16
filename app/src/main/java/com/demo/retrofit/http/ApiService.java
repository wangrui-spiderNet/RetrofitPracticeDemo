package com.demo.retrofit.http;

import com.demo.retrofit.entity.LoginResp;
import com.demo.retrofit.entity.TmpSecurityToken;
import com.demo.retrofit.entity.UserInfo;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    String URL_LOGIN = "uaa/oauth/token";
    String URL_REGIST = "users/";
    String URL_CUR_USER_INFO = "users/current";
    String URL_UPDATE_USER_INFO = "users/%d";
    String URL_MODIFY_PWD = "uaa/users/%d/pass";
    String URL_RESET_PWD_BY_EMAIL = "password-reset/tokens";
    String URL_RESET_PWD = "password-reset/reset";
    String URL_INVITE_FRIEND = "frame/users/%d/invite";

    String URL_TMP_TOKEN_BY_STS = "storage/oss/sts";
    String URL_TMP_TOKEN_BY_S3_STS = "storage/s3/sts";
    String URL_GET_UPLOAD_PARAM = "storage/oss/configs";
    String URL_UPLOAD_DONE_NOTIFY = "frame/medias/push";
    String URL_OPTIMAL_BUCKET = "storage/oss/buckets";
    String URL_OPTIMAL_S3_BUCKET = "storage/s3/buckets";

    @FormUrlEncoded
    @POST(URL_LOGIN)
    Call<LoginResp> login(@Field("username") String username, @Field("password") String password, @Field("grant_type") String type, @Field("client_id") String app);

    @GET(URL_CUR_USER_INFO)
    Call<UserInfo> getUserInfo();

    @GET(URL_TMP_TOKEN_BY_STS)
    Call<TmpSecurityToken> getOssToken();


}
