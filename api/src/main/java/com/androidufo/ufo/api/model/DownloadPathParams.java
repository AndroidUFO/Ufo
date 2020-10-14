package com.androidufo.ufo.api.model;

import org.jetbrains.annotations.NotNull;

public class DownloadPathParams {

    private String fileDir;
    private String fileName;

    public DownloadPathParams() {
    }

    public DownloadPathParams(@NotNull String fileName) {
        this.fileName = fileName;
    }

    public DownloadPathParams(@NotNull String fileDir, @NotNull String fileName) {
        this.fileDir = fileDir;
        this.fileName = fileName;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
