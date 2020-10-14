package com.androidufo.ufo.okhttp.body;

import com.androidufo.ufo.dispatch.UIDispatcher;
import com.androidufo.ufo.listener.UploadListener;
import com.androidufo.ufo.model.Progress;
import com.androidufo.ufo.core.call.UploadCall;
import com.androidufo.ufo.enums.State;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class UploadReqBody extends RequestBody {

    private final RequestBody requestBody;
    private final UploadCall uploadCall;
    private BufferedSink bufferedSink;
    private int lastPercent;

    public UploadReqBody(RequestBody requestBody, UploadCall uploadCall) {
        this.requestBody = requestBody;
        this.uploadCall = uploadCall;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody == null ? null : requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody == null ? 0 : requestBody.contentLength();
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        if (bufferedSink==null){
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //刷新
        bufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {

        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (uploadCall == null) {
                    return;
                }
                if (contentLength == 0){
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                int percent = (int) (100 * bytesWritten / contentLength);
//                boolean complete = bytesWritten == contentLength;
                //回调
                if (percent == 0) {
                    // 开始
                    update(State.START, new Progress(bytesWritten, contentLength, percent));
                } else if (percent == 100) {
                    // 完成
                    update(State.COMPLETE, new Progress(bytesWritten, contentLength, percent));
                } else {
                    // 进度
                    if (percent > lastPercent) {
                        // 只有进度发生变化才更新
                        update(State.IN_PROGRESS, new Progress(bytesWritten, contentLength, percent));
                    }
                }
                lastPercent = percent;
            }
        };
    }

    private void update(final State uploadState, final Progress progress) {
        final UploadListener listener = uploadCall.getListener();
        if (listener == null) {
            return;
        }
        UIDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                listener.onUploading(uploadState, progress);
            }
        });
    }

}
