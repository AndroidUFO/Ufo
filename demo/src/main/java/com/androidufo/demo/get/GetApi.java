package com.androidufo.demo.get;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.method.Get;
import com.androidufo.ufo.api.annos.params.Query;
import com.androidufo.ufo.api.model.QueryParams;
import com.androidufo.ufo.core.call.ResultCall;

@Api(baseUrl = "https://www.baidu.com")
public interface GetApi {
    @Get
    ResultCall<String> getBaiduHtml(@Query QueryParams params);
}
