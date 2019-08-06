package com.liz.androidutils;

/**
 * FileUtils:
 * Created by liz on 2019/1/14.
 */

public class FileUtils {
    /**
     * @return get file extension name from file absolute path
     */
    public static String getFileExtension(String fileAbsolute) {
        int index = fileAbsolute.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return fileAbsolute.substring(index + 1);
    }

    /**
     * @return get file path from file absolute path
     */
    public static String getFilePath(String fileAbsolute) {
        int index = fileAbsolute.lastIndexOf("/");
        if (index == -1) {
            return "";
        }
        return fileAbsolute.substring(0, index);
    }
}
