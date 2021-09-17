package com.demo.retrofit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.demo.retrofit.entity.BadRequestBean;
import com.demo.retrofit.entity.LoginResp;
import com.demo.retrofit.entity.UserInfo;
import com.demo.retrofit.http.CustomCallBack;
import com.demo.retrofit.http.OkHttpHelper;
import com.demo.retrofit.other.ResultHandler;
import com.demo.retrofit.other.HttpManager;
import com.tencent.mmkv.MMKV;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.demo.retrofit.http.ApiService.URL_LOGIN;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnSubmit)
    Button btnSubmit;
    @BindView(R.id.btnUserInfo)
    Button btnUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MMKV.initialize(this);
        ButterKnife.bind(this);

        btnSubmit.setOnClickListener(view -> {
            OkHttpHelper.getInstance().login(this, new CustomCallBack<LoginResp>() {
                @Override
                public void onSuccess(LoginResp loginResp) {

                }

                @Override
                public boolean onError(BadRequestBean badRequestBean) {

                    return false;
                }
            });

            Map<String, Object> params = new HashMap<>();

//            login(@Field("username") String username, @Field("password") String password, @Field("grant_type") String type, @Field("client_id")
            params.put("username", "945401414@qq.com");
            params.put("password", "a123456");
            params.put("grant_type", "password");
            params.put("client_id", "app");

            HttpManager.sendPostRequestForm(URL_LOGIN, params, LoginResp.class, new ResultHandler<LoginResp>(this) {
                @Override
                public void onBeforeRequest() {

                }

                @Override
                public void onSuccess(LoginResp loginResp) {

                    Log.e("http>>>>>", "TOKEN>>>>>:" + loginResp.access_token);
                }

                @Override
                public boolean onError(BadRequestBean badRequestBean) {
                    return false;
                }

                @Override
                public void onFinish() {

                }
            });
        });

        btnUserInfo.setOnClickListener(view -> {
            OkHttpHelper.getInstance().getUserInfo(this, new CustomCallBack<UserInfo>() {
                @Override
                public void onSuccess(UserInfo userInfo) {

                }

                @Override
                public boolean onError(BadRequestBean badRequestBean) {

                    return false;
                }
            });
        });
    }

}