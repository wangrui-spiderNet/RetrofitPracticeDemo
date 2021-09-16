package com.demo.retrofit.other;
 
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.demo.retrofit.config.Config;
import com.demo.retrofit.util.NetworkUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tencent.mmkv.MMKV;

import org.json.JSONObject;
 
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import io.reactivex.internal.schedulers.RxThreadFactory;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
 
/**
 * @author xuwei
 * on 2019/9/26
 */
public class RetrofitManager {
 
    private static int TIME_OUT = 30;
    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            //打印retrofit日志
            Log.e("RetrofitLog", "retrofitBack = " + message);
//            if (TextUtils.isEmpty(message)) {
//                return;
//            }
//            String s = message.substring(0, 1);
//            if ("{".equals(s) || "[".equals(s)) {
//                try {
//                    getToken(message);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
 
        }
    });

    public static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(new LoggingInterceptor())
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .build();

    //    //缓存路径
//    File cacheFile = new File(Environment.getExternalStorageDirectory(), "HttpCache");
//    Cache cache = new Cache(cacheFile, 1024 * 1024 * 10);//缓存文件为100MB
//        Log.e(TAG, "initRetrofit: " + cacheFile.getAbsolutePath());
//
//    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//    OkHttpClient httpClient = new OkHttpClient.Builder()
//            .addInterceptor(new Interceptor() {
//                @Override
//                public Response intercept(Chain chain) throws IOException {
//                    Request request = chain.request()
//                            .newBuilder()
//                                .addHeader("TSQ-RequestId", BaseUtil.encryptByPublicKey())
//                            .build();
//                    return chain.proceed(request);
//                }
//            })
//            .connectTimeout(5, TimeUnit.SECONDS)//设置连接超时
//            .readTimeout(10, TimeUnit.SECONDS)//读取超时
//            .writeTimeout(10, TimeUnit.SECONDS)//写入超时
//            .addInterceptor(interceptor)//添加日志拦截器
//            .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)//添加缓存拦截器
//            .cache(cache)//把缓存添加进来
//            .build();
 
    public static class LoggingInterceptor implements Interceptor {
 
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            //第一步，获得chain内的request
            Request request = chain.request();
            Log.e("intercept", "intercept-request==:" + request.url());
            //第二步，用chain执行request
            okhttp3.Response response = chain.proceed(request);
            Log.e("intercept", "intercept-response==" + "-" + response.request().url());
            //第三步，返回response
            if (response.code() == 401) {
                String newSession = MMKV.defaultMMKV().getString("token","");
                //使用新的Token，创建新的请求
                Request newRequest = chain.request()
                        .newBuilder()
                        .header("access_token", newSession)
                        .header("app_key", "1")
                        .build();
                //重新请求
                return chain.proceed(newRequest);
            }
            return response;
        }
    }

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .client(client)
            .build();
 
    /**
     * 发送GET网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendGetRobot(String url, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
 
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
 
        GetRequest getRequest = retrofit.create(GetRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = getRequest.getUrl(addTokenMap(),url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     *  添加header
     * @return
     */
    public static Map addTokenMap() {
        Map<String,String> map = new HashMap<String, String>();
        map.put("access_token", MMKV.defaultMMKV().getString("token",""));
        map.put("app_key","1");
        return map;
    }
    /**
     * 发送GET网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendGetRequest(String url, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
 
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
 
        GetRequest getRequest = retrofit.create(GetRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = getRequest.getUrl(addTokenMap(),url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    string = string.replace("null(", "").replace(")", "");
                    T t = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送Get网络请求 提交表单
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendGetRequestFrom(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        GetRequest getRequest = retrofit.create(GetRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = getRequest.getForm(addTokenMap(),url, paramMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送get网络请求 提交Json
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendGetRequestJson(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        GetRequest getRequest = retrofit.create(GetRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = getRequest.getJson(addTokenMap(),url, getBody(paramMap));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送put网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendPutRequest(String url, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PutRequest putRequest = retrofit.create(PutRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = putRequest.putUrl(addTokenMap(),url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送put网络请求 提交表单
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendPutRequestFrom(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PutRequest putRequest = retrofit.create(PutRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = putRequest.putForm(addTokenMap(),url, paramMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送put网络请求 提交Json
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendPutRequestJson(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PutRequest putRequest = retrofit.create(PutRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = putRequest.putJson(addTokenMap(),url, getBody(paramMap));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
//                    if (response.code() == CHARACTER) {
//                        ToastUtil.show(MyApplication.getContext(), "上传内容包含非法字符不允许上传");
//                        return;
//                    }
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
 
    /**
     * 发送post网络请求 提交表单
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendPostRequestFrom(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = postRequest.postForm(addTokenMap(),url, paramMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送post网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendPostRequest(String url, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = postRequest.postUrl(addTokenMap(),url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送post网络请求 提交Json
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendPostRequestJson(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = postRequest.postJson(addTokenMap(),url, getBody(paramMap));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送post网络请求 提交二进制流
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     * @param fileUtil      Byte数组
     */
    public static <T> void sendPostRequestStream(String url, Map<String, Object> paramMap, final Class<T> clazz, byte[] fileUtil, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), fileUtil);
        // 构建请求
        Call<ResponseBody> call = postRequest.postStream(addTokenMap(),url, paramMap, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送delete网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendDeleteRequest(String url, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        DeleteRequest deleteRequest = retrofit.create(DeleteRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = deleteRequest.deleteUrl(addTokenMap(),url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送delete网络请求 提交表单
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendDeleteRequestForm(String url, Map<String, Object> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        DeleteRequest deleteRequest = retrofit.create(DeleteRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = deleteRequest.deleteForm(addTokenMap(),url, paramMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
 
    /**
     * 发送delete网络请求 提交Json
     *
     * @param url           请求地址
     * @param paramMap      参数列表
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendDeleteRequestJson(String url, Map<String, String> paramMap, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        DeleteRequest deleteRequest = retrofit.create(DeleteRequest.class);
 
        // 构建请求
        Call<ResponseBody> call = deleteRequest.deleteJson(addTokenMap(),url, getBody(paramMap));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 发送上传文件网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     * @param file          上传的文件
     */
    public static <T> void fileUpload(String url, File file, final Class<T> clazz, final AbstractResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onAfterFailure();
            return;
        }
        FileRequest fileRequest = retrofit.create(FileRequest.class);
 
        Map<String, RequestBody> paramMap = new HashMap<>(16);
        addMultiPart(paramMap, "file", file);
 
        // 构建请求
        Call<ResponseBody> call = fileRequest.postFile(addTokenMap(),url, paramMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultHandler.onBeforeResult();
                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        resultHandler.onServerError();
                        resultHandler.onAfterFailure();
                        return;
                    }
                    String string = body.string();
                    T t = new Gson().fromJson(string, clazz);
 
                    resultHandler.onResult(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onFailure(e);
                    resultHandler.onAfterFailure();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onAfterFailure();
            }
        });
    }
 
    /**
     * 文件下载
     *
     * @param url             下载地址
     * @param downloadHandler 下载回调
     */
    public static void fileDownload(String url, final DownloadHandler downloadHandler) {
        // 回调方法执行器，定义回调在子线程中执行，避免Callback返回到MainThread，导致文件下载出现NetworkOnMainThreadException
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();

        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
 
        // 网络框架
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL)
                .callbackExecutor(singleThreadPool)
                .build();
 
        FileRequest fileRequest = retrofit.create(FileRequest.class);
        // 构建请求
        Call<ResponseBody> call = fileRequest.download(addTokenMap(),url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 写入文件
                    downloadHandler.onBody(response.body());
                } else {
                    downloadHandler.onError();
                }
            }
 
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                downloadHandler.onError();
            }
        });
    }
 
    /**
     * 添加多媒体类型
     */
    private static void addMultiPart(Map<String, RequestBody> paramMap, String key, Object obj) {
        if (obj instanceof String) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain;charset=UTF-8"), (String) obj);
            paramMap.put(key, body);
        } else if (obj instanceof File) {
            RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data;charset=UTF-8"), (File) obj);
            paramMap.put(key + "\"; filename=\"" + ((File) obj).getName() + "", body);
        }
    }
 
    /**
     * 网络请求结果处理类
     *
     * @param <T> 请求结果封装对象
     */
    public static abstract class AbstractResultHandler<T> {
        Context context;
 
        public AbstractResultHandler(Context context) {
            this.context = context;
        }
 
        /**
         * 判断网络是否未连接
         *
         * @return
         */
        public boolean isNetDisconnected() {
            return !NetworkUtil.isNetworkConnected(context);
        }
 
        /**
         * 请求成功之前
         */
        public abstract void onBeforeResult();
 
        /**
         * 请求成功时
         *
         * @param t 结果数据
         */
        public abstract void onResult(T t);
 
        /**
         * 服务器出错
         */
        public void onServerError() {
            // 服务器处理出错
//            Toast.makeText(context, R.string.net_server_error, Toast.LENGTH_SHORT).show();
        }
 
        /**
         * 请求失败后的处理
         */
        public abstract void onAfterFailure();
 
        /**
         * 请求失败时的处理
         *
         * @param t
         */
        public void onFailure(Throwable t) {
            if (t instanceof SocketTimeoutException || t instanceof ConnectException) {
                // 连接异常
                if (NetworkUtil.isNetworkConnected(context)) {
                    // 服务器连接出错
                    Toast.makeText(context, "服务器断开", Toast.LENGTH_SHORT).show();
                } else {
                    // 手机网络不通
                    Toast.makeText(context,"请检查您的网络", Toast.LENGTH_SHORT).show();
                }
            } else if (t instanceof Exception) {
                // 功能异常
                Toast.makeText(context, "未知异常", Toast.LENGTH_SHORT).show();
            }
        }
    }
 
    /**
     * 文件下载回调
     */
    public interface DownloadHandler {
        /**
         * 接收到数据体
         *
         * @param body 响应体
         */
        public void onBody(ResponseBody body);
 
        /**
         * 文件下载出错
         */
        public void onError();
    }
 
    /**
     * map转换Json
     */
    public static RequestBody getBody(Map map) {
        Gson gson = new Gson();
        String strEntity = gson.toJson(map);
        Log.e("=====json串", strEntity);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), strEntity);
        return body;
    }
}