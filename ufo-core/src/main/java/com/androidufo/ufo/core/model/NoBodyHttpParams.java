package com.androidufo.ufo.core.model;

public class NoBodyHttpParams extends BaseHttpParams<NoBodyHttpParams.Builder, NoBodyHttpParams> {

    private NoBodyHttpParams(Builder builder) {
        super(builder);
    }

    public static class Builder extends BaseHttpParams.Builder<NoBodyHttpParams, Builder> {

        @Override
        public NoBodyHttpParams createHttpParams() {
            return new NoBodyHttpParams(this);
        }
    }
}
