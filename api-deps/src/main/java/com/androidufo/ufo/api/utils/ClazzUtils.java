package com.androidufo.ufo.api.utils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;

public class ClazzUtils {

    /**
     * 判断child是否father的子类或者同一类型：忽略泛型
     */
    public static boolean isChildOrSameType(Elements elements, Types types, TypeMirror child, Class<?> fatherClazz, Messager messager) {
        // 对比的时候两个类可能会存在泛型，这时比较就不会相等，因此要忽略泛型获取通用类名然后比较
        TypeElement fatherElement = elements.getTypeElement(fatherClazz.getCanonicalName());
        TypeElement childElement = (TypeElement) types.asElement(child);
        if (childElement == null) {
            return false;
        }
        return selfAndSuperClassAndInterfaceMatch(elements, types, messager, childElement, fatherElement);
    }

    public static boolean isChildOrSameType(Elements elements, Types types, TypeMirror child, TypeElement fatherElement, Messager messager) {
        // 对比的时候两个类可能会存在泛型，这时比较就不会相等，因此要忽略泛型获取通用类名然后比较
        TypeElement childElement = (TypeElement) types.asElement(child);
        if (childElement == null) {
            return false;
        }
        return selfAndSuperClassAndInterfaceMatch(elements, types, messager, childElement, fatherElement);
    }

    public static boolean isChildOrSameType(Elements elements, Types types, TypeMirror child, String fatherCanonicalName, Messager messager) {
        // 对比的时候两个类可能会存在泛型，这时比较就不会相等，因此要忽略泛型获取通用类名然后比较
        TypeElement fatherElement = elements.getTypeElement(fatherCanonicalName);
        TypeElement childElement = (TypeElement) types.asElement(child);
        if (childElement == null) {
            return false;
        }
        return selfAndSuperClassAndInterfaceMatch(elements, types, messager, childElement, fatherElement);
    }

    private static boolean interfaceMatch(Elements elements, Types types, Messager messager, TypeElement childElement, TypeElement fatherElement) {
        List<? extends TypeMirror> interfaces = childElement.getInterfaces();
        if (interfaces != null) {
            for (TypeMirror inter : interfaces) {
                if (selfAndSuperClassAndInterfaceMatch(elements, types, messager, (TypeElement) types.asElement(inter), fatherElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 使用多重递归，将当前类，当前类的父类，当前类实现的接口与FatherElement进行对比
     * @param elements
     * @param types
     * @param messager
     * @param childElement
     * @param fatherElement
     * @return
     */
    private static boolean selfAndSuperClassAndInterfaceMatch(Elements elements, Types types, Messager messager, TypeElement childElement, TypeElement fatherElement) {
        TypeMirror superclass = childElement.asType();
        // 如果父类是自定义的类，则满足条件
        while (superclass != null && superclass.getKind() == TypeKind.DECLARED) {
            // 对比当前类与fatherElement是否是父子关系
            TypeElement element = (TypeElement) types.asElement(superclass);
            if (isAssignableIgnoreT(elements, types, messager, element, fatherElement)) {
                return true;
            }
            // 对比当前类的接口以及接口父类与fatherElement是否是父子关系
            if (interfaceMatch(elements, types, messager, element, fatherElement)) {
                return true;
            }
            superclass = element.getSuperclass();
        }
        return false;
    }

    /**
     * 对比两个类是否是父子关系，并且忽略泛型
     * @param elements
     * @param types
     * @param childElement 未忽略泛型
     * @param fatherElement 已经忽略了泛型
     * @return
     */
    private static boolean isAssignableIgnoreT(Elements elements, Types types, Messager messager, TypeElement childElement, TypeElement fatherElement) {
        String qualifiedName = childElement.getQualifiedName().toString();
//        messager.printMessage(Diagnostic.Kind.NOTE, "child qualifiedName = " + qualifiedName
//                + ", father qualifiedName = " + fatherElement.getQualifiedName());
        TypeElement child = elements.getTypeElement(qualifiedName);
        return types.isAssignable(child.asType(), fatherElement.asType());
    }

    public static List<String> getInnerGenericClazzNameList(String clazzName, Messager messager) {
        List<String> list = new ArrayList<>();
        String innerClazzName = getInnerGenericClazzName(null, clazzName, messager);
        while (innerClazzName != null) {
            innerClazzName = getInnerGenericClazzName(list, innerClazzName, messager);
        }
        if (list.size() > 0) {
            List<String> newList = new ArrayList<>();
            // 将包路径去掉
            for (String fullName : list) {
                int index = fullName.lastIndexOf(".");
                if (index != -1) {
                    String simpleName = fullName.substring(index+1);
                    newList.add(simpleName);
                } else {
                    newList.add(fullName);
                }
            }
            return newList;
        } else {
            return list;
        }
    }

    private static String getInnerGenericClazzName(List<String> innerClazzList, String outerClazzFullName, Messager messager) {
        if (outerClazzFullName == null || outerClazzFullName.trim().equals("")) {
            return null;
        }
//        messager.printMessage(Diagnostic.Kind.NOTE, "outerClazzFullName = " + outerClazzFullName);
        int left = outerClazzFullName.indexOf("<");
        int right = outerClazzFullName.lastIndexOf(">");
        if (left != -1 && right != -1) {
            String innerClazzName = outerClazzFullName.substring(left + 1, right);
            String outerClazzName = outerClazzFullName.substring(0, left);
            if (innerClazzList != null) {
                innerClazzList.add(outerClazzName);
            }
            return innerClazzName;
        } else {
            if (innerClazzList != null) {
                innerClazzList.add(outerClazzFullName);
            }
        }
        return null;
    }

    public static boolean isString(TypeMirror typeMirror) {
        return typeMirror.toString().equals(String.class.getCanonicalName());
    }
}
