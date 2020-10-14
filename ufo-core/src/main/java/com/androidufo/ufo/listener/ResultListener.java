package com.androidufo.ufo.listener;

import com.androidufo.ufo.model.Error;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public interface ResultListener<T> {
    void onError(Error error);
    void onResult(T result);
}
