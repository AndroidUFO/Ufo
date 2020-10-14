package com.androidufo.ufo.listener;

import com.androidufo.ufo.enums.State;
import com.androidufo.ufo.model.Progress;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public interface UploadListener<T> extends ResultListener<T> {
    void onUploading(State uploadState, Progress progress);
}
