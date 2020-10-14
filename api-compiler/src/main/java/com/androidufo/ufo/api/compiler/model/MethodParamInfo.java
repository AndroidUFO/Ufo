package com.androidufo.ufo.api.compiler.model;

import javax.lang.model.type.TypeMirror;

/**
 * 封装方法参数的对象
 */
public class MethodParamInfo {
    private AnnotationInfos attInfos;
    private TypeMirror paramTypeMirror;
    private String keyName;
    private String paramName;

    public AnnotationInfos getAttInfos() {
        return attInfos;
    }

    public MethodParamInfo setAttInfos(AnnotationInfos attInfos) {
        this.attInfos = attInfos;
        return this;
    }

    public TypeMirror getParamTypeMirror() {
        return paramTypeMirror;
    }

    public MethodParamInfo setParamTypeMirror(TypeMirror paramTypeMirror) {
        this.paramTypeMirror = paramTypeMirror;
        return this;
    }

    public MethodParamInfo setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getParamName() {
        return paramName;
    }

    public MethodParamInfo setParamName(String paramName) {
        this.paramName = paramName;
        return this;
    }
}
