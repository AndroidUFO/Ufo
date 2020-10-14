package com.androidufo.ufo.api.compiler.utils;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.api.compiler.model.MethodParamInfo;
import com.androidufo.ufo.api.compiler.enums.SupportAnnotation;

import java.util.ArrayList;
import java.util.List;

public class ParamsUtils {

    public static List<MethodParamInfo> getPathParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_PATH;
            }
        });
    }

    public static List<MethodParamInfo> getQueryParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_QUERY;
            }
        });
    }

    public static List<MethodParamInfo> getBodyParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_BODY;
            }
        });
    }

    public static List<MethodParamInfo> getHeaderParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_HEADER;
            }
        });
    }

    public static List<MethodParamInfo> getDownloadParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_DOWNLOAD_PATH;
            }
        });
    }

    public static List<MethodParamInfo> getUploadParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_UPLOAD_FILE;
            }
        });
    }

    public static List<MethodParamInfo> getUrlParamsFromAll(List<MethodParamInfo> allParamInfos) {
        return getTargetParams(allParamInfos, new Condition() {
            @Override
            public boolean match(SupportAnnotation supportAtt) {
                return supportAtt == SupportAnnotation.P_URL;
            }
        });
    }

    private static List<MethodParamInfo> getTargetParams(List<MethodParamInfo> allParamInfos, Condition condition) {
        if (EmptyUtils.collectionNull(allParamInfos) || condition == null) {
            return null;
        }
        List<MethodParamInfo> paramInfos = new ArrayList<>();
        for (MethodParamInfo info : allParamInfos) {
            SupportAnnotation supportAtt = info.getAttInfos().getSupportAtt();
            if (condition.match(supportAtt)) {
                paramInfos.add(info);
            }
        }
        return paramInfos;
    }

    private interface Condition {
        boolean match(SupportAnnotation supportAtt);
    }
}
