package com.liz.screenhelper.utils;

/**
 * FileUtils:
 * Created by liz on 2019/1/14.
 */

public class FileUtils {
    /**
     * 获取文件扩展名
     * @return
     */
    public static String ext(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return filename.substring(index + 1);
    }
}
