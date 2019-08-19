package com.liz.androidutils;

import java.io.File;

/**
 * FileUtils:
 * Created by liz on 2019/1/14.
 */

@SuppressWarnings("unused, WeakerAccess")
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
        int index = fileAbsolute.lastIndexOf(File.separator);
        if (index == -1) {
            return "";
        }
        return fileAbsolute.substring(0, index);
    }

    public static void removeFile(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            //System.out.println("image file " + fileName + " already exist, delete it");
            if (!f.delete()) {
                System.out.println("Delete file " + fileName + " failed.");
            }
        }
    }
}
