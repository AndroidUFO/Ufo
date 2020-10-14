package com.androidufo.demo.urlenv;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.method.Get;
import com.androidufo.ufo.api.urlenv.MultipleUrlEnvConfigs;
import com.androidufo.ufo.api.urlenv.UrlEnvConfig;
import com.androidufo.ufo.core.call.ResultCall;

import java.util.ArrayList;
import java.util.List;

@Api
public interface UrlEnvApi extends MultipleUrlEnvConfigs {

    String SINA = "sina";
    String BAIDU = "baidu";

    @Override
    default List<UrlEnvConfig> initUrlEnvConfigs() {
        List<UrlEnvConfig> envConfigs = new ArrayList<>();
        envConfigs.add(new UrlEnvConfig(BAIDU, "https://www.baidu.com/"));
        envConfigs.add(new UrlEnvConfig(SINA, "https://www.sina.com.cn/"));
        return envConfigs;
    }

    @Get
    ResultCall<String> getWebHomeHtml();
}
