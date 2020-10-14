package com.androidufo.ufo.model;

import com.androidufo.ufo.enums.ErrorType;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class Error {
    private ErrorType errType;
    private String desc;
    private Exception exception;

    public Error(String desc) {
        this.desc = desc;
        errType = ErrorType.COMMON_FAILED;
    }

    public Error(ErrorType error, String desc) {
        this.errType = error;
        this.desc = desc;
    }

    public Error(ErrorType errType, String desc, Exception exception) {
        this.errType = errType;
        this.desc = desc;
        this.exception = exception;
    }

    public Error(String desc, Exception exception) {
        this.errType = ErrorType.COMMON_FAILED;
        this.desc = desc;
        this.exception = exception;
    }

    public ErrorType getErrType() {
        return errType;
    }

    public String getDesc() {
        return desc;
    }

    public Exception getException() {
        return exception;
    }
}

