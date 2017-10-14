package com.example.dell.musicsong;

import android.os.Build;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dell on 2017/9/27.
 * 拦截器
 */
public class HttpInterceptor implements Interceptor {
    private static final String UA = "User-Agent";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader(UA, makeUA())
                .build();
        return chain.proceed(request);
    }

    private String makeUA() {
        String s = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
        Log.e("myMessage", "makeUA: "+s);
        return Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
    }
}

