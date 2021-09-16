package com.demo.retrofit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.demo.retrofit.entity.BadRequestBean;
import com.demo.retrofit.entity.LoginResp;
import com.demo.retrofit.entity.TmpSecurityToken;
import com.demo.retrofit.entity.UserInfo;
import com.demo.retrofit.http.ApiService;
import com.demo.retrofit.http.CustomCallBack;
import com.demo.retrofit.http.HttpBaseParamsInterceptor;
import com.demo.retrofit.http.OkHttpHelper;
import com.tencent.mmkv.MMKV;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String SERVER_URL_RELEASE = "https://api.sys.simplysmartframe.com";



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