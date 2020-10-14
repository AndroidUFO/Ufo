package com.androidufo.ufo.utils;

public class CodeParseUtils {

    public static String getErrorByCode(int code) {
        StringBuilder builder = new StringBuilder();
        switch (code) {
            case 400:
                builder.append("发送了错误请求，");
                break;
            case 401:
                builder.append("请求要求身份认证，");
                break;
            case 403:
                builder.append("服务器拒绝请求，");
                break;
            case 404:
                builder.append("请求不存在，");
                break;
            case 405:
                builder.append("请求被禁用，");
                break;
            case 410:
                builder.append("请求的资源已被删除，");
                break;
            case 500:
                builder.append("服务器内部错误，");
                break;
            case 503:
                builder.append("服务器目前无法使用，");
                break;
            default:
                builder.append("请求失败，");
        }
        builder.append("错误码：").append(code);
        return builder.toString();
    }

}
