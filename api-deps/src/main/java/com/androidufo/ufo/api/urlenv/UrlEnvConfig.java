package com.androidufo.ufo.api.urlenv;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.commons.utils.RegexUtils;

public class UrlEnvConfig {
    /**
     * 代表环境对应的名称
     */
    private final String envName;
    /**
     * 访问的url
     */
    private final String baseUrl;

    public UrlEnvConfig(String envName, String baseUrl) {
        this.envName = envName;
        this.baseUrl = baseUrl;
        if (EmptyUtils.stringNull(envName)) {
            throw new RuntimeException("环境名称envName不能为空");
        }
        if (EmptyUtils.stringNull(baseUrl)) {
            throw new RuntimeException("地址baseUrl不能为空");
        }
        if (!RegexUtils.isValidUrl(baseUrl)) {
            throw new RuntimeException(baseUrl + "不是一个合法且有效的url地址");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getEnvName() {
        return envName;
    }
}
