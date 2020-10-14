package com.androidufo.ufo.dispatch;

import android.os.Handler;
import android.os.Looper;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class UIDispatcher {

    private final Handler dispatcher;

    private UIDispatcher() {
        dispatcher = new Handler(Looper.getMainLooper());
    }

    public static void dispatch(Runnable action) {
        if (action != null) {
            Holder.INSTANCE.dispatcher.post(action);
        }
    }

    private static class Holder {
        private static final UIDispatcher INSTANCE = new UIDispatcher();
    }
}
