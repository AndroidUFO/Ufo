package com.androidufo.ufo.okhttp.convert;

import java.lang.reflect.Type;

public interface Converter<T> {
    T convert(String response, Type resultType) throws Exception;
}
