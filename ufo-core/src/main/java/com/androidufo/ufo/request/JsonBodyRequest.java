package com.androidufo.ufo.request;

import com.androidufo.ufo.core.call.ResultCall;
import com.androidufo.ufo.core.generic.GenericResultType;
import com.androidufo.ufo.core.model.StringHttpParams;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.request.base.BaseRequest;
import com.androidufo.ufo.request.base.ICallBuilder;
import org.jetbrains.annotations.NotNull;

public class JsonBodyRequest<T> extends BaseRequest<T, JsonBodyRequest<T>, StringHttpParams.Builder, StringHttpParams> implements ICallBuilder<T, ResultCall<T>> {

    public JsonBodyRequest(@NotNull String url, @NotNull HttpMethod method) {
        super(url, method);
        builder.jsonBody(true);
    }

    public JsonBodyRequest<T> jsonBody(String json) {
        builder.bodyContent(json);
        return this;
    }

    @Override
    protected StringHttpParams.Builder createHttpParamsBuilder() {
        return new StringHttpParams.Builder();
    }

    @Override
    public ResultCall<T> newCall(GenericResultType<T> type) {
        return getHttpProtocol().httpJsonBody(
                httpMethod,
                builder.build(this.url).setResultType(type.get())
        );
    }

    @Override
    public ResultCall<T> newCall(Class<T> resultType) {
        return getHttpProtocol().httpJsonBody(
                httpMethod,
                builder.build(this.url).setResultType(resultType)
        );
    }

}
