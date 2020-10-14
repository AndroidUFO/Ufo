package com.androidufo.ufo.request;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.core.call.DownloadCall;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.core.model.DownloadHttpParams;
import com.androidufo.ufo.request.base.BaseRequest;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DownloadRequest extends BaseRequest<File, DownloadRequest, DownloadHttpParams.Builder, DownloadHttpParams> {

    public DownloadRequest(@NotNull String url) {
        super(url, HttpMethod.GET);
    }

    public DownloadRequest fileDir(String dirPath) {
        builder.fileDir(dirPath);
        return this;
    }

    public DownloadRequest fileName(String fileName) {
        builder.fileName(fileName);
        return this;
    }

    public DownloadRequest filePath(String fileDir, String fileName) {
        builder.fileDir(fileDir);
        builder.fileName(fileName);
        return this;
    }

    public DownloadCall newCall() {
        return getHttpProtocol().download(builder.build(this.url));
    }

    @Override
    protected DownloadHttpParams.Builder createHttpParamsBuilder() {
        return new DownloadHttpParams.Builder();
    }
}
