package com.androidufo.demo.configs;

import com.androidufo.ufo.okhttp.configs.HttpConfigs;
import okhttp3.Interceptor;

public class MyHttpConfigs extends HttpConfigs {
    // PS：HttpConfigs中已经定义了大部分可以配置的参数，如需要修改其他配置，则直接重写方法返回对象即可
    public MyHttpConfigs(String name) {
        super(name);
    }

    @Override
    public long readTimeoutByMilliseconds() {
        // 配置读超时时间，单位毫秒
        return 1000;
    }

    @Override
    public long connectTimeoutByMilliseconds() {
        // 配置读超时时间，单位毫秒
        return 1000;
    }

    @Override
    public Interceptor interceptor() {
        return new MyInterceptor();
    }
}
