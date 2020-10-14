package com.androidufo.ufo.request.base;

import com.androidufo.ufo.Ufo;
import com.androidufo.commons.utils.RegexUtils;
import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.core.model.BaseHttpParams;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class BaseRequest<T, R extends BaseRequest<T, R, B, P>, B extends BaseHttpParams.Builder<P, B>, P extends BaseHttpParams<B, P>> {

    private HttpProtocol protocol;
    protected String url;
    protected HttpMethod httpMethod;
    protected B builder;

    protected abstract B createHttpParamsBuilder();

    public BaseRequest(@NotNull String url, @NotNull HttpMethod method) {
        checkUrl(url);
        this.url = url;
        this.httpMethod = method;
        builder = createHttpParamsBuilder();
    }

    public R httpProtocol(HttpProtocol protocol) {
        this.protocol = protocol;
        return (R) this;
    }

    private void checkUrl(String url) {
        if (!RegexUtils.isValidUrl(url)) {
            throw new RuntimeException("url格式不合法，请检查");
        }
    }

    public R urlEncode(boolean urlEncode) {
        builder.urlEncode(urlEncode);
        return (R) this;
    }

    public R query(@NotNull String key, @NotNull String value) {
        builder.query(key, value);
        return (R) this;
    }

    public R query(@NotNull Map<String, String> query) {
        builder.query(query);
        return (R) this;
    }

    public R header(@NotNull String key, @NotNull String value) {
        builder.header(key, value);
        return (R) this;
    }

    public R header(@NotNull Map<String, String> header) {
        builder.header(header);
        return (R) this;
    }

    protected HttpProtocol getHttpProtocol() {
        return protocol == null ? Ufo.getInstance().getHttpProtocol() : protocol;
    }

}
