package com.androidufo.demo.upload;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.method.Upload;
import com.androidufo.ufo.api.annos.params.UploadFile;
import com.androidufo.ufo.api.model.UploadFileParams;
import com.androidufo.ufo.core.call.UploadCall;

@Api(baseUrl = "https://s9.pstatp.com/package/apk/")
public interface UploadApi {

    @Upload
    UploadCall<String> uploadFileToDouYin(@UploadFile UploadFileParams fileParams);

}
