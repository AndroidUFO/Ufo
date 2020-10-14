package com.androidufo.ufo.core.model;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.utils.UrlUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseHttpParams<B extends BaseHttpParams.Builder<P, B>, P extends BaseHttpParams<B, P>> {
    protected B builder;
    protected String url;
    private Type resultType;

    public BaseHttpParams(B builder) {
        this.builder = builder;
    }

    public String url() {
        return url;
    }

    public P setResultType(Type resultType) {
        this.resultType = resultType;
        return (P) this;
    }

    public Type resultType() {
        return resultType;
    }

    public Map<String, String> headers() {
        return builder.headers;
    }

    public static abstract class Builder<P extends BaseHttpParams<B, P>, B extends Builder<P, B>> {
        private boolean urlEncode;
        private Map<String, String> queries;
        protected Map<String, String> headers;

        public B urlEncode(boolean urlEncode) {
            this.urlEncode = urlEncode;
            return (B) this;
        }

        public B query(@NotNull String key, @NotNull String value) {
            checkQueries();
            queries.put(key, value);
            return (B) this;
        }

        public B query(@NotNull Map<String, String> query) {
            if (!EmptyUtils.mapNull(query)) {
                checkQueries();
                queries.putAll(query);
            }
            return (B) this;
        }

        public B header(@NotNull String key, @NotNull String value) {
            checkHeaders();
            headers.put(key, value);
            return (B) this;
        }

        public B header(@NotNull Map<String, String> header) {
            if (!EmptyUtils.mapNull(header)) {
                checkHeaders();
                headers.putAll(header);
            }
            return (B) this;
        }

        private void checkQueries() {
            if (queries == null) {
                queries = new HashMap<>();
            }
        }

        private void checkHeaders() {
            if (headers == null) {
                headers = new HashMap<>();
            }
        }

        public P build(String url) {
            P httpParams = createHttpParams();
            if (httpParams != null) {
                httpParams.url = UrlUtils.contactUrl(url, queries, urlEncode);
            }
            return httpParams;
        }

        public abstract P createHttpParams();

    }

}
