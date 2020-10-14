package com.androidufo.ufo.utils;

import com.androidufo.commons.utils.EmptyUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class UrlUtils {

    public static String contactUrl(String url, Map<String, String> queries, boolean urlEncode) {
        if (EmptyUtils.mapNull(queries)) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        if (url.endsWith("/")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("?");
        for (Map.Entry<String, String> entry : queries.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(key).append("=");
            if (EmptyUtils.stringNull(value)) {
                builder.append("");
            } else {
                if (urlEncode) {
                    try {
                        builder.append(URLEncoder.encode(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        builder.append(value);
                    }
                } else {
                    builder.append(value);
                }
            }
            builder.append("&");
        }
        return builder.deleteCharAt(builder.length() - 1).toString();
    }

    public static String contactUrlWithRestUrl(String baseUrl, String restUrl) {
        boolean baseEndSeparator = baseUrl.endsWith("/");
        boolean restStartSeparator = restUrl.startsWith("/");
        if (baseEndSeparator) {
            if (restStartSeparator) {
                return baseUrl + restUrl.substring(1);
            }
            return baseUrl + restUrl;
        } else {
            if (restStartSeparator) {
                return baseUrl + restUrl;
            }
            return baseUrl + "/" + restUrl;
        }
    }

}
