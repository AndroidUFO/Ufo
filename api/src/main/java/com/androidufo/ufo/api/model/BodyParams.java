package com.androidufo.ufo.api.model;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.commons.utils.GSonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BodyParams {

    private Builder builder;

    private BodyParams(Builder builder) {
        this.builder = builder;
    }

    public Map<String, String> toParams() {
        return builder.params;
    }

    public String stringBody() {
        return builder.stringBody;
    }

    public static class Builder {
        private final Map<String, String> params = new HashMap<>();
        private String stringBody;

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

        public <T> Builder stringBody(String body) {
            this.stringBody = body;
            return this;
        }

        public BodyParams build() {
            return new BodyParams(this);
        }
    }

}
