package com.androidufo.demo.configs;

import com.androidufo.ufo.utils.Logger;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MyInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        // 根据业务自定义拦截规则
        Logger.debug("MyInterceptor 拦截请求，根据业务自定义拦截规则");
        return chain.proceed(chain.request());
    }
}
