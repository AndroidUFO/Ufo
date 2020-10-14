package com.androidufo.ufo.okhttp.call;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.exceptions.UfoException;
import com.androidufo.ufo.listener.UploadListener;
import com.androidufo.ufo.model.Error;
import com.androidufo.ufo.core.call.UploadCall;
import com.androidufo.ufo.okhttp.convert.Converter;
import com.androidufo.ufo.utils.CbUtils;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class OkUploadCall<T> extends CommonResultCall<T, UploadCall<T>> implements UploadCall<T> {

    private Converter<T> converter;
    private UploadListener<T> listener;

    public OkUploadCall(@NotNull Type resultType, @NotNull HttpProtocol protocol) {
        super(null, resultType, protocol);
    }

    @Override
    protected void createRawCall() {
        if (request == null) {
            return;
        }
        this.call = httpProtocol.getOkHttpClient().newCall(request);
    }

    public OkUploadCall<T> setRequest(@NotNull Request request) {
        this.request = request;
        createRawCall();
        return this;
    }

    @Override
    public void execute(final UploadListener<T> listener) {
        this.listener = listener;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                CbUtils.callbackError(OkUploadCall.this, listener, CbUtils.parseToError(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    T result = parseResponse(response, resultType, converter);
                    CbUtils.callbackResult(OkUploadCall.this, listener, result);
                } catch (Exception e) {
                    Error error;
                    if (e instanceof UfoException) {
                        UfoException exception = (UfoException) e;
                        error = new Error(exception.getErrorType(), exception.getMessage(), e);
                    } else {
                        error = new Error(e.getMessage(), e);
                    }
                    CbUtils.callbackError(OkUploadCall.this, listener, error);
                }
            }
        });
    }

    @Override
    public UploadListener<T> getListener() {
        return listener;
    }

    @Override
    public OkUploadCall<T> responseConverter(Converter<T> converter) {
        this.converter = converter;
        return this;
    }

}
