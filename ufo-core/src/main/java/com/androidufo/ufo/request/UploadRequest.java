package com.androidufo.ufo.request;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.core.call.UploadCall;
import com.androidufo.ufo.core.generic.GenericResultType;
import com.androidufo.ufo.core.model.UploadHttpParams;
import com.androidufo.ufo.request.base.BaseRequest;
import com.androidufo.ufo.request.base.ICallBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UploadRequest<T> extends BaseRequest<T, UploadRequest<T>, UploadHttpParams.Builder, UploadHttpParams> implements ICallBuilder<T, UploadCall<T>> {

    public UploadRequest(@NotNull String url) {
        super(url, HttpMethod.POST);
    }

    public UploadRequest<T> file(@NotNull String key, @NotNull String fileName) {
        builder.file(key, fileName);
        return this;
    }

    public UploadRequest<T> file(@NotNull String key, @NotNull File file) {
        builder.file(key, file);
        return this;
    }

    public UploadRequest<T> file(@NotNull String fileName) {
        builder.file(fileName);
        return this;
    }

    public UploadRequest<T> file(@NotNull File file) {
        builder.file(file);
        return this;
    }

    public UploadRequest<T> files(@NotNull List<File> fileList) {
        builder.files(fileList);
        return this;
    }

    public UploadRequest<T> files(@NotNull Map<String, File> fileMap) {
        builder.files(fileMap);
        return this;
    }

    public UploadRequest<T> fileNames(@NotNull List<String> fileList) {
        builder.fileNames(fileList);
        return this;
    }

    public UploadRequest<T> fileNames(@NotNull Map<String, String> fileMap) {
        builder.fileNames(fileMap);
        return this;
    }

    @Override
    public UploadCall<T> newCall(@NotNull GenericResultType<T> type) {
        return getHttpProtocol().upload(builder.build(this.url).setResultType(type.get()));
    }


    @Override
    public UploadCall<T> newCall(@NotNull Class<T> resultType) {
        return getHttpProtocol().upload(builder.build(this.url).setResultType(resultType));
    }

    @Override
    protected UploadHttpParams.Builder createHttpParamsBuilder() {
        return new UploadHttpParams.Builder();
    }
}
