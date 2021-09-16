package com.demo.retrofit.other;
 
import java.util.Map;
 
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.PUT;
import retrofit2.http.Url;
 
/**
 * @author xuwei
 * on 2019/9/26
 * put请求封装
 */
public interface PutRequest {
 
    /**
     * 发送put请求
     * @param url
     * @return
     */
    @PUT
    Call<ResponseBody> putUrl(@HeaderMap Map<String, String> headers, @Url String url);
 
    /**
     * 发送put请求 表单
     * @param url
     * @param requestMap
     * @return
     */
    @FormUrlEncoded
    @PUT
    Call<ResponseBody> putForm(@HeaderMap Map<String, String> headers,@Url String url, @FieldMap Map<String, Object> requestMap);
 
    /**
     * 发送put请求  json
     * @param url
     * @param route
     * @return
     */
    @PUT
    Call<ResponseBody> putJson(@HeaderMap Map<String, String> headers,@Url String url, @Body RequestBody route);
 
}