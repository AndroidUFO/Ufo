package com.androidufo.ufo.core;

import com.androidufo.ufo.core.call.DownloadCall;
import com.androidufo.ufo.core.model.*;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.core.call.ResultCall;
import com.androidufo.ufo.core.call.UploadCall;
import com.androidufo.ufo.okhttp.configs.HttpConfigs;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */

/**
 * 注意：实现该接口的类必须要有无参构造方法
 */
public interface HttpProtocol {

    void setHttpConfigs(HttpConfigs httpConfigs);

    OkHttpClient getOkHttpClient();

    <T> ResultCall<T> httpNoBody(@NotNull HttpMethod httpMethod, @NotNull NoBodyHttpParams httpParams);

    <T> ResultCall<T> httpFormBody(@NotNull HttpMethod httpMethod, @NotNull FormHttpParams httpParams);

    <T> ResultCall<T> httpJsonBody(@NotNull HttpMethod httpMethod, @NotNull StringHttpParams httpParams);

    <T> ResultCall<T> httpStringBody(@NotNull HttpMethod httpMethod, @NotNull StringHttpParams httpParams);

    <T> UploadCall<T> upload(@NotNull UploadHttpParams httpParams);

    DownloadCall download(DownloadHttpParams httpParams);

}
