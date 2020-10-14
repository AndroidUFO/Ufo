package com.androidufo.ufo.okhttp.body;

import com.androidufo.ufo.core.call.DownloadCall;
import com.androidufo.ufo.dispatch.UIDispatcher;
import com.androidufo.ufo.listener.DownloadListener;
import com.androidufo.ufo.model.Progress;
import com.androidufo.ufo.enums.State;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class DownloadResBody extends ResponseBody {
    private final ResponseBody responseBody;
    private final DownloadCall downloadCall;
    private BufferedSource bufferedSource;
    private int lastPercent;

    public DownloadResBody(ResponseBody responseBody, DownloadCall downloadCall) {
        this.responseBody = responseBody;
        this.downloadCall = downloadCall;
    }

    @Override
    public long contentLength() {
        return responseBody != null ? responseBody.contentLength() : 0;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return responseBody != null ? responseBody.contentType() : null;
    }

    @NotNull
    @Override
    public BufferedSource source() {
        assert responseBody != null;
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source){
        if (source == null) {
            return null;
        }
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long total = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink,byteCount);
                if (downloadCall != null) {
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (total == 0) {
                        total = responseBody.contentLength();
                    }
                    int percent = (int) (100 * totalBytesRead / total);
                    boolean complete = bytesRead == -1;
                    if (percent == 0) {
                        update(State.START, new Progress(totalBytesRead, total, percent));
                    } else if (complete) { // 这里不使用percent判断，否则100进度会调用两次
                        update(State.COMPLETE, new Progress(totalBytesRead, total, percent));
                    } else {
                        if (percent > lastPercent) {
                            // 只有进度发生变化才更新
                            update(State.IN_PROGRESS, new Progress(totalBytesRead, total, percent));
                        }
                    }
                    lastPercent = percent;
                }
                return bytesRead;
            }
        };
    }

    private void update(final State downloadState, final Progress progress) {
        final DownloadListener listener = downloadCall.getListener();
        if (listener == null) {
            return;
        }
        UIDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                listener.onDownloading(downloadState, downloadCall.downloadFilePath(), progress);
            }
        });
    }

}
