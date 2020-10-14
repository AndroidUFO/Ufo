package com.androidufo.ufo.api.compiler.processsor;

import com.androidufo.ufo.api.annos.Api;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.api.compiler.consts.DepsClazzPath;
import com.androidufo.ufo.api.utils.GenerateRules;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.FieldSignature;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        DepsClazzPath.AN_AUTOWIRED
})
public class AutowiredProcessor extends AbsProcessor {

    private ClassName _ApiProvider;

    @Override
    public synchronized void init(ProcessingEnvironment pEnv) {
        super.init(pEnv);
        _ApiProvider = DepsClazzPath.toClassName(elementUtils, DepsClazzPath.CLAZZ_API_PROVIDER);
    }

    @Override
    public void handleAnnotations(RoundEnvironment roundEnvironment) {
        Set<? extends Element> autowiredElements = roundEnvironment.getElementsAnnotatedWith(Autowired.class);
        if (!autowiredElements.isEmpty()) {
            for (Element element : autowiredElements) {
                // 从Autowired注解的类型上获取Api注解，如果没有，则无效，不能够使用该框架注入其他类，防止其他问题产生
                TypeMirror typeMirror = element.asType();
                TypeElement clazzElement = (TypeElement) typeUtils.asElement(typeMirror);
                Api api = clazzElement.getAnnotation(Api.class);
                if (api == null) {
                    printError("使用@Autowired注解标注的字段类，必须使用@Api注解标记");
                }
                // 字段当前命名变量名称
                String fieldParamName = element.getSimpleName().toString();
                // 字段类完全路径名称
                String fieldClazzName = clazzElement.getQualifiedName().toString();
                // 字段变量所在的类的完全路径名称
                Element fieldHost = element.getEnclosingElement();
                String fieldHostFullName = fieldHost.asType().toString();
                String generateClassName = GenerateRules.generateAutowiredClazzName(
                        fieldHostFullName, fieldParamName
                );
                //
                String fieldClazzConstName = "FIELD_CLASS_NAME";
                FieldSpec.Builder fieldClazzConstBuilder = FieldSpec.builder(
                        TypeName.get(String.class),
                        fieldClazzConstName,
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                ).initializer("$S", fieldClazzName);
                ;
                String fieldParamConstName = "FIELD_PARAM_NAME";
                FieldSpec.Builder fieldParamConstBuilder = FieldSpec.builder(
                        TypeName.get(String.class),
                        fieldParamConstName,
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                ).initializer("$S", fieldHostFullName + "." + fieldParamName);

                String pointcutName = "POINTCUT";
                FieldSpec.Builder pointcutConstBuilder = FieldSpec.builder(
                        TypeName.get(String.class),
                        pointcutName,
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                ).initializer("\"get(\" + $L + \" \" + $L + \")\"", fieldClazzConstName, fieldParamConstName);
                // pointcut方法
                String pointcutMethodName = "lazyAssignment";
                MethodSpec.Builder pointcutMethodBuilder =
                        MethodSpec.methodBuilder(pointcutMethodName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(
                                        AnnotationSpec.builder(
                                                Pointcut.class
                                        ).addMember("value", "$L", pointcutName).build()
                                );
                // weave方法
                String joinPointParam = "joinPoint";
                MethodSpec.Builder weaveMethodBuilder =
                        MethodSpec.methodBuilder("weave")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.get(Object.class))
                                .addParameter(
                                        ParameterSpec.builder(
                                                ProceedingJoinPoint.class,
                                                joinPointParam
                                        ).build()
                                ).addException(TypeName.get(Throwable.class));

                String fieldValue = "fieldValue";
                String fieldHostObjParam = "fieldHostObj";
                String fieldSignatureParam = "signature";
                String fieldParam = "field";
                String instanceParam = "instance";
                weaveMethodBuilder
                        .addAnnotation(
                                AnnotationSpec.builder(Around.class)
                                        .addMember("value", "\"$L()\"", pointcutMethodName)
                                        .build()
                        )
                        .addStatement(
                                "$T $L = $L.proceed()",
                                Object.class,
                                fieldValue,
                                joinPointParam
                        )
                        .addCode(
                                CodeBlock.builder().beginControlFlow("if ($L != null)", fieldValue)
                                        .addStatement("return $L", fieldValue)
                                        .endControlFlow()
                                        .build()
                        )
                        .addStatement(
                                "$T $L = $L.getTarget()",
                                Object.class,
                                fieldHostObjParam,
                                joinPointParam
                        )
                        .addStatement(
                                "$T $L = ($T) $L.getSignature()",
                                FieldSignature.class,
                                fieldSignatureParam,
                                FieldSignature.class,
                                joinPointParam

                        )
                        .addStatement(
                                "$T $L = $L.getField()",
                                Field.class,
                                fieldParam,
                                fieldSignatureParam
                        )
                        .addStatement(
                                "$T $L = $T.get().apiInstance($T.class)",
                                Object.class,
                                instanceParam,
                                _ApiProvider,
                                typeMirror
                        )
                        .addStatement(
                                "$L.set($L, $L)",
                                fieldParam,
                                fieldHostObjParam,
                                instanceParam
                        )
                        .addStatement(
                                "return $L",
                                instanceParam
                        );
                // 构建类
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(generateClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(Aspect.class)
                        .addField(fieldClazzConstBuilder.build())
                        .addField(fieldParamConstBuilder.build())
                        .addField(pointcutConstBuilder.build())
                        .addMethod(pointcutMethodBuilder.build())
                        .addMethod(weaveMethodBuilder.build());

                JavaFile javaFile = JavaFile.builder(
                        GenerateRules.generateApiClazzNamePkg(),
                        typeBuilder.build()
                ).build();
                try {
                    // 输出文件
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
