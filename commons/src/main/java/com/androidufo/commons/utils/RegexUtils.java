package com.androidufo.commons.utils;

public class RegexUtils {

    private static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    /**
     * Check the URL is valid
     * @param url url(http/https/ftp and so on)
     * @return true if the URL is valid, other false
     */
    public static boolean isValidUrl(String url) {
        if (url == null) return false;
        return url.matches(URL_REGEX);
    }
}
