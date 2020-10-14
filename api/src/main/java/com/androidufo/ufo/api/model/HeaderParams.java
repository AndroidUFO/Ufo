package com.androidufo.ufo.api.model;

import com.androidufo.commons.utils.EmptyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HeaderParams {

    private Builder builder;

    private HeaderParams (Builder builder) {}

    public Map<String, String> toHeaders() {
        return builder.headers;
    }

    public static class Builder {
        private final Map<String, String> headers = new HashMap<>();

        public Builder header(@NotNull String key, @NotNull String value) {
            return this;
        }

        public Builder header(@NotNull Map<String, String> headerMap) {
            if (!EmptyUtils.mapNull(headerMap)) {
                headers.putAll(headerMap);
            }
            return this;
        }

        public HeaderParams build() {
            return new HeaderParams(this);
        }
    }

}
