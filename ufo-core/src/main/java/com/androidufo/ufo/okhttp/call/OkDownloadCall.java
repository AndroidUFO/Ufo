package com.androidufo.ufo.okhttp.call;

import com.androidufo.ufo.Ufo;
import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.core.call.DownloadCall;
import com.androidufo.ufo.dispatch.UIDispatcher;
import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.listener.DownloadListener;
import com.androidufo.ufo.model.Error;
import com.androidufo.ufo.okhttp.body.DownloadResBody;
import com.androidufo.ufo.utils.CbUtils;
import com.androidufo.ufo.utils.FileUtils;
import com.androidufo.ufo.utils.Logger;
import com.androidufo.ufo.utils.StorageUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class OkDownloadCall extends CommonResultCall<File, DownloadCall> implements DownloadCall {

    private DownloadListener listener;
    // 自定义的文件下载路径目录
    private final String fileDir;
    // 自定义的文件下载名称
    private final String fileName;
    // 文件实际下载时候的完全路径
    private String realFilePath;

    public OkDownloadCall(@NotNull Request request, @NotNull HttpProtocol protocol, String fileDir, String fileName) {
        super(request, File.class, protocol);
        this.fileDir = fileDir;
        this.fileName = fileName;
    }

    @Override
    protected void createRawCall() {
        OkHttpClient.Builder builder = httpProtocol.getOkHttpClient().newBuilder();
        builder.addNetworkInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new DownloadResBody(originalResponse.body(), OkDownloadCall.this))
                        .build();
            }
        });
        this.call = builder.build().newCall(request);
    }

    public void execute(final DownloadListener listener) {
        this.listener = listener;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callbackDownloadFailed(CbUtils.parseToError(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    // 获取下载路径
                    realFilePath = getDownloadFilePath(response);
                    InputStream inputStream = body.byteStream();
                    try {
                        FileUtils.writeFile(realFilePath, inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 回调下载失败
                        callbackDownloadFailed(CbUtils.parseToError(e));
                    }
                }
            }
        });
    }

    private void callbackDownloadFailed(final Error error) {
        CbUtils.removeCallFromCache(this);
        if (listener == null) {
            return;
        }
        UIDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                listener.onDownloadFailed(error);
            }
        });
    }

    private String getDownloadFilePath(Response response) {
        String fileDir = getFileDir();
        String fileName = getFileName(response);
        return StorageUtils.getFilePath(fileDir, fileName);
    }

    private String getFileName(Response response) {
        // 获取文件名字优先级：
        // （1）使用自定义名字；
        // （2）使用url获取的名字；
        // （3）使用header获取的名字；
        // （4）UUID
        if (!EmptyUtils.stringNull(fileName)) {
            return fileName;
        }
        // 获取媒体类型，如果文件名字没有.后缀结尾，则需要用mime去拼接
        String mime = response.header("Content-Type");
        String urlFileName = getUrlFileName(response);
        if (!EmptyUtils.stringNull(urlFileName)) {
            return appendMime(urlFileName, mime);
        }
        String headerFileName = getHeaderFileName(response);
        if (!EmptyUtils.stringNull(headerFileName)) {
            return appendMime(headerFileName, mime);
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return appendMime(uuid, mime);
    }

    private String appendMime(String fileName, String mime) {
        if (EmptyUtils.stringNull(mime)) {
            return fileName;
        }
        // 文件有后缀名
        if (fileName.lastIndexOf(".") != -1) {
            return fileName;
        }
        // 这里只处理常见的mime类型，不常见的直接返回fileName
        MediaType mediaType = MediaType.parse(mime);
        if (mediaType != null) {
            return fileName + "." + mediaType.subtype();
        }
        return fileName;
    }

    private String getFileDir() {
        // 如果下载路径为空，则使用默认的下载路径
        return EmptyUtils.stringNull(fileDir) ? Ufo.getInstance().getDownloadDir() : fileDir;
    }

    private String getUrlFileName(Response response) {
        HttpUrl httpUrl = response.request().url();
        List<String> list = httpUrl.pathSegments();
        Logger.debug(list.toString());
        if (!EmptyUtils.collectionNull(list)) {
            return list.get(list.size() - 1);
        }
        return null;
    }

    /**
     * 解析文件头
     * Content-Disposition:attachment;filename=FileName.txt
     * Content-Disposition: attachment; filename*="UTF-8''%E6%9B%BF%E6%8D%A2%E5%AE%9E%E9%AA%8C%E6%8A%A5%E5%91%8A.pdf"
     */
    private String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (!EmptyUtils.stringNull(dispositionHeader)) {
            dispositionHeader.replace("attachment;filename=", "");
            dispositionHeader.replace("filename*=utf-8", "");
            String[] strings = dispositionHeader.split("; ");
            if (strings.length > 1) {
                dispositionHeader = strings[1].replace("filename=", "");
                dispositionHeader = dispositionHeader.replace("\"", "");
                return dispositionHeader;
            }
            return "";
        }
        return "";
    }

    @Override
    public DownloadListener getListener() {
        return listener;
    }

    @Override
    public String downloadFilePath() {
        return realFilePath;
    }
}
