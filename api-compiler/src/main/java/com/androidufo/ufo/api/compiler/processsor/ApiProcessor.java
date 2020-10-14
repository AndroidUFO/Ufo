package com.androidufo.ufo.api.compiler.processsor;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.commons.utils.GSonUtils;
import com.androidufo.commons.utils.RegexUtils;
import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.compiler.consts.DepsClazzPath;
import com.androidufo.ufo.api.compiler.model.*;
import com.androidufo.ufo.api.compiler.enums.SupportAnnotation;
import com.androidufo.ufo.api.compiler.utils.AnnotationUtils;
import com.androidufo.ufo.api.compiler.utils.ParamsUtils;
import com.androidufo.ufo.api.enums.BodyFormat;
import com.androidufo.ufo.api.model.HttpsInfos;
import com.androidufo.ufo.api.urlenv.MultipleUrlEnvConfigs;
import com.androidufo.ufo.api.urlenv.UrlEnvConfig;
import com.androidufo.ufo.api.utils.ClazzUtils;
import com.androidufo.ufo.api.utils.GenerateRules;
import com.androidufo.ufo.api.utils.RestUrlParser;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        DepsClazzPath.AN_API,
        DepsClazzPath.AN_CONFIGS
})
public class ApiProcessor extends AbsProcessor {

    private final String curUrlEnvFieldName = "curUrlEnv";
    private final String urlEnvListFieldName = "urlEnvList";
    private final String getUrlEnvByEnvNameMethod = "getUrlEnvByEnvName";
    // url变量
    private final String urlLocalParam = "ufoApiBaseUrl";

    private ClassName _UrlUtils;
    private ClassName _Provider;
    private ClassName _Ufo;
    private ClassName _HttpMethod;
    private ClassName _MergeParamsUtils;
    private ClassName _GenericResultType;
    private ClassName _TypeToken;

