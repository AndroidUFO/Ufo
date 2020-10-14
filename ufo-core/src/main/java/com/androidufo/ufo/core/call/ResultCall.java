package com.androidufo.ufo.core.call;

import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.okhttp.convert.Converter;

public interface ResultCall<T> extends ICall<ResultCall<T>> {
    T executeSync() throws Exception;
    void execute(ResultListener<T> listener);
    // 设置自定义结果转换器，可用于自定义转换结果，当数据是加密数据时可以进行解密
    ResultCall<T> responseConverter(Converter<T> converter);
}
