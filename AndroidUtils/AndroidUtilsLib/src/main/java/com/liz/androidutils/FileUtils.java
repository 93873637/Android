package com.liz.androidutils;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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


    /**
     * Format As:
     * Log_IMEI_YYMMDD_hhmmss.zip
     * Log_862851030258951_180122_161632.zip
     */
    public static String genLogZipName(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");
        String strDateTime = format.format(new Date(System.currentTimeMillis()));
        return "log_" + SysUtils.getIMEI(context) + "_" + strDateTime + ".zip";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getFormattedFileSize(String fileAbsolutePath) {
        return FormatFileSize(FileUtils.getFileSize(fileAbsolutePath));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static long getFileSize(String filePath) {
        long size = 0;
        try {
            File f = new File(filePath);
            if (f.isDirectory()) {
                size = getFolderSize(f);
            } else {
                size = getFileSize(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("getFileSize exception: " + e.toString());
        }
        return size;
    }

    private static long getFolderSize(File f) throws Exception {
        long size = 0;
        File fileList[] = f.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                size = size + getFolderSize(fileList[i]);
            } else {
                size = size + getFileSize(fileList[i]);
            }
        }
        return size;
    }

    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file != null && file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String FormatFileSize(long fileSize) {
        final long KB_V = 1024;   //real value
        final long KB_C = 1000;  //compare value, less than 4 degits
        final long MB_V = KB_V * KB_V;
        final long MB_C = KB_C * KB_C;
        final long GB_V = MB_V * KB_V;
        final long GB_C = MB_C * KB_C;

        String sizeString = "";
        DecimalFormat df = new DecimalFormat("#.0");
        if (fileSize <= 0) {
            LogUtils.w("FormatFileSize: wrong size " + fileSize);
        }
        else if (fileSize < KB_C) {
            sizeString = df.format((double) fileSize)+ " B";
        }
        else if (fileSize < MB_C) {
            sizeString = df.format((double) fileSize / KB_V) + " KB";
        }
        else if (fileSize < GB_C) {
            sizeString = df.format((double) fileSize / MB_V)+ " MB";
        }
        else {
            sizeString = df.format((double) fileSize / GB_V) + " GB";
        }

        return sizeString;
    }
}
