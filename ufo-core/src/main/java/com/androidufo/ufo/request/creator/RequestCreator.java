package com.androidufo.ufo.request.creator;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.enums.HttpMethod;
import com.androidufo.ufo.request.FormBodyRequest;
import com.androidufo.ufo.request.JsonBodyRequest;
import com.androidufo.ufo.request.StringBodyRequest;
import org.jetbrains.annotations.NotNull;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public abstract class RequestCreator<T> {

    private String url;
    private HttpProtocol protocol;

    public RequestCreator(@NotNull String url) {
        this.url = url;
    }

    public StringBodyRequest<T> string() {
        return new StringBodyRequest<T>(url, httpMethod());
    }

    public JsonBodyRequest<T> json() {
        return new JsonBodyRequest<T>(url, httpMethod());
    }

    public FormBodyRequest<T> form() {
        return new FormBodyRequest<T>(url, httpMethod());
    }

    protected abstract HttpMethod httpMethod();
}
