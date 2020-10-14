package com.androidufo.ufo.api.compiler.consts;

import com.squareup.javapoet.ClassName;

import javax.lang.model.util.Elements;

public class DepsClazzPath {

    public static final String API_MODULE_PKG = "com.androidufo.ufo.api";
    private static final String UFO_CORE_MODULE_PKG = "com.androidufo.ufo";
    private static final String API_MODULE_ANNOTATION_PKG = API_MODULE_PKG + ".annos";
    /**
     * apt解析编译的注解路径
     */
    public static final String AN_API = API_MODULE_ANNOTATION_PKG + ".Api";
    public static final String AN_AUTOWIRED = API_MODULE_ANNOTATION_PKG + ".inject.Autowired";
    public static final String AN_CONFIGS = API_MODULE_ANNOTATION_PKG + ".configs.Configs";
    // 注解的属性参数key
    public static final String AN_A_REST_URL = "restUrl";
    public static final String AN_A_PATH_KEY = "key";
    public static final String AN_A_FORMAT = "format";
    // 参数依赖类
    public static final String MODEL_DOWNLOAD_PATH_PARAMS = API_MODULE_PKG + ".model.DownloadPathParams";
    public static final String MODEL_HEADER_PARAMS = API_MODULE_PKG + ".model.HeaderParams";
    public static final String MODEL_BODY_PARAMS = API_MODULE_PKG + ".model.BodyParams";
    public static final String MODEL_QUERY_PARAMS = API_MODULE_PKG + ".model.QueryParams";
    public static final String MODEL_UPLOAD_FILE_PARAMS = API_MODULE_PKG + ".model.UploadFileParams";
    // ufo-core依赖类
    public static final String CORE_UFO = UFO_CORE_MODULE_PKG + ".Ufo";
    public static final String CORE_HTTP_METHOD = UFO_CORE_MODULE_PKG + ".enums.HttpMethod";
    public static final String CORE_HTTP_CONFIGS = UFO_CORE_MODULE_PKG + ".okhttp.configs.HttpConfigs";
    // 其他依赖类
    public static final String CLAZZ_API_PROVIDER = API_MODULE_PKG + ".provider.Provider";
    public static final String CLAZZ_MERGE_PARAMS_UTILS = API_MODULE_PKG + ".utils.MergeParamsUtils";
    public static final String CLAZZ_URL_UTILS = UFO_CORE_MODULE_PKG + ".utils.UrlUtils";
    public static final String CLAZZ_GENERIC_RESULT_TYPE = UFO_CORE_MODULE_PKG + ".core.generic.GenericResultType";
    public static final String CLAZZ_TYPE_TOKEN = "com.google.gson.reflect.TypeToken";
    // 配置类
    public static final String CONFIGS_HOLDER = API_MODULE_PKG + ".";

    // 将类的完全路径名称转换成可以进行javaPoet拼接的ClassName
    public static ClassName toClassName(Elements elements, String clazzCanonicalName) {
        return ClassName.get(elements.getTypeElement(clazzCanonicalName));
    }
}
