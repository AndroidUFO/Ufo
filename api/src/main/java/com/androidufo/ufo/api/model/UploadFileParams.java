package com.androidufo.ufo.api.model;

import com.androidufo.commons.utils.EmptyUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadFileParams {

    private Builder builder;

    private UploadFileParams(Builder builder) {
        this.builder = builder;
    }

    public Map<String, File> toFiles() {
        return builder.files;
    }

    public static class Builder {
        private final Map<String, File> files = new HashMap<>();
        private static final String DEFAULT_FILE_KEY = "UfoFile";
        private int keyIndex = 0;

        public Builder file(@NotNull String key, @NotNull String fileName) {
            if (!EmptyUtils.stringNull(fileName)) {
                files.put(key, new File(fileName));
            }
            return this;
        }

        public Builder file(@NotNull String key, @NotNull File file) {
            files.put(key, file);
            return this;
        }

        public Builder file(@NotNull String fileName) {
            if (!EmptyUtils.stringNull(fileName)) {
                files.put(getDefaultKey(), new File(fileName));
            }
            return this;
        }

        public Builder file(@NotNull File file) {
            files.put(getDefaultKey(), file);
            return this;
        }

        public Builder files(@NotNull List<File> fileList) {
            if (!EmptyUtils.collectionNull(fileList)) {
                for (File file : fileList) {
                    files.put(getDefaultKey(), file);
                }
            }
            return this;
        }

        public Builder files(@NotNull Map<String, File> fileMap) {
            if (!EmptyUtils.mapNull(fileMap)) {
                files.putAll(fileMap);
            }
            return this;
        }

        public Builder fileNames(@NotNull List<String> fileList) {
            if (!EmptyUtils.collectionNull(fileList)) {
                for (String fileName : fileList) {
                    files.put(getDefaultKey(), new File(fileName));
                }
            }
            return this;
        }

        public Builder fileNames(@NotNull Map<String, String> fileMap) {
            if (!EmptyUtils.mapNull(fileMap)) {
                for (Map.Entry<String, String> entry : fileMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (!EmptyUtils.stringNull(value)) {
                        files.put(key, new File(value));
                    }
                }
            }
            return this;
        }

        public UploadFileParams build() {
            return new UploadFileParams(this);
        }

        private String getDefaultKey() {
            String key;
            if (keyIndex == 0) {
                key = DEFAULT_FILE_KEY;
            } else {
                key = DEFAULT_FILE_KEY + keyIndex;
            }
            keyIndex++;
            return key;
        }
    }


}