    @Override
    public synchronized void init(ProcessingEnvironment pEnv) {
        super.init(pEnv);
        // 注意，如果DepsClazzPath的路径写错了，编译会报错：java.lang.NullPointerException: element == null
        _UrlUtils = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CLAZZ_URL_UTILS);
        _Provider = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CLAZZ_API_PROVIDER);
        _Ufo = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CORE_UFO);
        _HttpMethod = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CORE_HTTP_METHOD);
        _MergeParamsUtils = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CLAZZ_MERGE_PARAMS_UTILS);
        _GenericResultType = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CLAZZ_GENERIC_RESULT_TYPE);
        _TypeToken = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CLAZZ_TYPE_TOKEN);
    }

    @Override
    public void handleAnnotations(RoundEnvironment roundEnvironment) {
        // 获取Element，解析带有@Api注解的接口类
        try {
            parseInterfaceWithApiAnnotation(roundEnvironment);
        } catch (Exception e) {
            // 报错，并打印编译错误信息
            printError(e.getMessage());
        }
    }

    private void parseInterfaceWithApiAnnotation(RoundEnvironment roundEnvironment) throws Exception {
        Set<? extends Element> apiElements = roundEnvironment.getElementsAnnotatedWith(Api.class);
        if (EmptyUtils.collectionNull(apiElements)) {
            return;
        }
        for (Element element : apiElements) {
            // 转换成类对应的Element
            TypeElement clazzElement = (TypeElement) element;
            // 类的完整名称（包含包路径）
            String qualifiedName = clazzElement.getQualifiedName().toString();
            // 规则：使用@Api注解定义的网络访问接口必须是interface，否则不能编译通过
            if (!clazzElement.getKind().isInterface()) {
                throwAptException("规则约定，使用注解@Api定义的网络访问类必须是interface接口类型，错误类：" + qualifiedName);
            }
            // 获取该interface是否支持多域名配置：
            // 定义了网络接口的类继承了MultipleDomainSupport接口或者MultipleDomainSupport的子类即代表支持多域名
            boolean multipleUrlEnv = ClazzUtils.isChildOrSameType(
                    elementUtils, typeUtils, element.asType(),
                    MultipleUrlEnvConfigs.class,
                    messager
            );
            // 获取interface上@Api注解对象
            Api api = element.getAnnotation(Api.class);
            // api注解中配置的baseUrl值
            String baseUrl = api.baseUrl();
            boolean urlEncode = api.urlEncode();
            String assetsSslCer = api.assetsSslCer();
            String assetsBks = api.assetsBks();
            String bksPassword = api.bksPassword();
            HttpsInfos httpsInfos = null;
            if (!EmptyUtils.stringNull(assetsSslCer)) {
                httpsInfos = new HttpsInfos(assetsSslCer, assetsBks, bksPassword);
            }
            // 获取httpConfigs
            String httpConfigsName = parseHttpConfigsClazz(element);
            // 规则：如果支持多域名，@Api上的baseUrl则可以为空，否则必须满足url正则
            if (!multipleUrlEnv) {
                if (EmptyUtils.stringNull(baseUrl)) {
                    throwAptException(qualifiedName + "接口注解@Api定义的baseUrl不能为空");
                }
                if (!RegexUtils.isValidUrl(baseUrl)) {
                    throwAptException(qualifiedName + "接口注解@Api定义的baseUrl不合法，请检查");
                }
            }
            // 生成interface对应的class类
            generateApiClass(clazzElement, baseUrl, urlEncode, httpConfigsName, httpsInfos, multipleUrlEnv);
        }
    }

    private String parseHttpConfigsClazz(Element element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            if (Api.class.getCanonicalName().equals(mirror.getAnnotationType().toString())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                    ExecutableElement key = entry.getKey();
                    AnnotationValue value = entry.getValue();
                    if (key.getSimpleName().toString().equals("httpConfigs")) {
                        String configsName = value.getValue().toString();
                        printMsg("configsName = " + configsName);
                        TypeMirror valueMirror = elementUtils.getTypeElement(configsName).asType();
                        if (!ClazzUtils.isChildOrSameType(
                                elementUtils,
                                typeUtils,
                                valueMirror,
                                DepsClazzPath.CORE_HTTP_CONFIGS,
                                messager
                        )) {
                            throwAptException("@Api注解绑定的httpConfigs属性的Class必须继承自" + DepsClazzPath.CORE_HTTP_CONFIGS);
                        }
                        return configsName;
                    }
                }
            }
        }
        return null;
    }

    // 生成定义网络访问接口对应的实现类
    private void generateApiClass(TypeElement clazzElement, String baseUrl,
                                  boolean urlEncode, String httpConfigsName,
                                  HttpsInfos httpsInfos, boolean multipleUrlEnv) {
        // 操作两大步骤：
        // （1）先将该接口类的所有方法进行解析成需要的数据；
        // （2）利用解析得到的数据，使用JavaPoet拼接方法；
        String apiClazzName = GenerateRules.generateApiClazzName(clazzElement.getSimpleName().toString());
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(apiClazzName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(clazzElement.asType());
        // 生成对应的成员变量以及构造方法：分成单url环境和多url环境处理
        String tagName = "TAG";
        FieldSpec.Builder tagStaticField = FieldSpec.builder(
                String.class, tagName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
        ).initializer("$L.class.getCanonicalName()", apiClazzName);
        typeSpecBuilder.addField(tagStaticField.build());
        typeSpecBuilder.addField(UrlEnvConfig.class, curUrlEnvFieldName, Modifier.PRIVATE);
        if (multipleUrlEnv) {
            // 创建List<UrlEnvConfig>成员变量
            FieldSpec urlEnvListSpec = generateUrlEnvListField();
            typeSpecBuilder.addField(urlEnvListSpec);
            // 创建构造方法
            MethodSpec multipleConstructor = generateMultipleConstructor(clazzElement, httpConfigsName, httpsInfos);
            typeSpecBuilder.addMethod(multipleConstructor);
            // 实现多域名抽象接口的方法MultipleUrlEnvConfigs.addUrlEnvMethodName
            MethodSpec addUrlEnvMethod = generateAddUrlEnvMethod();
            typeSpecBuilder.addMethod(addUrlEnvMethod);
            // 实现自定义方法getUrlEnvByEnvName
            MethodSpec getUrlEnvByEnvNameMethod = generateGetUrlEnvByEnvNameMethod();
            typeSpecBuilder.addMethod(getUrlEnvByEnvNameMethod);
            // 实现多域名抽象接口的方法MultipleUrlEnvConfigs.switchUrlEnvMethodName
            MethodSpec switchUrlEnvMethod = generateSwitchUrlEnvMethod();
            typeSpecBuilder.addMethod(switchUrlEnvMethod);
        } else {
            // 单域url环境造方法
            typeSpecBuilder.addMethod(generateSingleUrlEnvConstructor(tagName, baseUrl, clazzElement, httpConfigsName, httpsInfos));
        }
        // 获取接口类中定义的所有子元素
        List<? extends Element> childElements = clazzElement.getEnclosedElements();
        if (!EmptyUtils.collectionNull(childElements)) {
            // 生成实现类的内容
            // 解析所有方法，并生成JavaPoet方法对象
            List<MethodSpec> methodSpecList = generateMethodSpec(childElements, urlEncode, multipleUrlEnv);
            if (!EmptyUtils.collectionNull(methodSpecList)) {
                for (MethodSpec methodSpec : methodSpecList) {
                    typeSpecBuilder.addMethod(methodSpec);
                }
            }
        }
        // 创建文件
        JavaFile javaFile = JavaFile
                .builder(GenerateRules.generateApiClazzNamePkg(), typeSpecBuilder.build())
                .build();
        try {
            // 输出文件
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec generateSwitchUrlEnvMethod() {
        String envNameParam = "envName";
        String urlEnvParamField = "urlEnvConfig";
        return MethodSpec.methodBuilder(MultipleUrlEnvConfigs.SWITCH_URL_ENV_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .addException(Exception.class)
                .addParameter(ParameterSpec.builder(String.class, envNameParam).build())
                .addStatement(
                        "$T $L = $L($L)",
                        UrlEnvConfig.class,
                        urlEnvParamField,
                        getUrlEnvByEnvNameMethod,
                        envNameParam
                )
                .addCode(
                        CodeBlock.builder()
                                .beginControlFlow(
                                        "if ($L == null)",
                                        urlEnvParamField
                                )
                                .addStatement(
                                        "throw new $T(\"没有找到环境名称$L对应的url地址\")",
                                        Exception.class,
                                        envNameParam
                                )
                                .endControlFlow()
                                .build()
                )
                .addStatement(
                        "$L = $L",
                        curUrlEnvFieldName,
                        urlEnvParamField
                )
                .build();
    }

    private MethodSpec generateGetUrlEnvByEnvNameMethod() {
        String envNameParam = "envName";
        String domainParam = "urlEnvConfig";
        return MethodSpec.methodBuilder(getUrlEnvByEnvNameMethod)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterSpec.builder(String.class, envNameParam).build())
                .returns(UrlEnvConfig.class)
                .addCode(
                        CodeBlock.builder()
                                .beginControlFlow(
                                        "if ($T.stringNull($L))",
                                        EmptyUtils.class,
                                        envNameParam
                                )
                                .addStatement(
                                        "throw new $T(\"环境名称不能为空\")",
                                        RuntimeException.class
                                )
                                .endControlFlow()
                                .build()
                )
                .addCode(
                        CodeBlock.builder()
                                .beginControlFlow(
                                        "for ($T $L : $L)",
                                        UrlEnvConfig.class,
                                        domainParam,
                                        urlEnvListFieldName
                                )
                                .add(
                                        CodeBlock.builder()
                                                .beginControlFlow(
                                                        "if ($L.getEnvName().equals($L))",
                                                        domainParam,
                                                        envNameParam
                                                )
                                                .addStatement(
                                                        "return $L",
                                                        domainParam
                                                )
                                                .endControlFlow()
                                                .build()
                                )
                                .endControlFlow()
                                .build()
                )
                .addStatement("return null")
                .build();
    }

    private MethodSpec generateAddUrlEnvMethod() {
        String urlEnvParam = "urlEnvConfig";
        return MethodSpec.methodBuilder(MultipleUrlEnvConfigs.ADD_URL_ENV_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(UrlEnvConfig.class, urlEnvParam).build())
                .addCode(
                        CodeBlock.builder()
                                .beginControlFlow(
                                        "if ($L != null)",
                                        urlEnvParam
                                )
                                .addStatement(
                                        "$L.add($L)",
                                        urlEnvListFieldName,
                                        urlEnvParam
                                )
                                .endControlFlow()
                                .build()
                )
                .build();
    }

    private MethodSpec generateMultipleConstructor(TypeElement apiClazz, String httpConfigsName, HttpsInfos httpsInfos) {
        // 构造方法内容：（1）给domainList赋值（2）判断赋值后是否有值，没有值则抛出异常，编译错误
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        if (httpsInfos != null) {
            builder.addStatement(
                "$T.get().bindHttpsInfos($T.class.getCanonicalName(), $S, $S, $S)",
                    _Provider,
                    apiClazz.asType(),
                    httpsInfos.getAssetsSslCer(),
                    httpsInfos.getAssetsBks(),
                    httpsInfos.getBksPassword()
            );
        }
        if (!EmptyUtils.stringNull(httpConfigsName)) {
            builder.addStatement(
                    "$T.get().bindHttpConfigs($T.class.getCanonicalName(), $T.class)",
                    _Provider,
                    apiClazz.asType(),
                    DepsClazzPath.toClassName(elementUtils, httpConfigsName)
            );
        }
        return builder.addStatement(
                        "$L = $L()",
                        urlEnvListFieldName,
                        MultipleUrlEnvConfigs.INIT_URL_ENV_METHOD
                )
                .addCode(
                        // domainList为null跑出异常
                        CodeBlock.builder()
                                .beginControlFlow(
                                        "if ($T.collectionNull($L))",
                                        EmptyUtils.class,
                                        urlEnvListFieldName
                                )
                                .addStatement(
                                        "throw new $T(\"实现了$T接口必须初始化$T<$T> $L()方法，且至少返回一个合法Url环境配置值\")",
                                        RuntimeException.class,
                                        MultipleUrlEnvConfigs.class,
                                        List.class,
                                        UrlEnvConfig.class,
                                        MultipleUrlEnvConfigs.INIT_URL_ENV_METHOD
                                )
                                .endControlFlow()
                                .build()
                )
                .addComment("默认使用第一个值作为默认url环境")
                .addStatement(
                        "$L = $L.get(0)",
                        curUrlEnvFieldName,
                        urlEnvListFieldName
                ).build();
    }

    private FieldSpec generateUrlEnvListField() {
        ClassName urlEnvConfigClazz = ClassName.get(UrlEnvConfig.class);
        ClassName list = ClassName.get(List.class);
        TypeName urlEnvListClazz = ParameterizedTypeName.get(list, urlEnvConfigClazz);
        return FieldSpec.builder(
                urlEnvListClazz,
                urlEnvListFieldName,
                Modifier.PRIVATE
        ).build();
    }

    private MethodSpec generateSingleUrlEnvConstructor(String staticTagName, String baseUrl,
                                                       TypeElement apiClazz, String httpConfigsName,
                                                       HttpsInfos httpsInfos) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addComment("单url环境");
        if (httpsInfos != null) {
            builder.addStatement(
                    "$T.get().bindHttpsInfos($T.class.getCanonicalName(), $S, $S, $S)",
                    _Provider,
                    apiClazz.asType(),
                    httpsInfos.getAssetsSslCer(),
                    httpsInfos.getAssetsBks(),
                    httpsInfos.getBksPassword()
            );
        }
        if (!EmptyUtils.stringNull(httpConfigsName)) {
            builder.addStatement(
                    "$T.get().bindHttpConfigs($T.class.getCanonicalName(), $T.class)",
                    _Provider,
                    apiClazz.asType(),
                    DepsClazzPath.toClassName(elementUtils, httpConfigsName)
            );
        }
        return builder.addStatement(
                        "$L = new $T($L, $S)",
                        curUrlEnvFieldName,
                        UrlEnvConfig.class,
                        staticTagName,
                        baseUrl
                ).build();
    }

    private List<MethodSpec> generateMethodSpec(List<? extends Element> childElements,
                                                boolean urlEncode, boolean multipleUrlEnv) {
        // 获取子元素中的所有方法
        List<ExecutableElement> methodElements = ElementFilter.methodsIn(childElements);
        if (EmptyUtils.collectionNull(methodElements)) {
            return null;
        }
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for (ExecutableElement methodElement : methodElements) {
            // 开始解析每一个方法中的数据，5大步骤：
            //（1）解析方法的注解；
            //（2）解析方法返回值；
            //（3）解析方法名称；
            //（4）解析方法参数；
            //（5）解析方法异常；
            //（6）需要处理方法注解的url属性参数，如果包含有path参数，则方法的参数必须有对应值；

            AnnotationInfos methodAttInfos = parseAnnotations(methodElement, true);
            TypeMirror returnType = methodElement.getReturnType();
            String methodName = methodElement.getSimpleName().toString();
            List<MethodParamInfo> methodParamInfos = parseMethodParams(methodElement);
            if (methodAttInfos == null) {
                // 如果没有方法注解，并且不是多url环境中的方法，则不合法
                if (multipleUrlEnv) {
                    if (isMultipleUrlEnvMethod(methodName, methodParamInfos)) {
                        // 没有注解的方法如果是多url环境中的方法，则忽略
                        continue;
                    }
                }
                throwAptException("方法" + methodName + "必须使用框架内部的注解进行标注，否则无法解析");
            }
            // 获取方法注解中的url值，也就是restUrl值字符串，并解析其中的path参数
            RestUrlParser restUrlParser = getMethodAnnotationUrlPathParams(methodAttInfos, methodParamInfos);
            List<TypeName> exceptions = parseMethodExceptions(methodElement);

            // 在解析所有方法信息的时候，如果不合法，编译不会通过，代码走到这里，代表拿到的所有方法信息数据都可以直接使用
            // 接下来使用JavaPoet拼接方法对象
            MethodSpec methodSpec = makeupMethodSpec(
                    methodAttInfos,
                    returnType,
                    methodName,
                    methodParamInfos,
                    exceptions,
                    restUrlParser,
                    urlEncode
            );
            methodSpecList.add(methodSpec);
        }
        return methodSpecList;
    }

    /**
     * 该方法是否是多域名接口中的方法：只要名字相同即可
     */
    private boolean isMultipleUrlEnvMethod(String methodName, List<MethodParamInfo> methodParamInfos) {
        boolean infosEmpty = EmptyUtils.collectionNull(methodParamInfos);
        // 如果是多域名，MultipleDomainSupport.class方法直接忽略，进入下一次循环
        String[] methodArray = MultipleUrlEnvConfigs.METHOD_NAME_ARRAY;
        Object[] paramsArray = MultipleUrlEnvConfigs.METHOD_PARAMS_ARRAY;
        for (int i = 0; i < methodArray.length; i++) {
            String multipleMethodName = methodArray[i];
            Object[] params = (Object[]) paramsArray[i];
            if (!methodName.equals(multipleMethodName)) {
                continue;
            }
            if (params == null) {
                if (infosEmpty) {
                    return true;
                }
            } else {
                // 参数长度是否相等，不等则不是同一个方法
                if (params.length != methodParamInfos.size()) {
                    continue;
                }
                // 参数长度相等，对比参数类型，必须按照顺序
                // 默认相同方法
                boolean sameMethod = true;
                for (int j = 0; j < params.length; j++) {
                    Class pClazz = (Class) params[j];
                    MethodParamInfo paramInfo = methodParamInfos.get(j);
                    String methodParamName = paramInfo.getParamTypeMirror().toString();
                    if (!pClazz.getCanonicalName().equals(methodParamName)) {
                        sameMethod = false;
                    }
                }
                if (sameMethod) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取方法注解中url属性参数中的path参数（使用{}包裹的参数）集合
     *
     * @param annotationInfos
     * @return
     */
    private RestUrlParser getMethodAnnotationUrlPathParams(AnnotationInfos annotationInfos,
                                                           List<MethodParamInfo> methodParamInfos) {
        if (annotationInfos == null) {
            return null;
        }
        List<AnnotationMember> members = annotationInfos.getMembers();
        AnnotationMember urlMember = AnnotationUtils.getMemberByKeyName(members, DepsClazzPath.AN_A_REST_URL);
        if (urlMember == null) {
            return null;
        }
        AnnotationValue urlAttValue = urlMember.getValue();
        String restUrl = urlAttValue == null ? null : urlAttValue.getValue().toString();
        if (EmptyUtils.stringNull(restUrl)) {
            return null;
        }
        // 直接先简单判断restUrl中是否含有{}的符号，如果没有则代表没有Path参数，就不用了接下来的解析，否则需要解析
        if (!restUrl.contains("{") || !restUrl.contains("}")) {
            return null;
        }
        // 符合基本判断，可以解析，最终根据解析结果为准
        List<MethodParamInfo> pathParamInfos = ParamsUtils.getPathParamsFromAll(methodParamInfos);
        // 解析resetUrl中的参数
        RestUrlParser restUrlParser = RestUrlParser.parseRestUrl(restUrl);
        // 进行对比：
        // （1）restUrlParser为空则不必继续对比，直接返回null
        // （2）restUrlParser中参数集合为空，不必对比，返回对象
        if (restUrlParser == null) {
            return null;
        }
        List<String> pathArgs = restUrlParser.getPathArgs();
        if (EmptyUtils.collectionNull(pathArgs)) {
            return restUrlParser;
        }
        if (EmptyUtils.collectionNull(pathParamInfos)) {
            throwAptException(annotationInfos.getMirror().toString() + "缺少注解@Path参数，无法替换属性"
                    + DepsClazzPath.AN_A_REST_URL + "中的变量");
        }
        // 有参数，对比
        List<String> notMatchArgs = new ArrayList<>();
        for (String arg : pathArgs) {
            // 是否匹配上
            boolean match = false;
            for (MethodParamInfo paramInfo : pathParamInfos) {
                AnnotationInfos attInfos = paramInfo.getAttInfos();
                List<AnnotationMember> pathParamMembers = attInfos.getMembers();
                AnnotationMember keyMember = AnnotationUtils.getMemberByKeyName(pathParamMembers, DepsClazzPath.AN_A_PATH_KEY);
                if (keyMember != null) {
                    AnnotationValue attValue = keyMember.getValue();
                    if (attValue != null && attValue.getValue() != null) {
                        String valueStr = attValue.getValue().toString();
                        if (!EmptyUtils.stringNull(valueStr) && arg.equals(valueStr)) {
                            // 匹配成功
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (!match) {
                notMatchArgs.add(arg);
            }
        }
        if (notMatchArgs.size() > 0) {
            StringBuilder errorBuilder = new StringBuilder(restUrl + "中参数");
            for (String arg : notMatchArgs) {
                errorBuilder.append(arg).append("，");
            }
            errorBuilder.append("没有找到对应的注解@Path的属性" + DepsClazzPath.AN_A_PATH_KEY + "值，请检查");
            throwAptException(errorBuilder.toString());
        }
        return restUrlParser;
    }

    private MethodSpec makeupMethodSpec(AnnotationInfos methodAttInfos,
                                        TypeMirror returnType,
                                        String methodName,
                                        List<MethodParamInfo> methodParamInfos,
                                        List<TypeName> exceptions,
                                        RestUrlParser restUrlParser,
                                        boolean urlEncode) {
        // 创建方法builder对象
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(returnType));
        // 添加exceptions
        if (!EmptyUtils.collectionNull(exceptions)) {
            builder.addExceptions(exceptions);
        }
        // 创建注解对象
        AnnotationSpec annotationSpec = makeupAnnotationSpec(methodAttInfos);
        builder.addAnnotation(annotationSpec);
        // 创建参数对象
        List<ParameterSpec> parameterSpecList = makeupParameterSpecList(methodParamInfos);

        if (!EmptyUtils.collectionNull(parameterSpecList)) {
            // 添加方法参数
            builder.addParameters(parameterSpecList);
        }
        // 添加方法体内容
        CodeBlock codeBlock = makeupMethodContent(returnType, methodAttInfos, methodParamInfos, restUrlParser, urlEncode);
        builder.addCode(codeBlock);
        return builder.build();
    }

    private CodeBlock makeupMethodContent(TypeMirror returnType, AnnotationInfos methodAttInfos,
                                          List<MethodParamInfo> methodParamInfos,
                                          RestUrlParser restUrlParser, boolean urlEncode) {
        CodeBlock.Builder contentBuilder = CodeBlock.builder();
        // 生成url代码
        generateUrlCodeBlock(contentBuilder, methodParamInfos, restUrlParser);
        // 获取对应参数类型的集合
        List<MethodParamInfo> bodyParamInfos = ParamsUtils.getBodyParamsFromAll(methodParamInfos);
        List<MethodParamInfo> queryParamInfos = ParamsUtils.getQueryParamsFromAll(methodParamInfos);
        List<MethodParamInfo> headerParamInfos = ParamsUtils.getHeaderParamsFromAll(methodParamInfos);
        List<MethodParamInfo> downloadParamInfos = ParamsUtils.getDownloadParamsFromAll(methodParamInfos);
        List<MethodParamInfo> uploadParamInfos = ParamsUtils.getUploadParamsFromAll(methodParamInfos);
        // 获取泛型字符串
        String genericTypeStr = getMethodGenericTypeStr(returnType);
        // 判断方法，根据方法注解，生成后面的内容：（1）Api访问注解；（2）上传注解；（3）下载注解；
        SupportAnnotation supportAtt = methodAttInfos.getSupportAtt();
        if (supportAtt == SupportAnnotation.M_DOWNLOAD) {
            // 只能有一个或者没有@DownloadPath注解的DownloadPathParams，不能定义@UploadFile注解，不能有body参数
            if (!EmptyUtils.collectionNull(uploadParamInfos)) {
                throwAptException("@Download标记的下载文件方法，定义了注解@UploadFile标记的无效的参数");
            }
            boolean downloadParamEmpty = EmptyUtils.collectionNull(downloadParamInfos);
            if (!downloadParamEmpty && downloadParamInfos.size() > 1) {
                throwAptException("@Download标记的下载文件方法，最多只能使用一个@DownloadPath标记的参数(可以不定义)");
            }
            if (!EmptyUtils.collectionNull(bodyParamInfos)) {
                throwAptException("@Download标记的下载文件方法不能添加请求体参数");
            }
            MethodParamInfo downloadParams = downloadParamEmpty ? null : downloadParamInfos.get(0);
            generateDownloadMethod(contentBuilder, urlEncode, downloadParams, queryParamInfos, headerParamInfos);
        } else if (supportAtt == SupportAnnotation.M_UPLOAD) {
            // 有且必须有一个@UploadFile注解的UploadFileParams，不能有@DownloadPath标记的参数
            if (!EmptyUtils.collectionNull(downloadParamInfos)) {
                throwAptException("@Upload标记的上传文件方法，定义了注解@DownloadPath标记的无效参数");
            }
            if (EmptyUtils.collectionNull(uploadParamInfos) || uploadParamInfos.size() > 1) {
                throwAptException("@Upload标记的上传文件方法，必须有，且只能有一个@UploadFile标记的参数，且参数不能为空");
            }
            MethodParamInfo uploadParams = uploadParamInfos.get(0);
            generateUploadMethod(contentBuilder, genericTypeStr, urlEncode, uploadParams, bodyParamInfos, queryParamInfos, headerParamInfos);
        } else {
            generateHttpApiMethod(contentBuilder, supportAtt, genericTypeStr, urlEncode, bodyParamInfos, queryParamInfos, headerParamInfos, methodAttInfos);
        }
        return contentBuilder.build();
    }

    private void generateDownloadMethod(CodeBlock.Builder contentBuilder,
                                        boolean urlEncode,
                                        MethodParamInfo downloadParams,
                                        List<MethodParamInfo> queryParamInfos,
                                        List<MethodParamInfo> headerParamInfos) {
        StringBuilder downloadBuilder =
                new StringBuilder("return $T.download($L)");
        downloadBuilder.append("\n.httpProtocol($T.get().apiHttpProtocol(this))");
        downloadBuilder.append("\n.urlEncode($L)");
        List<Object> downloadObjs = new ArrayList<>();
        downloadObjs.add(_Ufo);
        downloadObjs.add(urlLocalParam);
        downloadObjs.add(_Provider);
        downloadObjs.add(urlEncode);
        if (downloadParams != null) {
            String paramName = downloadParams.getParamName();
            downloadBuilder.append("\n.fileDir($L == null ? null : $L.getFileDir())");
            downloadBuilder.append("\n.fileName($L == null ? null : $L.getFileName())");
            downloadObjs.add(paramName);
            downloadObjs.add(paramName);
            downloadObjs.add(paramName);
            downloadObjs.add(paramName);
        }
        // 添加@Query参数
        appendQueries(downloadBuilder, downloadObjs, queryParamInfos);
        // 添加@Header参数
        appendHeaders(downloadBuilder, downloadObjs, headerParamInfos);
        //
        downloadBuilder.append("\n.newCall()");
        contentBuilder.addStatement(downloadBuilder.toString(), downloadObjs.toArray());
    }

    private void generateUploadMethod(CodeBlock.Builder contentBuilder,
                                      String genericTypeStr,
                                      boolean urlEncode,
                                      MethodParamInfo uploadParams,
                                      List<MethodParamInfo> bodyParamInfos,
                                      List<MethodParamInfo> queryParamInfos,
                                      List<MethodParamInfo> headerParamInfos) {
        StringBuilder uploadBuilder =
                new StringBuilder("return $T.$Lupload($L)");
        uploadBuilder.append("\n.httpProtocol($T.get().apiHttpProtocol(this))");
        uploadBuilder.append("\n.urlEncode($L)");
        uploadBuilder.append("\n.files($L == null ? null : $L.toFiles())");
        List<Object> uploadObjs = new ArrayList<>();
        uploadObjs.add(_Ufo);
        uploadObjs.add(genericTypeStr);
        uploadObjs.add(urlLocalParam);
        uploadObjs.add(_Provider);
        uploadObjs.add(urlEncode);
        uploadObjs.add(uploadParams.getParamName());
        uploadObjs.add(uploadParams.getParamName());
        // 添加@Body参数
        appendBodies(uploadBuilder, uploadObjs, bodyParamInfos, null);
        // 添加@Query参数
        appendQueries(uploadBuilder, uploadObjs, queryParamInfos);
        // 添加@Header参数
        appendHeaders(uploadBuilder, uploadObjs, headerParamInfos);
        // 回调Call
        uploadBuilder.append("\n.newCall(new $T$L(new $T$L(){}))");
        uploadObjs.add(_GenericResultType);
        uploadObjs.add(genericTypeStr);
        uploadObjs.add(_TypeToken);
        uploadObjs.add(genericTypeStr);
        contentBuilder.addStatement(uploadBuilder.toString(), uploadObjs.toArray());
    }

    private void generateHttpApiMethod(CodeBlock.Builder contentBuilder,
                                       SupportAnnotation supportAtt,
                                       String genericTypeStr,
                                       boolean urlEncode,
                                       List<MethodParamInfo> bodyParamInfos,
                                       List<MethodParamInfo> queryParamInfos,
                                       List<MethodParamInfo> headerParamInfos,
                                       AnnotationInfos methodAttInfos) {

        // 获取当前上传数据的格式，默认form格式
        String bodyFormatName = BodyFormat.FORM.name();
        boolean isGetMethod = methodAttInfos.getSupportAtt() == SupportAnnotation.M_GET;
        if (!isGetMethod) {
            // Get方法没有BodyForm
            List<AnnotationMember> methodAttMembers = methodAttInfos.getMembers();
            AnnotationMember formMember = AnnotationUtils.getMemberByKeyName(methodAttMembers, DepsClazzPath.AN_A_FORMAT);
            if (formMember != null) {
                bodyFormatName = formMember.getValue().getValue().toString();
                printMsg("bodyFormatName = " + bodyFormatName);
            }
        } else {
            // get方法不能添加请求体
            if (!EmptyUtils.collectionNull(bodyParamInfos)) {
                throwAptException("Get方法方法不能添加请求体参数");
            }
        }

        StringBuilder requestStrBuilder = new StringBuilder("return $T.$L$L($L)");
        List<Object> requestObjs = new ArrayList<>();
        requestObjs.add(_Ufo);
        requestObjs.add(genericTypeStr);
        requestObjs.add(supportAtt.getHttpMethod().toLowerCase());
        requestObjs.add(urlLocalParam);
        BodyFormat bodyFormat = null;
        if (!isGetMethod) {
            if (bodyFormatName.equals(BodyFormat.JSON.name())) {
                bodyFormat = BodyFormat.JSON;
                requestStrBuilder.append("\n.json()");
            } else if (bodyFormatName.equals(BodyFormat.TEXT.name())) {
                requestStrBuilder.append("\n.string()");
                bodyFormat = BodyFormat.TEXT;
            } else {
                requestStrBuilder.append("\n.form()");
                bodyFormat = BodyFormat.FORM;
            }
        }
        requestStrBuilder.append("\n.httpProtocol($T.get().apiHttpProtocol(this))");
        requestStrBuilder.append("\n.urlEncode($L)");
        requestObjs.add(_Provider);
        requestObjs.add(urlEncode);

        // 添加参数
        if (!isGetMethod) {
            appendBodies(requestStrBuilder, requestObjs, bodyParamInfos, bodyFormat);
        }
        // 添加@Query参数
        appendQueries(requestStrBuilder, requestObjs, queryParamInfos);
        // 添加@Header参数
        appendHeaders(requestStrBuilder, requestObjs, headerParamInfos);
        // 获取回调
        requestStrBuilder.append("\n.newCall(new $T$L(new $T$L(){}))");
        requestObjs.add(_GenericResultType);
        requestObjs.add(genericTypeStr);
        requestObjs.add(_TypeToken);
        requestObjs.add(genericTypeStr);
        // 添加
        contentBuilder.addStatement(requestStrBuilder.toString(), requestObjs.toArray());
    }

    private void appendBodies(StringBuilder requestStrBuilder, List<Object> requestObjs,
                              List<MethodParamInfo> bodyParamInfos, BodyFormat bodyFormat) {
        if (!EmptyUtils.collectionNull(bodyParamInfos)) {
            // 只有一个直接添加，有多个则先合并再添加
            if (bodyParamInfos.size() == 1) {
                if (bodyFormat == null || bodyFormat == BodyFormat.FORM) {
                    requestStrBuilder.append("\n.body($L == null ? null : $L.toParams())");
                    String paramName = bodyParamInfos.get(0).getParamName();
                    requestObjs.add(paramName);
                    requestObjs.add(paramName);
                } else if (bodyFormat == BodyFormat.JSON) {
                    requestStrBuilder.append("\n.jsonBody($L == null ? null : $T.toJson($L.toParams()))");
                    String paramName = bodyParamInfos.get(0).getParamName();
                    requestObjs.add(paramName);
                    requestObjs.add(GSonUtils.class);
                    requestObjs.add(paramName);
                } else {
                    requestStrBuilder.append("\n.stringBody($L == null ? null : $L.stringBody())");
                    String paramName = bodyParamInfos.get(0).getParamName();
                    requestObjs.add(paramName);
                    requestObjs.add(paramName);
                }

            } else {
                if (bodyFormat == null || bodyFormat == BodyFormat.FORM) {
                    requestStrBuilder.append("\n.body(");
                    requestStrBuilder.append("$T.merge(");
                    for (MethodParamInfo paramInfo : bodyParamInfos) {
                        requestStrBuilder.append(paramInfo.getParamName()).append(",");
                    }
                    requestStrBuilder.deleteCharAt(requestStrBuilder.length() - 1).append("))");
                    requestObjs.add(_MergeParamsUtils);
                } else if (bodyFormat == BodyFormat.JSON) {
                    requestStrBuilder.append("\n.jsonBody(");
                    requestStrBuilder.append("$T.toJson($T.merge(");
                    requestObjs.add(GSonUtils.class);
                    for (MethodParamInfo paramInfo : bodyParamInfos) {
                        requestStrBuilder.append(paramInfo.getParamName()).append(",");
                    }
                    requestStrBuilder.deleteCharAt(requestStrBuilder.length() - 1).append(")))");
                    requestObjs.add(_MergeParamsUtils);
                } else {
                    // BodyFormat.TEXT类型的BodyParams参数只能够有一个
                    throwAptException("BodyFormat.TEXT类型的请求体只能够配置一个@Body参数，且添加请求体需要调用stringBody()方法！");
                }
            }
        }
    }

    private void appendHeaders(StringBuilder requestStrBuilder, List<Object> requestObjs, List<MethodParamInfo> headerParamInfos) {
        if (!EmptyUtils.collectionNull(headerParamInfos)) {
            if (headerParamInfos.size() == 1) {
                requestStrBuilder.append("\n.header($L == null ? null : $L.toHeaders())");
                String paramName = headerParamInfos.get(0).getParamName();
                requestObjs.add(paramName);
                requestObjs.add(paramName);
            } else {
                requestStrBuilder.append("\n.header(");
                requestStrBuilder.append("$T.merge(");
                for (MethodParamInfo paramInfo : headerParamInfos) {
                    requestStrBuilder.append(paramInfo.getParamName()).append(",");
                }
                requestStrBuilder.deleteCharAt(requestStrBuilder.length() - 1).append("))");
                requestObjs.add(_MergeParamsUtils);
            }
        }
    }

    private void appendQueries(StringBuilder requestStrBuilder, List<Object> requestObjs, List<MethodParamInfo> queryParamInfos) {
        // 添加@Query参数
        if (!EmptyUtils.collectionNull(queryParamInfos)) {
            if (queryParamInfos.size() == 1) {
                requestStrBuilder.append("\n.query($L == null ? null : $L.toParams())");
                String paramName = queryParamInfos.get(0).getParamName();
                requestObjs.add(paramName);
                requestObjs.add(paramName);
            } else {
                requestStrBuilder.append("\n.query(");
                requestStrBuilder.append("$T.merge(");
                for (MethodParamInfo paramInfo : queryParamInfos) {
                    requestStrBuilder.append(paramInfo.getParamName()).append(",");
                }
                requestStrBuilder.deleteCharAt(requestStrBuilder.length() - 1).append("))");
                requestObjs.add(_MergeParamsUtils);
            }
        }
    }

    private void generateUrlCodeBlock(CodeBlock.Builder contentBuilder, List<MethodParamInfo> methodParamInfos, RestUrlParser restUrlParser) {
        // 如果使用了自定义的url，则这里就不使用环境配置中的baseUrl
        List<MethodParamInfo> urlParamInfos = ParamsUtils.getUrlParamsFromAll(methodParamInfos);
        if (EmptyUtils.collectionNull(urlParamInfos)) {
            contentBuilder.addStatement(
                    "$T $L = $L.getBaseUrl()",
                    String.class,
                    urlLocalParam,
                    curUrlEnvFieldName
            );
        } else {
            // 如果不为空，则有且只有一个，前面已经判断过了，大于1个直接报错
            MethodParamInfo paramInfo = urlParamInfos.get(0);
            String paramName = paramInfo.getParamName();
            // 如果传入的url为空，或者不符合正则，则抛出异常
            contentBuilder.add(
                    CodeBlock.builder()
                            .beginControlFlow(
                                    "if ($T.stringNull($L) || !$T.isValidUrl($L))",
                                    EmptyUtils.class,
                                    paramName,
                                    RegexUtils.class,
                                    paramName
                            )
                            .addStatement(
                                    "throw new $T($L + \"is empty or is illegal url format\")",
                                    RuntimeException.class,
                                    paramName
                            )
                            .endControlFlow()
                            .build()
            );
            contentBuilder.addStatement(
                    "$T $L = $L",
                    String.class,
                    urlLocalParam,
                    paramName
            );
        }

        List<MethodParamInfo> pathParamInfos = ParamsUtils.getPathParamsFromAll(methodParamInfos);
        // 拼接url和restUrl，restUrlParser为空则代表没有restUrl，
        // 如果不为空有两种情况：（1）restUrl带有参数；（2）只有不带有参数的restUrl;
        // 因此还要根据pathParamInfos进行判断，才能确定是否有参数
        if (restUrlParser != null) {
            String restUrlParserLocalParam = "restUrlParser";
            contentBuilder.addStatement(
                    "$T $L = new $T($S, $S)",
                    RestUrlParser.class,
                    restUrlParserLocalParam,
                    RestUrlParser.class,
                    restUrlParser.getMatchUrl(),
                    restUrlParser.getArgsStr()
            );
            // 如果有path参数才拼接，否则直接添加到url后
            if (!EmptyUtils.collectionNull(pathParamInfos)) {
                // 解析pathParamInfos成集合，拼接path参数的Map
                String pathMapParam = "pathMap";
                contentBuilder.addStatement(
                        "$T<$T, $T> $L = new $T<>()",
                        Map.class,
                        String.class,
                        String.class,
                        pathMapParam,
                        HashMap.class
                );

                for (MethodParamInfo info : pathParamInfos) {
                    contentBuilder.addStatement(
                            "$L.put($S, $L)",
                            pathMapParam,
                            info.getKeyName(),
                            info.getParamName()
                    );
                }
                // 有参数
                contentBuilder.addStatement(
                        "$L = $T.contactUrlWithRestUrl($L, $L.makeRestUrl($L))",
                        urlLocalParam,
                        _UrlUtils,
                        urlLocalParam,
                        restUrlParserLocalParam,
                        pathMapParam
                );
            }
        }
    }

    private String getMethodGenericTypeStr(TypeMirror returnType) {
        // 获取返回类型的内部类型，也就是泛型集合
        List<String> innerList = ClazzUtils.getInnerGenericClazzNameList(returnType.toString(), messager);
        // 如果innerList集合长度为0，则代表泛型为Object，不处理
        String innerStr = "";
        if (innerList.size() > 0) {
            for (int i = innerList.size() - 1; i >= 0; i--) {
                String innerClazzName = innerList.get(i);
                // 从内往外拼接
                innerStr = "<" + innerClazzName + innerStr + ">";
            }
        } else {
            // 添加为Object
            innerStr = "<Object>";
        }
        return innerStr;
    }

    private List<ParameterSpec> makeupParameterSpecList(List<MethodParamInfo> methodParamInfos) {
        if (EmptyUtils.collectionNull(methodParamInfos)) {
            return null;
        }
        List<ParameterSpec> parameterSpecList = new ArrayList<>();
        for (MethodParamInfo paramInfos : methodParamInfos) {
            String paramName = paramInfos.getParamName();
            ParameterSpec.Builder builder = ParameterSpec.builder(
                    TypeName.get(paramInfos.getParamTypeMirror()), paramName);
            // 添加参数注解
            builder.addAnnotation(makeupAnnotationSpec(paramInfos.getAttInfos()));
            //
            parameterSpecList.add(builder.build());
        }
        return parameterSpecList;
    }

    private AnnotationSpec makeupAnnotationSpec(AnnotationInfos attInfos) {
        AnnotationSpec.Builder attBuilder =
                AnnotationSpec.builder(
                        ClassName.get((TypeElement) attInfos.getMirror()
                                .getAnnotationType().asElement())
                );
        List<AnnotationMember> members = attInfos.getMembers();
        if (!EmptyUtils.collectionNull(members)) {
            for (AnnotationMember member : members) {
                String key = member.getKey();
                AnnotationMember.Type type = member.getType();
                AnnotationValue value = member.getValue();
                // 这里不需要判断非空了，前面已经判断过
                if (type == AnnotationMember.Type.ENUM) {
                    attBuilder.addMember(key, "$T.$L", member.getReturnType(), value.getValue());
                } else {
                    attBuilder.addMember(key, "$L", value);
                }
            }
        }
        return attBuilder.build();
    }

    /**
     * 解析方法或者参数的注解
     *
     * @param element   解析注解的元素
     * @param methodAtt 是否是解析方法
     * @return 返回解析出来的注解对象信息
     */
    private AnnotationInfos parseAnnotations(Element element, boolean methodAtt) {
        List<? extends AnnotationMirror> attMirrors = element.getAnnotationMirrors();
        // 规则：
        //（1）接口中定义的所有方法和方法参数（除了继承自多域名接口）必须使用框架内部的注解进行标注，有且只有一个，否则不合法；
        //（2）方法或方法参数上面的注解只有框架内部的方法注解会被应用到具体实现类，其余注解全都忽略；
        if (EmptyUtils.collectionNull(attMirrors)) {
            return null;
        }
        List<AnnotationMirror> validMirrors = new ArrayList<>();
        SupportAnnotation supportAtt = null;
        for (AnnotationMirror am : attMirrors) {
            // 这里直接使用注解的全路径名称与去进行字符串对比
            String qualifiedName = am.getAnnotationType().toString();
            // SupportAnnotation的transfer方法进行匹配，如果能够得到对应的枚举，则代表属于框架内部支持的注解
            // 这里无需直接区别方法注解和参数注解，因为参数注解无法使用到方法上，会直接报错
            // 因此只要能转换成枚举，则代表正确
            SupportAnnotation supportAnnotation = SupportAnnotation.transfer(qualifiedName);
            if (supportAnnotation != null) {
                validMirrors.add(am);
                supportAtt = supportAnnotation;
            }
        }
        if (EmptyUtils.collectionNull(validMirrors)) {
            return null;
        }
        if (validMirrors.size() != 1) {
            String type = methodAtt ? "方法" : "参数";
            throwAptException("注解使用不合法，" + type + element.getSimpleName() + "上，有且只能有一个框架内部的注解标注");
        }
        AnnotationMirror mirror = validMirrors.get(0);
        // 解析注解的参数
        List<AnnotationMember> members = parseAnnotationMembers(mirror);
        // 如果满足条件，代码执行到这里，则supportAtt不会为null，因此不需要判断
        return new AnnotationInfos(mirror, members, supportAtt);
    }

    private List<AnnotationMember> parseAnnotationMembers(AnnotationMirror mirror) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
        if (EmptyUtils.mapNull(elementValues)) {
            return null;
        }
        List<AnnotationMember> memberList = new ArrayList<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();

            // 注意：在java中，注解的参数支持基本数据类和枚举类型
            // 因此，这里需要枚举类型

            // 自定义的注解成员参数枚举类型，默认基本数据类型
            AnnotationMember.Type memberType = AnnotationMember.Type.PRIMARY;
            // 根据注解中定义成员参数方法的返回值的kind进行判断
            TypeMirror returnType = key.getReturnType();
            TypeKind kind = returnType.getKind();
            if (TypeKind.DECLARED == kind) {
                // 自定义类型：包含string和枚举
                if (!ClazzUtils.isString(returnType)) {
                    memberType = AnnotationMember.Type.ENUM;
                }
            }
            memberList.add(new AnnotationMember(key.getSimpleName().toString(), value, memberType, returnType));
        }
        return memberList;
    }

    // 判断注解对应的参数类型是否合法，如果不合法则返回错误提示，合法则返回null
    private String checkIsAnnotationParamsLegal(TypeMirror paramTypeMirror, String paramsTypePath, String annotationsName) {
        boolean valid =  ClazzUtils.isChildOrSameType(
                elementUtils,
                typeUtils,
                paramTypeMirror,
                paramsTypePath,
                messager
        );
        if (valid) {
            return null;
        }
        return  "注解" + annotationsName + "标识的参数必须使用" + paramsTypePath + "类型";
    }

    private List<MethodParamInfo> parseMethodParams(ExecutableElement methodElement) {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (EmptyUtils.collectionNull(parameters)) {
            return null;
        }
        List<MethodParamInfo> paramInfos = new ArrayList<>();
        // 标识@Url参数的个数
        int urlParamsCount = 0;
        for (VariableElement element : parameters) {
            // 规则：参数有且仅有一个框架内部的参数注解修饰，如果没有修时注解，在生成实现类时，不知道该参数有何用，至于框架外部的注解，直接忽略
            AnnotationInfos attInfos = parseAnnotations(element, false);
            // 参数名称
            String paramName = element.getSimpleName().toString();
            // 如果注解信息为null，则代表参数并为使用对应的参数注解标注，不合法
            if (attInfos == null) {
                throwAptException("参数" + paramName + "必须使用框架内部的注解进行标注，否则无法解析");
            }

            // 规则：因为数据提交时候需要用到key-value，必须满足该格式，因此要求如下：
            // （1）注解@Body必须使用BodyParams对象作为参数；
            // （2）注解@Query必须使用QueryParams对象作为参数；
            // （3）注解@Header必须使用HeaderParams对象作为参数；
            // （4）注解@Path必须使用PathParams对象作为参数；
            // （5）注解@UploadFile必须使用UploadFileParams对象作为参数；
            // （6）注解@DownloadPath必须对应使用DownloadPathParams对象作为参数；
            // （7）注解@Url为自定义的baseUrl，必须使用String类型，且只能定义一个

            // 获取注解参数的参数类型和kind用于确定参数是否合法
            TypeMirror paramTypeMirror = element.asType();
            TypeKind paramKind = paramTypeMirror.getKind();
            SupportAnnotation supportAtt = attInfos.getSupportAtt();
            String errorMsg = null;
            // kv注解的key参数对应的字符串值，也就是参数的key值
            String keyValueName = null;
            switch (supportAtt) {
                case P_BODY:
                    errorMsg = checkIsAnnotationParamsLegal(
                            paramTypeMirror,
                            DepsClazzPath.MODEL_BODY_PARAMS,
                            "@Body"
                    );
                    break;
                case P_QUERY:
                    errorMsg = checkIsAnnotationParamsLegal(
                            paramTypeMirror,
                            DepsClazzPath.MODEL_QUERY_PARAMS,
                            "@Query"
                    );
                    break;
                case P_HEADER:
                    errorMsg = checkIsAnnotationParamsLegal(
                            paramTypeMirror,
                            DepsClazzPath.MODEL_HEADER_PARAMS,
                            "@Header"
                    );
                    break;
                case P_PATH:
                    if (!ClazzUtils.isString(paramTypeMirror)) {
                        errorMsg = "@Path注解标识的参数必须使用String类型";
                    } else {
                        // @Path注解参数key的值不能为空字符串
                        List<AnnotationMember> members = attInfos.getMembers();
                        AnnotationMember keyMember = AnnotationUtils.getMemberByKeyName(members, DepsClazzPath.AN_A_PATH_KEY);
                        if (keyMember != null) {
                            AnnotationValue attValue = keyMember.getValue();
                            if (attValue == null || attValue.getValue() == null
                                    || EmptyUtils.stringNull(attValue.getValue().toString())) {
                                throwAptException("参数" + paramName + "的注解"
                                        + attInfos.getSupportAtt().getPathName() + "的key属性值不能为空");
                            }
                            keyValueName = attValue.getValue().toString();
                        }
                    }
                    break;
                case P_UPLOAD_FILE:
                    errorMsg = checkIsAnnotationParamsLegal(
                            paramTypeMirror,
                            DepsClazzPath.MODEL_UPLOAD_FILE_PARAMS,
                            "@UploadFile"
                    );
                    break;
                case P_DOWNLOAD_PATH:
                    errorMsg = checkIsAnnotationParamsLegal(
                            paramTypeMirror,
                            DepsClazzPath.MODEL_DOWNLOAD_PATH_PARAMS,
                            "@DownloadPath"
                    );
                    break;
                case P_URL:
                    if (!ClazzUtils.isString(paramTypeMirror)) {
                        errorMsg = "@Url注解标识的参数必须使用String类型";
                    }
                    urlParamsCount++;
                    printMsg("urlParamsCount ===== " + urlParamsCount);
                    if (urlParamsCount > 1) {
                        errorMsg = "@Url注解标识自定义的动态BaseUrl，每个接口方法最多只能够定义一个该类型参数";
                    }
                    break;
                default:
                    errorMsg = "参数注解不合法，使用了不支持的参数注解：" + supportAtt.getPathName();
            }
            // 如果errorMsg不为null，则报错
            if (!EmptyUtils.stringNull(errorMsg)) {
                throwAptException(errorMsg);
            }
            // 到这里，参数已经满足条件
            MethodParamInfo methodParamInfo =
                    new MethodParamInfo()
                            .setAttInfos(attInfos)
                            .setParamTypeMirror(paramTypeMirror)
                            .setKeyName(keyValueName)
                            .setParamName(paramName);
            paramInfos.add(methodParamInfo);
        }
        return paramInfos;
    }

    private List<TypeName> parseMethodExceptions(ExecutableElement methodElement) {
        List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
        if (EmptyUtils.collectionNull(thrownTypes)) {
            return null;
        }
        List<TypeName> exceptions = new ArrayList<>();
        for (TypeMirror thrownType : thrownTypes) {
            exceptions.add(TypeName.get(thrownType));
        }
        return exceptions;
    }

    private void throwAptException(String error) {
        throw new RuntimeException(error);
    }

}
