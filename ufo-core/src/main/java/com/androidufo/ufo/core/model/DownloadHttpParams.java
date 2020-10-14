package com.androidufo.ufo.core.model;

public class DownloadHttpParams extends BaseHttpParams<DownloadHttpParams.Builder, DownloadHttpParams> {

    private DownloadHttpParams(Builder builder) {
        super(builder);
    }

    public String fileDir() {
        return builder.downloadFileDir;
    }

    public String fileName() {
        return builder.downloadFileName;
    }

    public static class Builder extends BaseHttpParams.Builder<DownloadHttpParams, Builder> {

        private String downloadFileDir;
        private String downloadFileName;

        public Builder fileDir(String dir) {
            this.downloadFileDir = dir;
            return this;
        }

        public Builder fileName(String fileName) {
            this.downloadFileName = fileName;
            return this;
        }

        @Override
        public DownloadHttpParams createHttpParams() {
            return new DownloadHttpParams(this);
        }
    }
}
