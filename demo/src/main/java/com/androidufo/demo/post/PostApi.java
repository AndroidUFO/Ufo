package com.androidufo.demo.post;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.method.Post;
import com.androidufo.ufo.api.annos.params.Body;
import com.androidufo.ufo.api.model.BodyParams;
import com.androidufo.ufo.core.call.ResultCall;

@Api(baseUrl = "https://www.baidu.com")
public interface PostApi {
    @Post
    ResultCall<String> getBaiduInfos(@Body BodyParams params);
}
