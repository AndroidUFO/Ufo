package com.androidufo.ufo.request.base;

import com.androidufo.ufo.core.generic.GenericResultType;

public interface ICallBuilder<T, C> {
    C newCall(GenericResultType<T> type);
    C newCall(Class<T> resultType);
}
