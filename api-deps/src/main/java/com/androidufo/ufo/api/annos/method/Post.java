package com.androidufo.ufo.api.annos.method;

import com.androidufo.ufo.api.enums.BodyFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Post {
    String restUrl() default "";
    BodyFormat format() default BodyFormat.FORM;
}
