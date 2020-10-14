package com.androidufo.ufo.utils;

import android.os.Environment;
import com.androidufo.commons.utils.EmptyUtils;

import java.io.File;

public class StorageUtils {
    // TODO 根据Android版本判断，使用SDCARD还是应用内置目录
    private static String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String UFO_DIR;

    static {
        UFO_DIR = SDCARD + File.separator + "AndroidUFO" + File.separator + "Download" + File.separator;
    }

    public static String getFilePath(String fileDir, String fileName) {
        if (EmptyUtils.stringNull(fileDir)) {
            return fileName;
        }
        if (EmptyUtils.stringNull(fileName)) {
            return fileDir;
        }
        boolean dirEnd = fileDir.endsWith(File.separator);
        boolean fileStart = fileName.startsWith(File.separator);
        if (dirEnd) {
            if (fileStart) {
                return fileDir + fileName.substring(1);
            }
            return fileDir + fileName;
        } else {
            if (fileStart) {
                return fileDir + fileName;
            }
            return fileDir + File.separator + fileName;
        }
    }

}
