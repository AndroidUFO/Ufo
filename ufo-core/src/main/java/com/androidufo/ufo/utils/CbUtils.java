package com.androidufo.ufo.utils;

import android.text.TextUtils;
import com.androidufo.ufo.core.call.ICall;
import com.androidufo.ufo.core.lifecycle.CallLifecycleManager;
import com.androidufo.ufo.dispatch.UIDispatcher;
import com.androidufo.ufo.enums.ErrorType;
import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.model.Error;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class CbUtils {

    public static void removeCallFromCache(ICall call) {
        if (!TextUtils.isEmpty(call.getGroupTag()) && call.canCancel()) {
            CallLifecycleManager.getManager().removeCall(call, call.getGroupTag());
        }
    }

    public static <T> void callbackError(ICall call, final ResultListener<T> listener, final Error error) {
        removeCallFromCache(call);
        if (listener == null) {
            return;
        }
        UIDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                listener.onError(error);
            }
        });
    }

    public static <T> void callbackResult(ICall call, final ResultListener<T> listener, final T result) {
        removeCallFromCache(call);
        if (listener == null) {
            return;
        }
        UIDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                listener.onResult(result);
            }
        });
    }

    public static Error parseToError(Exception e) {
        String msg = e.getMessage();
        ErrorType error = ErrorType.COMMON_FAILED;
        if (e instanceof ConnectException) {
            if (msgContains(msg, "failed to connect to")) {
                msg = "连接服务器失败";
                error = ErrorType.CONNECT_FAILED;
            }
        } else if (e instanceof SocketException) {
            if (msgContains(msg, "Socket closed")) {
                msg = "请求已取消";
                error = ErrorType.USER_CANCEL;
            }
        } else if (e instanceof SocketTimeoutException) {
            if (msgContains(msg, "failed to connect to")) {
                msg = "连接服务器失败";
                error = ErrorType.CONNECT_FAILED;
            } else {
                msg = "网络连接超时";
                error = ErrorType.SOCKET_TIMEOUT;
            }
        } else if (msg != null && msg.equals("Canceled")) {
            error = ErrorType.CALL_CANCELED_REQ;
        }
        return new Error(error, msg, e);
    }

    private static boolean msgContains(String msg, String target) {
        return msg != null && msg.contains(target);
    }
}
