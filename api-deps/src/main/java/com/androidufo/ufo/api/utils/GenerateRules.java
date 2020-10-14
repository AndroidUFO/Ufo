package com.androidufo.ufo.api.utils;

public class GenerateRules {

    private static final String AUTOWIRED_PREFIX = "Inject$$";
    private static final String API_PREFIX = "Api$$";
    public static final String CLASS_PKG = "com.androidufo.ufo.apt";

    // 生成类的名称
    public static String generateApiClazzName(String originApiName) {
        return API_PREFIX + originApiName;
    }

    // 生成类的包路径
    public static String generateApiClazzNamePkg() {
        return CLASS_PKG;
    }

    // 根据原始类，获取生成类的Class对象
    public static Class<?> getGenerateApiClazz(Class<?> api) throws ClassNotFoundException {
        return getGenerateApiClazz(api.getSimpleName());
    }

    public static Object createGenerateApiInstance(String originClazzName) throws Exception {
        return getGenerateApiClazz(originClazzName).newInstance();
    }

    public static Class<?> getGenerateApiClazz(String originApiName) throws ClassNotFoundException {
        String easyApiName = generateApiClazzNamePkg() + "." + generateApiClazzName(originApiName);
        return Class.forName(easyApiName);
    }

    public static String generateAutowiredClazzName(String fieldHostFullName, String fieldParamName) {
        return AUTOWIRED_PREFIX + fieldHostFullName.replace(".", "$") + "$$" + fieldParamName;
    }
}
