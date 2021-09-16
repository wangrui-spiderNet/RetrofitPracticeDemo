package com.demo.retrofit.http;

import com.demo.retrofit.entity.BadRequestBean;


public interface CustomCallBack<T> {

    void onSuccess(T t);
    boolean onError(BadRequestBean badRequestBean);

}
