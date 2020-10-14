package com.androidufo.ufo.core.call;

import com.androidufo.ufo.listener.DownloadListener;

public interface DownloadCall extends ICall<DownloadCall> {
    void execute(DownloadListener listener);
    DownloadListener getListener();
    String downloadFilePath();
}
