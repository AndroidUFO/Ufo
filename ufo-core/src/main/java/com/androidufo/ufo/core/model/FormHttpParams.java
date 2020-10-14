package com.androidufo.ufo.core.model;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.commons.utils.GSonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FormHttpParams extends BaseHttpParams<FormHttpParams.Builder, FormHttpParams> {

    private FormHttpParams(Builder builder) {
        super(builder);
    }

    public Map<String, String> formBody() {
        return builder.formBody;
    }

    public static class Builder extends BaseHttpParams.Builder<FormHttpParams, Builder> {

        private Map<String, String> formBody;

        public Builder body(@NotNull String key, @NotNull String value) {
            checkBodies();
            formBody.put(key, value);
            return this;
        }

        public Builder body(@NotNull Map<String, String> bodyMap) {
            if (!EmptyUtils.mapNull(bodyMap)) {
                checkBodies();
                formBody.putAll(bodyMap);
            }
            return this;
        }

        public Builder body(Object javaBean) {
            if (javaBean != null) {
                Map<String, String> map = GSonUtils.toMap(javaBean);
                if (!EmptyUtils.mapNull(map)) {
                    checkBodies();
                    formBody.putAll(map);
                }
            }
            return this;
        }

        private void checkBodies() {
            if (formBody == null) {
                formBody = new HashMap<>();
            }
        }

        @Override
        public FormHttpParams createHttpParams() {
            return new FormHttpParams(this);
        }
    }
}
