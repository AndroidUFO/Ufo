package com.androidufo.ufo.okhttp.call;

import androidx.lifecycle.LifecycleOwner;

import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.core.lifecycle.CallLifecycleManager;
import com.androidufo.ufo.enums.ErrorType;
import com.androidufo.ufo.exceptions.UfoException;
import com.androidufo.ufo.okhttp.convert.Converter;
import com.androidufo.ufo.utils.CodeParseUtils;
import com.androidufo.commons.utils.GSonUtils;
import com.androidufo.ufo.core.call.ICall;

import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public abstract class CommonResultCall<T, R extends ICall<R>> implements ICall<R> {

    protected Request request;
    protected Type resultType;
    protected HttpProtocol httpProtocol;
    protected Call call;
    private String groupTag;

    public CommonResultCall(Request request, @NotNull Type resultType, @NotNull HttpProtocol protocol) {
        this.request = request;
        this.resultType = resultType;
        this.httpProtocol = protocol;
        createRawCall();
    }

    protected abstract void createRawCall();

    @Override
    public void cancel() {
        if (canCancel()) {
            call.cancel();
        }
    }

    @Override
    public boolean canCancel() {
        return call != null && !call.isCanceled();
    }

    @Override
    public String getGroupTag() {
        return groupTag;
    }

    @Override
    public R bindLifecycle(LifecycleOwner owner) {
        if (owner != null) {
            groupTag = CallLifecycleManager.getManager().addCall(this, owner);
        }
        return (R) this;
    }

    protected T parseResponse(Response response, final Type resultType, final Converter<T> converter) throws Exception {
        // 如果有自定义转换器，那么使用，没有就按照常规解析
        CustomParser<T> customParser = null;
        if (converter != null) {
            customParser = new CustomParser<T>() {
                @Override
                public T parse(String bodyStr) throws Exception {
                    return converter.convert(bodyStr, resultType);
                }
            };
        }
        return parseResponse(response, resultType, customParser);
    }

    private T parseResponse(Response response, Type resultType, CustomParser<T> customParser) throws Exception {
        int code = response.code();
        if (!response.isSuccessful()) {
            throw new UfoException(ErrorType.COMMON_FAILED, CodeParseUtils.getErrorByCode(code));
        }
        ResponseBody body = response.body();
        if (body == null) {
            throw new UfoException(ErrorType.COMMON_FAILED, "请求数据失败，响应为空");
        }
        String bodyStr = body.string();
        if (customParser != null) {
            return customParser.parse(bodyStr);
        }
        if (Void.class == resultType) {
            return null;
        }
        if (String.class == resultType) {
            return (T) bodyStr;
        }
        T result = GSonUtils.toBean(bodyStr, resultType);
        if (result == null) {
            throw new UfoException(ErrorType.GSON_FORMAT_FAILED, "GSON解析返回结果失败");
        }
        return result;
    }

    private interface CustomParser<T> {
        T parse(String bodyStr) throws Exception;
    }
}
