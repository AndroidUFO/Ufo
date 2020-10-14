package com.androidufo.ufo;

import android.Manifest;

import android.content.Context;
import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.okhttp.configs.HttpConfigs;
import com.androidufo.ufo.okhttp.ssl.SSLCreator;
import com.androidufo.ufo.request.DownloadRequest;
import com.androidufo.ufo.request.NoBodyRequest;
import com.androidufo.ufo.request.creator.RequestCreator;
import com.androidufo.ufo.okhttp.OkHttpProtocol;
import com.androidufo.ufo.request.UploadRequest;

import com.androidufo.ufo.utils.StorageUtils;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import androidx.annotation.RequiresPermission;

import java.util.concurrent.TimeUnit;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class Ufo {

    private Context context;
    private HttpProtocol httpProtocol;
    private String downloadDir;
    private OkHttpClient okHttpClient;
    private boolean debugMode;

    private Ufo() {
        createOkHttpClient();
    }

    public static Ufo getInstance() {
        return Holder.UFO;
    }

    public void init(Context context) {
        if (this.context == null && context != null) {
            this.context = context.getApplicationContext();
        }
    }

    public Context getContext() {
        if (context == null) {
            throw new RuntimeException("ufo need context, you must call init first");
        }
        return context;
    }

    private void createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(HttpConfigs.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.connectTimeout(HttpConfigs.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.writeTimeout(HttpConfigs.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        SSLCreator.SSLConfigs sslConfigs = SSLCreator.createSSLFactory(null, null);
        builder.sslSocketFactory(sslConfigs.sslSocketFactory, sslConfigs.trustManager);
        builder.hostnameVerifier(sslConfigs.hostnameVerifier);
        okHttpClient = builder.build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    private HttpProtocol defaultDelegate() {
        return new OkHttpProtocol();
    }

    /**
     * 使用自定义核心层网络访问框架代理初始化
     * @param customHttpDelegate 自定义核心层网络访问代理
     */
    public <T extends HttpProtocol> Ufo initByCustomArch(@NotNull T customHttpDelegate) {
        checkValid();
        return setHttpProtocol(customHttpDelegate);
    }

    private Ufo setHttpProtocol(@NotNull HttpProtocol delegate) {
        this.httpProtocol = delegate;
        return this;
    }

    public HttpProtocol getHttpProtocol() {
        if (httpProtocol == null) {
            httpProtocol = defaultDelegate();
        }
        return httpProtocol;
    }

    /**
     * 检查合法性，如果已经调用过其中一个初始化操作，就不能够重复调用
     */
    private void checkValid() {
        if (httpProtocol != null) {
            throw new RuntimeException("已经进行过初始化操作，不能够重复执行");
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    // 开启调试模式，开启后会打印日志
    public Ufo setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    /**
     * 设置全局下载文件所在的目录
     */
    public Ufo setDownloadFileDir(String downloadFileDir) {
        this.downloadDir = downloadFileDir;
        return this;
    }

    /**
     * 获取下载文件所在的目录
     */
    public String getDownloadDir() {
        return EmptyUtils.stringNull(downloadDir) ? StorageUtils.UFO_DIR : downloadDir;
    }

    public static <T> NoBodyRequest<T> get(@NotNull String url) {
        return new NoBodyRequest<T>(url, HttpMethod.GET);
    }

    public static <T> RequestCreator<T> post(@NotNull String url) {
        return new RequestCreator<T>(url) {
            @Override
            public HttpMethod httpMethod() {
                return HttpMethod.POST;
            }
        };
    }

    public static <T> RequestCreator<T> put(@NotNull String url) {
        return new RequestCreator<T>(url) {
            @Override
            public HttpMethod httpMethod() {
                return HttpMethod.PUT;
            }
        };
    }

    public static <T> RequestCreator<T> patch(@NotNull String url) {
        return new RequestCreator<T>(url) {
            @Override
            public HttpMethod httpMethod() {
                return HttpMethod.PATCH;
            }
        };
    }

    public static <T> RequestCreator<T> delete(@NotNull String url) {
        return new RequestCreator<T>(url) {
            @Override
            public HttpMethod httpMethod() {
                return HttpMethod.DELETE;
            }
        };
    }

    @RequiresPermission(value = Manifest.permission.READ_EXTERNAL_STORAGE)
    public static <T> UploadRequest<T> upload(@NotNull String url) {
        return new UploadRequest<T>(url);
    }

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static DownloadRequest download(@NotNull String url) {
        return new DownloadRequest(url);
    }

    private static class Holder {
        private static final Ufo UFO = new Ufo();
    }

}
