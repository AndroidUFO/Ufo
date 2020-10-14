package com.androidufo.ufo.okhttp.call;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.exceptions.UfoException;
import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.core.call.ResultCall;
import com.androidufo.ufo.model.Error;
import com.androidufo.ufo.okhttp.convert.Converter;
import com.androidufo.ufo.utils.CbUtils;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class OkResultCall<T> extends CommonResultCall<T, ResultCall<T>> implements ResultCall<T> {

    private Converter<T> converter;

    public OkResultCall(@NotNull Request request, @NotNull Type resultType, @NotNull HttpProtocol protocol) {
        super(request, resultType, protocol);
    }

    @Override
    protected void createRawCall() {
        this.call = httpProtocol.getOkHttpClient().newCall(request);
    }

    @Override
    public T executeSync() throws Exception {
        Response response = call.execute();
        return parseResponse(response, resultType, converter);
    }

    @Override
    public void execute(final ResultListener<T> listener) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                e.printStackTrace();
                CbUtils.callbackError(OkResultCall.this, listener, CbUtils.parseToError(e));
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                try {
                    T result = parseResponse(response, resultType, converter);
                    CbUtils.callbackResult(OkResultCall.this, listener, result);
                } catch (Exception e) {
                    Error error;
                    if (e instanceof UfoException) {
                        UfoException exception = (UfoException) e;
                        error = new Error(exception.getErrorType(), exception.getMessage(), e);
                    } else {
                        error = new Error(e.getMessage(), e);
                    }
                    CbUtils.callbackError(OkResultCall.this, listener, error);
                }
            }
        });
    }

    @Override
    public OkResultCall<T> responseConverter(Converter<T> converter) {
        this.converter = converter;
        return this;
    }
}
