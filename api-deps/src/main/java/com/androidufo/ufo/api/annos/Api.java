package com.androidufo.ufo.api.annos;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
// 允许传递继承性
@Inherited
public @interface Api {
    /**
     * 网络访问的url：通常指域名
     */
    String baseUrl() default "";

    /**
     * 是否开启urlEncode
     */
    boolean urlEncode() default false;
    /**
     * 绑定的配置类
     */
    Class<?> httpConfigs() default Void.class;

    /**
     * https的证书在Assets文件夹下的路径
     */
    String assetsSslCer() default "";

    /**
     * bks证书在Assets文件夹下的路径
     */
    String assetsBks() default "";

    /**
     * bks证书的密码
     */
    String bksPassword() default "";

}
