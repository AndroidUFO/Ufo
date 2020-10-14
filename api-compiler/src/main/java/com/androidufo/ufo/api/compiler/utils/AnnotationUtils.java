package com.androidufo.ufo.api.compiler.utils;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.api.compiler.model.AnnotationMember;

import java.util.List;

public class AnnotationUtils {

    public static AnnotationMember getMemberByKeyName(List<AnnotationMember> members, String memberKeyName) {
        if (EmptyUtils.collectionNull(members) || EmptyUtils.stringNull(memberKeyName)) {
            return null;
        }
        for (AnnotationMember member : members) {
            String key = member.getKey();
            if (memberKeyName.equals(key)) {
                return member;
            }
        }
        return null;
    }

}
