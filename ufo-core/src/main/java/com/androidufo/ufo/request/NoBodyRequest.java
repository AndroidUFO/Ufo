package com.androidufo.ufo.request;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.core.call.ResultCall;
import com.androidufo.ufo.core.generic.GenericResultType;
import com.androidufo.ufo.core.model.NoBodyHttpParams;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.request.base.BaseRequest;
import com.androidufo.ufo.request.base.ICallBuilder;
import org.jetbrains.annotations.NotNull;

public class NoBodyRequest<T> extends BaseRequest<T, NoBodyRequest<T>, NoBodyHttpParams.Builder, NoBodyHttpParams> implements ICallBuilder<T, ResultCall<T>> {

    public NoBodyRequest(@NotNull String url, @NotNull HttpMethod httpMethod) {
        super(url, httpMethod);
    }

    @Override
    protected NoBodyHttpParams.Builder createHttpParamsBuilder() {
        return new NoBodyHttpParams.Builder();
    }

    @Override
    public ResultCall<T> newCall(GenericResultType<T> type) {
        return getHttpProtocol().httpNoBody(httpMethod, builder.build(this.url).setResultType(type.get()));
    }

    @Override
    public ResultCall<T> newCall(Class<T> resultType) {
        return getHttpProtocol().httpNoBody(httpMethod, builder.build(this.url).setResultType(resultType));
    }
}
