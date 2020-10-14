package com.androidufo.ufo.core.model;

public class StringHttpParams extends BaseHttpParams<StringHttpParams.Builder, StringHttpParams> {

    private StringHttpParams(Builder builder) {
        super(builder);
    }

    public boolean jsonBody() {
        return this.builder.jsonBody;
    }

    public String bodyContent() {
        return this.builder.bodyContent;
    }

    public static class Builder extends BaseHttpParams.Builder<StringHttpParams, Builder> {

        private String bodyContent;
        // 如果为true代表json请求体，否则为string请求体
        private boolean jsonBody;

        public Builder bodyContent(String content) {
            this.bodyContent = content;
            return this;
        }

        public Builder jsonBody(boolean jsonBody) {
            this.jsonBody = jsonBody;
            return this;
        }

        @Override
        public StringHttpParams createHttpParams() {
            return new StringHttpParams(this);
        }
    }
}
