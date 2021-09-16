package com.demo.retrofit.http;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.demo.retrofit.entity.BadRequestBean;
import com.demo.retrofit.entity.LoginResp;
import com.demo.retrofit.entity.TmpSecurityToken;
import com.demo.retrofit.entity.UserInfo;
import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OkHttpHelper {
    public static final String SERVER_URL_RELEASE = "https://api.sys.simplysmartframe.com";

    private OkHttpHelper() {
    }

    private static OkHttpHelper okHttpHelper = new OkHttpHelper();
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;
    private static ApiService apiService;
    private static HttpBaseParamsInterceptor baseParamsInterceptor;
    private static Gson gson;

    public static OkHttpHelper getInstance() {
        return okHttpHelper;
    }

    static {
        gson = new Gson();
        //http日志打印拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.e("Http>>>>", message);
            }
        });

        //添加公共参数拦截器
        baseParamsInterceptor = new HttpBaseParamsInterceptor
                .Builder()
                .addHeaderParam("x-api-key", "uio2vIija231JSTrzAGCy1Lu9hE0PROw8kOB89ks")
                .addParam("UploadClient", "android v3.1.2")
                .build();

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(baseParamsInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(SERVER_URL_RELEASE)
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public void login(Context context,@NonNull CustomCallBack<LoginResp> callBack) {
        Call<LoginResp> call = apiService.login("945401414@qq.com", "a123456", "password", "app");

        call.enqueue(new Callback<LoginResp>() {
            @Override
            public void onResponse(Call<LoginResp> call, Response<LoginResp> response) {

                if (response.isSuccessful()) {
                    LoginResp loginResp = response.body();

                    MMKV.defaultMMKV().putString("token", loginResp.token_type + " " + loginResp.access_token);
                    baseParamsInterceptor.headerParamsMap.put("Authorization", loginResp.token_type + " " + loginResp.access_token);

                    callBack.onSuccess(loginResp);
                    Toast.makeText(context, response.body().access_token, Toast.LENGTH_LONG).show();
                } else {
                    dealErrorResponse(response, callBack, context);
                }
            }

            @Override
            public void onFailure(Call<LoginResp> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getUserInfo(Context context,@NonNull CustomCallBack<UserInfo> callBack) {

        Call<UserInfo> call = apiService.getUserInfo();
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, response.body().lastName, Toast.LENGTH_LONG).show();
                    callBack.onSuccess(response.body());
                } else {
                    dealErrorResponse(response, callBack, context);
                }
                getOssToken(context);
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void getOssToken(Context context) {
        Call<TmpSecurityToken> call = apiService.getOssToken();
        call.enqueue(new Callback<TmpSecurityToken>() {
            @Override
            public void onResponse(Call<TmpSecurityToken> call, Response<TmpSecurityToken> response) {
                if(response.isSuccessful()){
                    TmpSecurityToken securityToken = response.body();
                    Toast.makeText(context, securityToken.securityToken, Toast.LENGTH_SHORT).show();
                }else{
                    dealErrorResponse(response,null,context);
                }
            }

            @Override
            public void onFailure(Call<TmpSecurityToken> call, Throwable t) {
                 call.request().method();
                 Log.e("http error>>>>>",call.request().method());
            }
        });
    }

    private void dealErrorResponse(Response response, CustomCallBack callBack, Context context) {
        try {
            BadRequestBean badRequestBean =  gson.fromJson(response.errorBody().string(), BadRequestBean.class);

            if(callBack!=null){
                //如果是true，表示调用的地方已经处理，不需要底层再处理
                if(callBack.onError(badRequestBean))return;
            }

            switch (badRequestBean.getCode()){
                case 1003:

                    break;
                case 2002:

                    break;
            }

        }catch (IOException e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
