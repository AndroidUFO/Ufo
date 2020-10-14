package com.androidufo.ufo.okhttp.utils;

import android.text.TextUtils;
import com.androidufo.commons.utils.EmptyUtils;
import okhttp3.*;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class OkHttpUtils {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.parse("application/x-www-from-urlencoded; charset=utf-8");
    public static final MediaType PLAIN = MediaType.parse("text/plain;charset=utf-8");
    private static final MediaType MULTI_FORM = MediaType.parse("multipart/form-data; charset=utf-8");
    private static final MediaType MULTI_FILE = MediaType.parse("application/octet-stream");

    public static Request newFormBodyRequest(
            String url,
            String methodName,
            Map<String, String> params,
            Map<String, String> headers
    ) {
        return newRequest(
                url,
                methodName,
                newFormBody(params),
                headers
        );
    }

    public static Request newRequestBodyRequest(
            String url,
            String methodName,
            String json,
            Map<String, String> headers
    ) {
        return newRequest(
                url,
                methodName,
                newRequestBody(json),
                headers
        );
    }

    public static Request newTextPlainRequest(
            String url,
            String methodName,
            String textPlain,
            Map<String, String> headers
    ) {
        return newRequest(
                url,
                methodName,
                newTextPlainBody(textPlain),
                headers
        );
    }

    public static Request newMultipartFormRequest(
            String url,
            Map<String, File> filesMap,
            Map<String, String> params,
            Map<String, String> headers
    ) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(newMultipartFormBody(filesMap, params));
        addHeaders(builder, headers);
        return builder.build();
    }

    public static Request newMultipartFormRequest(
            String url,
            RequestBody requestBody,
            Map<String, String> headers
    ) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(requestBody);
        addHeaders(builder, headers);
        return builder.build();
    }

    private static void addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (!EmptyUtils.mapNull(headers)) {
            builder.headers(Headers.of(headers));
        }
    }

    public static Request newRequest(
            String url,
            String methodName,
            RequestBody requestBody,
            Map<String, String> headers
    ) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .method(methodName, requestBody);
        addHeaders(builder, headers);
        return builder.build();
    }

    private static RequestBody newRequestBody(String json) {
        if (TextUtils.isEmpty(json)) {
            return RequestBody.create("{}", JSON);
        }
        return RequestBody.create(json, JSON);
    }

    private static RequestBody newTextPlainBody(String textPlain) {
        if (TextUtils.isEmpty(textPlain)) {
            return RequestBody.create("", PLAIN);
        }
        return RequestBody.create(textPlain, PLAIN);
    }

    private static RequestBody newFormBody(Map<String, String> bodyParams) {
        FormBody.Builder builder = new FormBody.Builder();
        if (!EmptyUtils.mapNull(bodyParams)) {
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.add(key == null ? "" : key, value == null ? "" : value);
            }
        }
        return builder.build();
    }

    public static RequestBody newMultipartFormBody(Map<String, File> filesMap, Map<String, String> paramsMap) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (!EmptyUtils.mapNull(filesMap)) {
            for (Map.Entry<String, File> entry : filesMap.entrySet()) {
                String fileKey = entry.getKey();
                File file = entry.getValue();
                if (file == null) {
                    continue;
                }
                String fileName = file.getName();
                String fileMimeType = getMimeType(fileName);
                builder.addFormDataPart(
                        fileKey,
                        fileName,
                        RequestBody.create(MediaType.parse(fileMimeType), file)
                );
            }
        }
        if (!EmptyUtils.mapNull(paramsMap)) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addFormDataPart(key, value == null ? "" : value);
            }
        }
        return builder.build();
    }

    private static String getMimeType(String filename) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }
}
