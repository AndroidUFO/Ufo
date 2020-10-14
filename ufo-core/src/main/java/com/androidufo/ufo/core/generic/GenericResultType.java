package com.androidufo.ufo.core.generic;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
public class GenericResultType<T> {

    private TypeToken<T> typeToken;

    public GenericResultType(@NotNull TypeToken<T> typeToken) {
        this.typeToken = typeToken;
    }

    public Type get() {
        return typeToken.getType();
    }
}
