package com.androidufo.ufo.okhttp.configs;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;

import javax.net.SocketFactory;

import com.androidufo.ufo.utils.Logger;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.CookieJar;
import okhttp3.Dispatcher;
import okhttp3.Dns;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.Protocol;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */

/**
 * 注意：这里返回对象的配置，默认都是返回null，代表不需要进行设置
 * 返回boolean和long类型的都是返回默认值，
 * 需要自定义哪个配置，就重写哪个方法
 */
public class HttpConfigs {

    public static final long DEFAULT_TIMEOUT = 30000;

    public HttpConfigs(String name) {
        Logger.debug(getClass().getSimpleName() + " init by name " + name);
    }

    public long readTimeoutByMilliseconds() {
        // 源码默认10秒
        return DEFAULT_TIMEOUT;
    }

    public long connectTimeoutByMilliseconds() {
        // 源码默认10秒
        return DEFAULT_TIMEOUT;
    }

    public long writeTimeoutByMilliseconds() {
        // 源码默认10秒
        return DEFAULT_TIMEOUT;
    }

    public Long callTimeoutByMilliseconds() {
        // 源码默认0
        return null;
    }

    public Long pingIntervalByMilliseconds() {
        // 源码默认0
        return null;
    }

    public Cache cache() {
        return null;
    }

    public Interceptor interceptor() {
        return null;
    }

    public List<Interceptor> interceptors() {
        return null;
    }

    public Interceptor networkInterceptor() {
        return null;
    }

    public List<Interceptor> networkInterceptors() {
        return null;
    }

    public Authenticator authenticator() {
        return null;
    }

    public CertificatePinner certificatePinner() {
        return null;
    }

    public ConnectionPool connectionPool() {
        return null;
    }

    public List<ConnectionSpec> connectionSpecs() {
        return null;
    }

    public CookieJar cookieJar() {
        return null;
    }

    public Dispatcher dispatcher() {
        return null;
    }

    public Dns dns() {
        return null;
    }

    public EventListener eventListener() {
        return null;
    }

    public EventListener.Factory eventListenerFactory() {
        return null;
    }

    public boolean followRedirects() {
        // 源码默认是true
        return true;
    }

    public Long minWebSocketMessageToCompress() {
        return null;
    }

    public SocketFactory socketFactory() {
        return null;
    }

    public boolean retryOnConnectionFailure() {
        // 源码默认为true
        return true;
    }

    public ProxySelector proxySelector() {
        return null;
    }

    public Proxy proxy() {
        return null;
    }

    public Authenticator proxyAuthenticator() {
        return null;
    }

    public List<Protocol> protocols() {
        return null;
    }

}
