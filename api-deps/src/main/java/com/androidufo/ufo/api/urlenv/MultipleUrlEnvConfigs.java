package com.androidufo.ufo.api.urlenv;

import com.androidufo.ufo.api.annos.Api;

import java.util.List;

public interface MultipleUrlEnvConfigs {

    String INIT_URL_ENV_METHOD = "initUrlEnvConfigs";
    String ADD_URL_ENV_METHOD = "addUrlEnvConfig";
    String SWITCH_URL_ENV_METHOD = "switchUrlEnvConfig";
    String[] METHOD_NAME_ARRAY = new String[]{
            INIT_URL_ENV_METHOD,
            ADD_URL_ENV_METHOD,
            SWITCH_URL_ENV_METHOD
    };
    // 参数与上面的方法名字顺序对应，用来判断方法的
    Object[] METHOD_PARAMS_ARRAY = new Object[] {
            null,
            new Class[] {UrlEnvConfig.class},
            new Class[] {String.class}
    };

    List<UrlEnvConfig> initUrlEnvConfigs();
    void addUrlEnvConfig(UrlEnvConfig config);
    void switchUrlEnvConfig(String envName) throws Exception;

}
