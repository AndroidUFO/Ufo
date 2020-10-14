package com.androidufo.commons.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GSonUtils {
    private final static Gson sGSon;

    static {
        sGSon = new GsonBuilder().disableHtmlEscaping().create();
    }

    public static Gson instance() {
        return sGSon;
    }

    public static <T> String toJson(T bean) {
        try {
            return sGSon.toJson(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        try {
            return sGSon.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T toBean(String json, Type type) {
        try {
            return sGSon.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> ArrayList<T> toList(String json, Class<T> clazz) {
        try {
            Type type = new TypeToken<ArrayList<JsonObject>>() {
            }.getType();
            ArrayList<JsonObject> jsonObjList = sGSon.fromJson(json, type);
            ArrayList<T> listOfT = new ArrayList<>();
            for (JsonObject jsonObj : jsonObjList) {
                listOfT.add(sGSon.fromJson(jsonObj, clazz));
            }
            return listOfT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<Map<String, T>> toListMap(String json) {
        try {
            return sGSon.fromJson(json, new TypeToken<List<Map<String, T>>>() {}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Map<String, T> toMapT(String json) {
        try {
            return sGSon.fromJson(json, new TypeToken<Map<String, T>>() {}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> toMap(String json) {
        try {
            return sGSon.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Map<String, String> toMap(T bean) {
        String json = toJson(bean);
        if (json == null) {
            return null;
        }
        return toMap(json);
    }

}