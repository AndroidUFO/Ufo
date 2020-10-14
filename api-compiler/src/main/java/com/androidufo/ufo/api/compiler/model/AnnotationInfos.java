package com.androidufo.ufo.api.compiler.model;

import com.androidufo.ufo.api.compiler.enums.SupportAnnotation;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;

/**
 * 封装注解所有信息的对象
 */
public class AnnotationInfos {
    private final AnnotationMirror mirror;
    private final List<AnnotationMember> members;
    private final SupportAnnotation supportAtt;

    public AnnotationInfos(AnnotationMirror mirror, List<AnnotationMember> members, SupportAnnotation supportAtt) {
        this.mirror = mirror;
        this.members = members;
        this.supportAtt = supportAtt;
    }

    public AnnotationMirror getMirror() {
        return mirror;
    }

    public List<AnnotationMember> getMembers() {
        return members;
    }

    public SupportAnnotation getSupportAtt() {
        return supportAtt;
    }
}
