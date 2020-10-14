package com.androidufo.ufo.enums;

/**
 * 作者：Created by fengYeChenXi on 2018/7/27.
 * 邮箱：3160744624@qq.com
 * github：https://github.com/fengyechenxi
 */
/**
 * http请求错误类型枚举
 */
public enum ErrorType {
    SOCKET_TIMEOUT, // 连接超时
    CONNECT_FAILED, // 连接失败，包含无法找到服务器地址，或者网络太差
    USER_CANCEL, // 用户主动调用了请求取消，或者Activity和Fragment生命周期结束后，自动取消请求
    CALL_CANCELED_REQ, // 执行了一个已经被取消了的请求
    GSON_FORMAT_FAILED, // gson解析数据失败
    COMMON_FAILED; // 其他暂时还未具体区分的错误，都包含在里面
}

