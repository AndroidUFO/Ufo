package com.androidufo.ufo.model;

public class Progress {
    private static final long KB = 1024;
    private static final long MB = 1024 * 1024;

    public long progress;
    public long total;
    public int percent;

    public Progress(long progress, long total, int percent) {
        this.progress = progress;
        this.total = total;
        this.percent = percent;
    }

    public String getMBProgress() {
        return bitToMB(progress);
    }

    public String getKBProgress() {
        return bitToKB(progress);
    }

    public String getMBTotal() {
        return bitToMB(total);
    }

    public String getKBTotal() {
        return bitToKB(total);
    }

    private String bitToMB(long value) {
        float mb = 1.0f * value / MB;
        return String.format("%.2fMB", mb);
    }

    private String bitToKB(long value) {
        float mb = 1.0f * value / KB;
        return String.format("%.0fKB", mb);
    }
}
