package com.androidufo.ufo.api.utils;

import com.androidufo.commons.utils.EmptyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestUrlParser {

    private static final String SEPARATOR = ":";

    private List<String> pathArgs;
    private final String matchUrl;
    private String argsStr;

    private RestUrlParser(List<String> pathArgs, String matchUrl) {
        this.pathArgs = pathArgs;
        this.matchUrl = matchUrl;
        if (!EmptyUtils.collectionNull(pathArgs)) {
            // 注意：这里最好不要使用GSON，如果解析成数组顺序错误，就会导致参数拼接错误
            this.argsStr = argListToStr(pathArgs);
            System.out.println("matchUrl ====== " + matchUrl);
            System.out.println("argsStr ====== " + argsStr);
        }
    }

    public RestUrlParser(String matchUrl, String argsStr) {
        this.matchUrl = matchUrl;
        this.argsStr = argsStr;
    }

    public List<String> getPathArgs() {
        return pathArgs;
    }

    public String getMatchUrl() {
        return matchUrl;
    }

    public String getArgsStr() {
        return argsStr;
    }

    private static String argListToStr(List<String> args) {
        if (EmptyUtils.collectionNull(args)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(SEPARATOR);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static List<String> argStrToList(String argStr) {
        if (EmptyUtils.stringNull(argStr)) {
            return null;
        }
        String[] split = argStr.split(SEPARATOR);
        return Arrays.asList(split);
    }

    public static RestUrlParser parseRestUrl(String restUrl) {
        if (EmptyUtils.stringNull(restUrl)) {
            return null;
        }
        // {xxx}的正则表达式
        String pattern = "\\{[^\\}]+\\}";
        StringBuilder matchUrl = new StringBuilder(restUrl);
        Matcher matcher = Pattern.compile(pattern).matcher(matchUrl);
        // 用于保存url中{}中的变量名称，也就是map中对应数据的key值
        List<String> args = new ArrayList<>();
        while (matcher.find()) {
            String matchStr = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            String argName = matchStr.replace("{", "").replace("}", "").trim();
            args.add(argName);
            matchUrl.replace(start, end, "$");
            matcher = Pattern.compile(pattern).matcher(matchUrl);
        }
        return new RestUrlParser(args, matchUrl.toString());
    }

    public String makeRestUrl(Map<String, String> pathParams) {
        if (EmptyUtils.stringNull(matchUrl)) {
            return "";
        }
        if (EmptyUtils.stringNull(argsStr)) {
            return matchUrl;
        }
        if (EmptyUtils.mapNull(pathParams)) {
            return matchUrl;
        }
        pathArgs = RestUrlParser.argStrToList(argsStr);
        int argsSize = pathArgs.size();
        String[] split = matchUrl.split("\\$");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String str = split[i];
            result.append(str);
            if (i < argsSize) {
                String key = pathArgs.get(i);
                if (!pathParams.containsKey(key)) {
                    throw new RuntimeException("url中对应的参数" + key + "没有定义，无法解析！");
                }
                Object value = pathParams.get(key);
                result.append(value == null ? "" : value);
            }
        }
        return result.toString();
    }

}
