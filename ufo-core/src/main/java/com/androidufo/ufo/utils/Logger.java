package com.androidufo.ufo.utils;

import android.util.Log;
import com.androidufo.ufo.BuildConfig;
import com.androidufo.ufo.Ufo;

public class Logger {

    private static final String TAG = "UFO >>> ";

    public static void debug(String info) {
        if (Ufo.getInstance().isDebugMode()) {
            Log.d(TAG, info);
        }
    }

    public static void error(String error) {
        Log.e(TAG, error);
    }

}
