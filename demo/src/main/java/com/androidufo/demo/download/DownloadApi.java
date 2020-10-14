package com.androidufo.demo.download;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.method.Download;
import com.androidufo.ufo.api.annos.params.Url;
import com.androidufo.ufo.core.call.DownloadCall;

@Api(baseUrl = "http://192.168.1.22:8080")
public interface DownloadApi {

    @Download
    DownloadCall downloadApk(@Url String apkUrl);

}
