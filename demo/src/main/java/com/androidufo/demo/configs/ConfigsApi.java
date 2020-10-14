package com.androidufo.demo.configs;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.method.Get;
import com.androidufo.ufo.core.call.ResultCall;

// 需要修改更多的配置请定义配置类，然后使用属性httpConfigs指定配置类的class即可生效
@Api(
        baseUrl = "https://www.baidu.com",
        httpConfigs = MyHttpConfigs.class
)
public interface ConfigsApi {

    @Get
    ResultCall<String> getBaiduHtml();
}
