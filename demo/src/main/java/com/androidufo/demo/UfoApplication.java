package com.androidufo.demo;

import android.app.Application;
import com.androidufo.ufo.Ufo;

public class UfoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Ufo.getInstance()
                .setDebugMode(true)
//                .setDownloadFileDir("填写你指定的文件下载目录，记得添加对应权限")
                .init(this);
    }
}
