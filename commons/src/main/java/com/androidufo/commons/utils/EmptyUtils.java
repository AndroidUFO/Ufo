package com.androidufo.commons.utils;

import java.util.Collection;
import java.util.Map;

public class EmptyUtils {

    public static boolean stringNull(String str) {
        return str == null || str.length() == 0;
    }

    public static <K, V> boolean mapNull(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <T> boolean collectionNull(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

}
