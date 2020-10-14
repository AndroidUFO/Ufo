package com.androidufo.ufo.request;

import com.androidufo.ufo.core.call.ResultCall;
import com.androidufo.ufo.core.generic.GenericResultType;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.core.model.FormHttpParams;
import com.androidufo.ufo.request.base.BaseRequest;
import com.androidufo.ufo.request.base.ICallBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FormBodyRequest<T> extends BaseRequest<T, FormBodyRequest<T>, FormHttpParams.Builder, FormHttpParams> implements ICallBuilder<T, ResultCall<T>> {

    public FormBodyRequest(@NotNull String url, @NotNull HttpMethod method) {
        super(url, method);
    }

    public FormBodyRequest<T> body(@NotNull String key, @NotNull String value) {
        builder.body(key, value);
        return this;
    }

    public FormBodyRequest<T> body(@NotNull Map<String, String> bodyMap) {
        builder.body(bodyMap);
        return this;
    }

    public FormBodyRequest<T> body(Object javaBean) {
        builder.body(javaBean);
        return this;
    }

    @Override
    protected FormHttpParams.Builder createHttpParamsBuilder() {
        return new FormHttpParams.Builder();
    }

    @Override
    public ResultCall<T> newCall(GenericResultType<T> type) {
        return getHttpProtocol().httpFormBody(
                httpMethod,
                builder.build(this.url).setResultType(type.get())
        );
    }

    @Override
    public ResultCall<T> newCall(Class<T> resultType) {
        return getHttpProtocol().httpFormBody(
                httpMethod,
                builder.build(this.url).setResultType(resultType)
        );
    }
}
