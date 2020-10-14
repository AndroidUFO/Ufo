package com.androidufo.ufo.api.utils;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.api.model.BodyParams;
import com.androidufo.ufo.api.model.HeaderParams;

import java.util.HashMap;
import java.util.Map;

public class MergeParamsUtils {

    public static Map<String, String> merge(HeaderParams... headers) {
        if (headers == null || headers.length == 0) return null;
        Map<String, String> map = new HashMap<>();
        for (HeaderParams header : headers) {
            if (header != null && !EmptyUtils.mapNull(header.toHeaders())) {
                map.putAll(header.toHeaders());
            }
        }
        return map;
    }

    public static Map<String, String> merge(BodyParams... params) {
        if (params == null || params.length == 0) return null;
        Map<String, String> map = new HashMap<>();
        for (BodyParams p : params) {
            if (p != null && !EmptyUtils.mapNull(p.toParams())) {
                map.putAll(p.toParams());
            }
        }
        return map;
    }

}
