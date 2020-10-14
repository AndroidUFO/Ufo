package com.androidufo.ufo.api.model;

public class HttpsInfos {
    private String assetsSslCer;
    private String assetsBks;
    private String bksPassword;

    public HttpsInfos(String assetsSslCer, String assetsBks, String bksPassword) {
        this.assetsSslCer = assetsSslCer;
        this.assetsBks = assetsBks;
        this.bksPassword = bksPassword;
    }

    public String getAssetsSslCer() {
        return assetsSslCer;
    }

    public String getAssetsBks() {
        return assetsBks;
    }

    public String getBksPassword() {
        return bksPassword;
    }

    @Override
    public String toString() {
        return "HttpsInfos{" +
                "assetsSslCer='" + assetsSslCer + '\'' +
                ", assetsBks='" + assetsBks + '\'' +
                ", bksPassword='" + bksPassword + '\'' +
                '}';
    }
}
