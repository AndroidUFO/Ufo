package com.androidufo.ufo.core.call;

import com.androidufo.ufo.listener.UploadListener;
import com.androidufo.ufo.okhttp.convert.Converter;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public interface UploadCall<T> extends ICall<UploadCall<T>> {
    void execute(UploadListener<T> listener);
    UploadListener<T> getListener();
    UploadCall<T> responseConverter(Converter<T> converter);
}
