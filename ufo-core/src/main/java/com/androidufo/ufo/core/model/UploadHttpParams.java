package com.androidufo.ufo.core.model;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.commons.utils.GSonUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadHttpParams extends BaseHttpParams<UploadHttpParams.Builder, UploadHttpParams> {

    private UploadHttpParams(Builder builder) {
        super(builder);
    }

    public Map<String, File> files() {
        return builder.files;
    }

    public Map<String, String> formBody() {
        return builder.formBody;
    }

    public static class Builder extends BaseHttpParams.Builder<UploadHttpParams, Builder> {
        private static final String DEFAULT_FILE_KEY = "UfoFile";
        // 当传入的上传多个文件没有设置key时，需要添加默认key，如果上传key重复，
        // 只会上传最后添加那个文件，这里为了防止出现覆盖问题，没有key的文件添加自定义key
        private int keyIndex = 0;
        private final Map<String, File> files = new HashMap<>();
        private Map<String, String> formBody;

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

        public Builder body(@NotNull String key, @NotNull String value) {
            checkBodies();
            formBody.put(key, value);
            return this;
        }

        public Builder body(@NotNull Map<String, String> bodyMap) {
            if (!EmptyUtils.mapNull(bodyMap)) {
                checkBodies();
                formBody.putAll(bodyMap);
            }
            return this;
        }

        public Builder body(Object javaBean) {
            if (javaBean != null) {
                Map<String, String> map = GSonUtils.toMap(javaBean);
                if (!EmptyUtils.mapNull(map)) {
                    checkBodies();
                    formBody.putAll(map);
                }
            }
            return this;
        }

        private void checkBodies() {
            if (formBody == null) {
                formBody = new HashMap<>();
            }
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

        @Override
        public UploadHttpParams createHttpParams() {
            return new UploadHttpParams(this);
        }
    }
}
