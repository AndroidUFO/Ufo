package com.androidufo.demo;

import android.app.Application;
import com.androidufo.ufo.Ufo;

public class UfoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 如果不需要配置https的证书，下面的init方法则可以不用调用
        Ufo.getInstance()
                .setDebugMode(true)
//                .setDownloadFileDir("填写你指定的文件下载目录，记得添加对应权限")
                .init(this);
    }
}
