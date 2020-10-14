package com.androidufo.ufo.api.model;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.commons.utils.GSonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class QueryParams {

    private Builder builder;

    private QueryParams(Builder builder) {
        this.builder = builder;
    }

    public Map<String, String> toParams() {
        return builder.params;
    }

    public static class Builder {
        private final Map<String, String> params = new HashMap<>();

        public Builder param(@NotNull String key, @NotNull String value) {
            params.put(key, value);
            return this;
        }

        public Builder params(@NotNull Map<String, String> map) {
            if (!EmptyUtils.mapNull(map)) {
                params.putAll(map);
            }
            return this;
        }

        public <T> Builder params(@NotNull T javaBean) {
            Map<String, String> map = GSonUtils.toMap(javaBean);
            if (!EmptyUtils.mapNull(map)) {
                params.putAll(map);
            }
            return this;
        }

        public QueryParams build() {
            return new QueryParams(this);
        }
    }

}
