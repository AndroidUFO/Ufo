package com.androidufo.ufo.okhttp;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.Ufo;
import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.core.call.DownloadCall;
import com.androidufo.ufo.core.model.*;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.core.call.ResultCall;
import com.androidufo.ufo.core.call.UploadCall;

import com.androidufo.ufo.okhttp.body.UploadReqBody;
import com.androidufo.ufo.okhttp.call.OkDownloadCall;
import com.androidufo.ufo.okhttp.call.OkResultCall;
import com.androidufo.ufo.okhttp.call.OkUploadCall;
import com.androidufo.ufo.okhttp.configs.HttpConfigs;
import com.androidufo.ufo.okhttp.ssl.SSLCreator;
import com.androidufo.ufo.okhttp.utils.OkHttpUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.SocketFactory;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class OkHttpProtocol implements HttpProtocol {

    private OkHttpClient okHttpClient;
    private HttpConfigs httpConfigs;
    private InputStream certificate;
    private InputStream bks;
    private String bksPassword;

    public OkHttpProtocol() {
    }

    public OkHttpProtocol(InputStream certificate, InputStream bks, String bksPassword) {
        this.certificate = certificate;
        this.bks = bks;
        this.bksPassword = bksPassword;
    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder builder = Ufo.getInstance().getOkHttpClient().newBuilder();
        SSLCreator.SSLConfigs sslConfigs = SSLCreator.createSSLFactory(bks, bksPassword, certificate);
        builder.sslSocketFactory(sslConfigs.sslSocketFactory, sslConfigs.trustManager);
        builder.hostnameVerifier(sslConfigs.hostnameVerifier);
        // 获取配置
        setConfigs(builder, httpConfigs);
        return builder.build();
    }

    @Override
    public void setHttpConfigs(HttpConfigs httpConfigs) {
        this.httpConfigs = httpConfigs;
    }

    @Override
    public OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = createClient();
        }
        return okHttpClient;
    }

    @Override
    public <T> ResultCall<T> httpNoBody(@NotNull HttpMethod httpMethod, @NotNull NoBodyHttpParams httpParams) {
        return new OkResultCall<T>(
                OkHttpUtils.newRequest(
                        httpParams.url(),
                        httpMethod.name(),
                        null,
                        httpParams.headers()
                ),
                httpParams.resultType(),
                this
        );
    }

    @Override
    public <T> ResultCall<T> httpFormBody(@NotNull HttpMethod httpMethod, @NotNull FormHttpParams httpParams) {
        return new OkResultCall<T>(
                OkHttpUtils.newFormBodyRequest(
                        httpParams.url(),
                        httpMethod.name(),
                        httpParams.formBody(),
                        httpParams.headers()
                ),
                httpParams.resultType(),
                this
        );
    }

    @Override
    public <T> ResultCall<T> httpJsonBody(@NotNull HttpMethod httpMethod, @NotNull StringHttpParams httpParams) {
        return new OkResultCall<T>(
                OkHttpUtils.newRequestBodyRequest(
                        httpParams.url(),
                        httpMethod.name(),
                        httpParams.bodyContent(),
                        httpParams.headers()
                ),
                httpParams.resultType(),
                this
        );
    }

    @Override
    public <T> ResultCall<T> httpStringBody(@NotNull HttpMethod httpMethod, @NotNull StringHttpParams httpParams) {
        return new OkResultCall<T>(
                OkHttpUtils.newTextPlainRequest(
                        httpParams.url(),
                        httpMethod.name(),
                        httpParams.bodyContent(),
                        httpParams.headers()
                ),
                httpParams.resultType(),
                this
        );
    }

    @Override
    public <T> UploadCall<T> upload(@NotNull UploadHttpParams httpParams) {
        OkUploadCall<T> uploadCall = new OkUploadCall<>(httpParams.resultType(), this);
        RequestBody requestBody = OkHttpUtils.newMultipartFormBody(httpParams.files(), httpParams.formBody());
        UploadReqBody uploadReqBody = new UploadReqBody(requestBody, uploadCall);
        Request request = OkHttpUtils.newMultipartFormRequest(httpParams.url(), uploadReqBody, httpParams.headers());
        return uploadCall.setRequest(request);
    }

    @Override
    public DownloadCall download(DownloadHttpParams httpParams) {
        return new OkDownloadCall(
                OkHttpUtils.newRequest(
                        httpParams.url(),
                        HttpMethod.GET.name(),
                        null,
                        httpParams.headers()
                ),
                this,
                httpParams.fileDir(),
                httpParams.fileName()
        );
    }

    private void setConfigs(OkHttpClient.Builder builder, HttpConfigs configs) {
        if (configs == null) {
            return;
        }
        // 配置参数
        builder.readTimeout(configs.readTimeoutByMilliseconds(), TimeUnit.MILLISECONDS);
        builder.writeTimeout(configs.writeTimeoutByMilliseconds(), TimeUnit.MILLISECONDS);
        builder.connectTimeout(configs.connectTimeoutByMilliseconds(), TimeUnit.MILLISECONDS);
        Long callTimeoutByMilliseconds = configs.callTimeoutByMilliseconds();
        if (callTimeoutByMilliseconds != null) {
            builder.callTimeout(callTimeoutByMilliseconds, TimeUnit.MILLISECONDS);
        }
        Long pingIntervalByMilliseconds = configs.pingIntervalByMilliseconds();
        if (pingIntervalByMilliseconds != null) {
            builder.pingInterval(pingIntervalByMilliseconds, TimeUnit.MILLISECONDS);
        }
        Cache cache = configs.cache();
        if (cache != null) {
            builder.cache(cache);
        }
        Interceptor interceptor = configs.interceptor();
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        List<Interceptor> interceptorList = configs.interceptors();
        if (!EmptyUtils.collectionNull(interceptorList)) {
            for (Interceptor inter : interceptorList) {
                builder.addInterceptor(inter);
            }
        }
        Interceptor networkInterceptor = configs.networkInterceptor();
        if (networkInterceptor != null) {
            builder.addNetworkInterceptor(networkInterceptor);
        }
        List<Interceptor> networkInterceptors = configs.networkInterceptors();
        if (!EmptyUtils.collectionNull(networkInterceptors)) {
            for (Interceptor inter : networkInterceptors) {
                builder.addNetworkInterceptor(inter);
            }
        }
        Authenticator authenticator = configs.authenticator();
        if (authenticator != null) {
            builder.authenticator(authenticator);
        }
        CertificatePinner certificatePinner = configs.certificatePinner();
        if (certificatePinner != null) {
            builder.certificatePinner(certificatePinner);
        }
        ConnectionPool connectionPool = configs.connectionPool();
        if (connectionPool != null) {
            builder.connectionPool(connectionPool);
        }
        List<ConnectionSpec> connectionSpecs = configs.connectionSpecs();
        if (!EmptyUtils.collectionNull(connectionSpecs)) {
            builder.connectionSpecs(connectionSpecs);
        }
        CookieJar cookieJar = configs.cookieJar();
        if (cookieJar != null) {
            builder.cookieJar(cookieJar);
        }
        Dispatcher dispatcher = configs.dispatcher();
        if (dispatcher != null) {
            builder.dispatcher(dispatcher);
        }
        Dns dns = configs.dns();
        if (dns != null) {
            builder.dns(dns);
        }
        EventListener eventListener = configs.eventListener();
        if (eventListener != null) {
            builder.eventListener(eventListener);
        }
        EventListener.Factory factory = configs.eventListenerFactory();
        if (factory != null) {
            builder.eventListenerFactory(factory);
        }
        boolean followRedirects = configs.followRedirects();
        builder.followRedirects(followRedirects);

        Long minWebSocketMessageToCompress = configs.minWebSocketMessageToCompress();
        if (minWebSocketMessageToCompress != null) {
            builder.minWebSocketMessageToCompress(minWebSocketMessageToCompress);
        }

        boolean retryOnConnectionFailure = configs.retryOnConnectionFailure();
        builder.retryOnConnectionFailure(retryOnConnectionFailure);
        ProxySelector proxySelector = configs.proxySelector();
        if (proxySelector != null) {
            builder.proxySelector(proxySelector);
        }
        Proxy proxy = configs.proxy();
        if (proxy != null) {
            builder.proxy(proxy);
        }
        Authenticator proxyAuthenticator = configs.proxyAuthenticator();
        if (proxyAuthenticator != null) {
            builder.proxyAuthenticator(proxyAuthenticator);
        }
        List<Protocol> protocols = configs.protocols();
        if (!EmptyUtils.collectionNull(protocols)) {
            builder.protocols(protocols);
        }
    }


}
