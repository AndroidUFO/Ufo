package com.androidufo.ufo.listener;

import com.androidufo.ufo.model.Progress;
import com.androidufo.ufo.enums.State;
import com.androidufo.ufo.model.Error;

public interface DownloadListener {
    void onDownloading(State downloadState, String fileName, Progress progress);
    void onDownloadFailed(Error error);
}
