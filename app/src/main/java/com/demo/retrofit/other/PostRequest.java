package com.demo.retrofit.other;
 

import java.util.Map;
 
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
 
/**
 * @author xuwei
 * on 2019/9/26
 * Post请求封装
 */
public interface PostRequest {
 
    /**
     * 发送Post请求
     * @param url
     * @return
     */
    @POST
    Call<ResponseBody> postUrl(@HeaderMap Map<String, String> headers,@Url String url);
 
    /**
     * 发送Post请求 表单
     * @param url
     * @param requestMap
     * @return
     */
    @FormUrlEncoded
    @POST
    Call<ResponseBody> postForm(@HeaderMap Map<String, String> headers,@Url String url, @FieldMap Map<String, Object> requestMap);
 
    /**
     * 发送Post请求  json
     * @param url
     * @param route
     * @return
     */
    @POST
    Call<ResponseBody> postJson(@HeaderMap Map<String, String> headers, @Url String url, @Body RequestBody route);
 
    /**
     * 发送Post请求  二进制流
     * @param url
     * @param options
     * @param file
     * @return
     */
    @Multipart
    @POST
    Call<ResponseBody> postStream(@HeaderMap Map<String, String> headers,@Url String url, @FieldMap Map<String, Object> options, @Part("file") RequestBody file);
 
 
}