package com.demo.retrofit.other;
 
import java.util.Map;
 
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.HeaderMap;
import retrofit2.http.Url;
 
/**
 * @author xuwei
 * on 2019/9/26
 * delete请求封装
 */
public interface DeleteRequest {
    /**
     * 发送delete请求
     * @param url
     * @return
     */
    @DELETE
    Call<ResponseBody> deleteUrl(@HeaderMap Map<String, String> headers, @Url String url);
 
    /**
     * 发送delete请求 表单
     * @param url
     * @param requestMap
     * @return
     */
    @DELETE
    Call<ResponseBody> deleteForm(@HeaderMap Map<String, String> headers,@Url String url, @FieldMap Map<String, Object> requestMap);
 
    /**
     * 发送delete请求  json
     * @param url
     * @param route
     * @return
     */
    @DELETE
    Call<ResponseBody> deleteJson(@HeaderMap Map<String, String> headers,@Url String url, @Body RequestBody route);
}