package com.androidufo.ufo.api.compiler.model;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * 用于封装注解成员参数的对象
 */
public class AnnotationMember {
    private final String key;
    private final AnnotationValue value;
    private final Type type;
    private final TypeMirror returnType;

    public AnnotationMember(String key, AnnotationValue value, Type type, TypeMirror returnType) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.returnType = returnType;
    }

    public String getKey() {
        return key;
    }

    public AnnotationValue getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    /**
     * 这里单独定一个枚举，为了区别枚举成员参数
     */
    public enum Type {
        PRIMARY,
        ENUM
    }
}
