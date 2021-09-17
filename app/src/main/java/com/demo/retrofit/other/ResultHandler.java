package com.demo.retrofit.other;

import android.content.Context;
import android.widget.Toast;

import com.demo.retrofit.entity.BadRequestBean;
import com.demo.retrofit.util.NetworkUtil;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * 网络请求结果处理类
 *
 * @param <T> 请求结果封装对象
 */
public abstract class ResultHandler<T> {
    Context context;

    public ResultHandler(Context context) {
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
     * 发出请求之前
     */
    public abstract void onBeforeRequest();

    /**
     * 请求成功时
     *
     * @param t 结果数据
     */
    public abstract void onSuccess(T t);

    /**
     * 请求失败
     */
    public abstract boolean onError(BadRequestBean badRequestBean);

    /**
     * 结束回调
     */
    public abstract void onFinish();

    /**
     * 请求出现异常
     *
     * @param t
     */
    public void onFailure(Throwable t) {
        if (t instanceof SocketTimeoutException || t instanceof ConnectException) {
            // 连接异常
            if (NetworkUtil.isNetworkConnected(context)) {
                // 服务器连接出错
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            } else {
                // 手机网络不通
                Toast.makeText(context, "Connect error", Toast.LENGTH_SHORT).show();
            }
        } else if (t instanceof Exception) {
            // 功能异常
            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void handlerError(BadRequestBean badRequestBean){
        switch (badRequestBean.getCode()){
            case 10008:
                break;


            default:
                break;
        }
    }

}