package com.demo.retrofit.other;

import android.util.Log;

import com.demo.retrofit.config.Config;
import com.demo.retrofit.entity.BadRequestBean;
import com.demo.retrofit.http.HttpBaseParamsInterceptor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
public class HttpManager {

    private static Gson gson;
    private static final int TIME_OUT = 15;

    static {
        gson = new Gson();
    }

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

    //可以添加公用参数和公用header的拦截器
    private static HttpBaseParamsInterceptor baseParamsInterceptor = new HttpBaseParamsInterceptor
            .Builder()
            .addHeaderParam("x-api-key", "uio2vIija231JSTrzAGCy1Lu9hE0PROw8kOB89ks")
            .addParam("UploadClient", "android " + MMKV.defaultMMKV().putString("app_version", "1.0.0"))
            .build();

    public static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(new RetryInterceptor())
            .addInterceptor(baseParamsInterceptor)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .build();

    public static class RetryInterceptor implements Interceptor {

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            //第一步，获得chain内的request
            Request request = chain.request();
            Log.e("intercept", "intercept-request==:" + request.url());
            //第二步，用chain执行request
            okhttp3.Response response = chain.proceed(request);
            Log.e("intercept", "intercept-response==" + "-" + response.request().url());
            //第三步，返回response ，请求失败，自动重试
            if (response.code() == 401) {
                String token = MMKV.defaultMMKV().getString("token", "");
                //使用新的Token，创建新的请求
                Request newRequest = chain.request()
                        .newBuilder()
                        .header("access_token", token)
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
    public static <T> void sendGetRobot(String url, final Class<T> clazz, final ResultHandler<T> resultHandler) {

        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }

        GetRequest getRequest = retrofit.create(GetRequest.class);

        // 构建请求
        Call<ResponseBody> call = getRequest.getUrl(url);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
            }
        });
    }



    /**
     * 发送GET网络请求
     *
     * @param url           请求地址
     * @param clazz         返回的数据类型
     * @param resultHandler 回调
     * @param <T>           泛型
     */
    public static <T> void sendGetRequest(String url, final Class<T> clazz, final ResultHandler<T> resultHandler) {

        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }

        GetRequest getRequest = retrofit.create(GetRequest.class);

        // 构建请求
        Call<ResponseBody> call = getRequest.getUrl(url);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendGetRequestFrom(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        GetRequest getRequest = retrofit.create(GetRequest.class);

        // 构建请求
        Call<ResponseBody> call = getRequest.getForm(url, paramMap);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendGetRequestJson(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {

            resultHandler.onFinish();
            return;
        }
        GetRequest getRequest = retrofit.create(GetRequest.class);

        // 构建请求
        Call<ResponseBody> call = getRequest.getJson(url, mapToJsonRequestBody(paramMap));
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPutRequest(String url, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PutRequest putRequest = retrofit.create(PutRequest.class);

        // 构建请求
        Call<ResponseBody> call = putRequest.putUrl(url);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPutRequestForm(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PutRequest putRequest = retrofit.create(PutRequest.class);

        // 构建请求
        Call<ResponseBody> call = putRequest.putForm(url, paramMap);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPutRequestJson(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PutRequest putRequest = retrofit.create(PutRequest.class);

        // 构建请求
        Call<ResponseBody> call = putRequest.putJson(url, mapToJsonRequestBody(paramMap));
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPostRequestForm(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);
        // 构建请求
        Call<ResponseBody> call = postRequest.postForm(url, paramMap);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPostRequest(String url, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);

        // 构建请求
        Call<ResponseBody> call = postRequest.postUrl(url);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPostRequestJson(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);

        // 构建请求
        Call<ResponseBody> call = postRequest.postJson(url, mapToJsonRequestBody(paramMap));
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendPostRequestStream(String url, Map<String, Object> paramMap, final Class<T> clazz, byte[] fileUtil, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        PostRequest postRequest = retrofit.create(PostRequest.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), fileUtil);
        // 构建请求
        Call<ResponseBody> call = postRequest.postStream(url, paramMap, requestBody);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendDeleteRequest(String url, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        DeleteRequest deleteRequest = retrofit.create(DeleteRequest.class);

        // 构建请求
        Call<ResponseBody> call = deleteRequest.deleteUrl(url);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendDeleteRequestForm(String url, Map<String, Object> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        DeleteRequest deleteRequest = retrofit.create(DeleteRequest.class);

        // 构建请求
        Call<ResponseBody> call = deleteRequest.deleteForm(url, paramMap);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void sendDeleteRequestJson(String url, Map<String, String> paramMap, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        DeleteRequest deleteRequest = retrofit.create(DeleteRequest.class);

        // 构建请求
        Call<ResponseBody> call = deleteRequest.deleteJson(url, mapToJsonRequestBody(paramMap));
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
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
    public static <T> void fileUpload(String url, File file, final Class<T> clazz, final ResultHandler<T> resultHandler) {
        // 判断网络连接状况
        if (resultHandler.isNetDisconnected()) {
            resultHandler.onFinish();
            return;
        }
        FileRequest fileRequest = retrofit.create(FileRequest.class);

        Map<String, RequestBody> paramMap = new HashMap<>(16);
        addMultiPart(paramMap, "file", file);

        // 构建请求
        Call<ResponseBody> call = fileRequest.postFile(url, paramMap);
        resultHandler.onBeforeRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dealWithResult(clazz, response, resultHandler);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resultHandler.onFailure(t);
                resultHandler.onFinish();
            }
        });
    }

    private static <T> void dealWithResult(final Class<T> clazz, Response<ResponseBody> response, final ResultHandler<T> resultHandler) {

        try {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body == null) {
                    resultHandler.onFailure(new Exception("No data response exception"));
                    resultHandler.onFinish();
                    return;
                }
                String string = body.string();
                T t = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(string, clazz);

                resultHandler.onSuccess(t);
                resultHandler.onFinish();
            } else {
                dealErrorResponse(response, resultHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
            resultHandler.onFailure(e);
            resultHandler.onFinish();
        }
    }

    private static void dealErrorResponse(Response response, ResultHandler resultHandler) {
        try {
            BadRequestBean badRequestBean = gson.fromJson(response.errorBody().string(), BadRequestBean.class);

            if (badRequestBean == null) {
                resultHandler.onFailure(new RuntimeException("Bad Request Data Error"));
            }

            if (resultHandler != null) {
                //如果是true，表示调用的地方已经处理，不需要底层再处理
                if (resultHandler.onError(badRequestBean)) return;

                resultHandler.handlerError(badRequestBean);
            }

        } catch (IOException e) {
            resultHandler.onFailure(e);
        }
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
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        // 网络框架
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL)
                .callbackExecutor(singleThreadPool)
                .build();

        FileRequest fileRequest = retrofit.create(FileRequest.class);
        // 构建请求
        Call<ResponseBody> call = fileRequest.download(url);
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
    public static RequestBody mapToJsonRequestBody(Map map) {
        String strEntity = gson.toJson(map);
        Log.e("=====json串", strEntity);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), strEntity);
        return body;
    }
}