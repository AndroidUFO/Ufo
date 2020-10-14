package com.androidufo.ufo.api.compiler.enums;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.api.annos.method.*;
import com.androidufo.ufo.api.annos.params.*;

public enum SupportAnnotation {
    P_QUERY(Query.class.getCanonicalName(), null),
    P_BODY(Body.class.getCanonicalName(), null),
    P_HEADER(Header.class.getCanonicalName(), null),
    P_DOWNLOAD_PATH(DownloadPath.class.getCanonicalName(), null),
    P_UPLOAD_FILE(UploadFile.class.getCanonicalName(), null),
    P_PATH(Path.class.getCanonicalName(), null),
    P_URL(Url.class.getCanonicalName(), null),
    M_GET(Get.class.getCanonicalName(), "GET"),
    M_POST(Post.class.getCanonicalName(), "POST"),
    M_PUT(Put.class.getCanonicalName(), "PUT"),
    M_PATCH(Patch.class.getCanonicalName(), "PATCH"),
    M_DELETE(Delete.class.getCanonicalName(), "DELETE"),
    M_DOWNLOAD(Download.class.getCanonicalName(), null),
    M_UPLOAD(Upload.class.getCanonicalName(), null);

    SupportAnnotation(String pathName, String httpMethod) {
        this.pathName = pathName;
        this.httpMethod = httpMethod;
    }

    private String pathName;
    private String httpMethod;

    public String getPathName() {
        return pathName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public static SupportAnnotation transfer(String pathName) {
        if (EmptyUtils.stringNull(pathName)) {
            return null;
        }
        SupportAnnotation[] values = values();
        for (SupportAnnotation value : values) {
            if (value.getPathName().equals(pathName)) {
                return value;
            }
        }
        return null;
    }
}
