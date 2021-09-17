package com.demo.retrofit.other;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.HeaderMap;
import retrofit2.http.Url;

/**
 * @author xuwei
 * on 2019/9/26
 * Get请求封装
 */
public interface GetRequest {
 
    /**
     * 发送Get请求请求
     *
     * @param url
     * @return
     */
 
    @GET
    Call<ResponseBody> getUrl( @Url String url);
 
    /**
     * 发送Get请求请求 表单
     *
     * @param url
     * @param requestMap
     * @return
     */
    @GET
    Call<ResponseBody> getForm(@Url String url, @FieldMap Map<String, Object> requestMap);
 
    /**
     * 发送Get请求请求  json
     *
     * @param url
     * @param route
     * @return
     */
    @HTTP(method = "GET", hasBody = true)
    Call<ResponseBody> getJson(@Url String url, @Body RequestBody route);

    /**
     * 如果需要单独接口加header，可以这样扩展
     * @param headers
     * @param url
     * @param route
     * @return
     */
    @HTTP(method = "GET", hasBody = true)
    Call<ResponseBody> getJson(@HeaderMap Map<String, String> headers,@Url String url, @Body RequestBody route);

}
 