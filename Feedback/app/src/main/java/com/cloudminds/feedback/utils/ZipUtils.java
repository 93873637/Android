package com.cloudminds.feedback.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by liz on 18-1-30.
 */

public class ZipUtils {

     public static boolean zip(String zipFileAbsolute, String fileAbsolute) {

         File zipFile = new File(zipFileAbsolute);
         if (zipFile != null && zipFile.exists()) {
             LogUtils.i("zip file already exist, delete it to zip again");
             zipFile.delete();
         }
         else {
             //ensure output path exists
             try {
                 String zipFilePath = zipFileAbsolute.substring(0, zipFileAbsolute.lastIndexOf("/"));
                 touchPath(zipFilePath);
             } catch (Exception ex) {
                 LogUtils.e("ERROR: Exception for touch zip file path: " + ex.toString());
                 return false;
             }
         }

         //check input file for zip
         File inputFile = new File(fileAbsolute);
         if (!inputFile.exists()) {
             LogUtils.i("ERROR: zip failed for input file not found.");
             return false;
         }

         ZipOutputStream outputStream = null;
         try {
             outputStream = new ZipOutputStream(new FileOutputStream(zipFileAbsolute));
             zipFile(outputStream, inputFile, "");
         }
         catch (Exception ex) {
             LogUtils.e("ERROR: zip Exception: " + ex.toString());
             return false;
         }

         try {
             if (outputStream != null) {
                 outputStream.close();
             }
         }
         catch (IOException ex) {
             LogUtils.e("outputStream close IOException: " + ex.toString());
         }

         return true;
    }

    /**
     * zip: zip @param files to @param strZipFile
     * @param strZipFile: zip file name with full path
     * @param files: files to zip
     * @return: true if success or false
     */
    public static boolean zipFiles(String strZipFile, File... files) {
        LogUtils.d("ZipUtils.zip: strZipFile=" + strZipFile);

        if (TextUtils.isEmpty(strZipFile)) {
            LogUtils.e("zip output file not valid");
            return false;
        }

        if (files.length == 0) {
            LogUtils.e("no file to zip");
            return false;
        }

        File zipFile = new File(strZipFile);
        if (zipFile != null && zipFile.exists()) {
            LogUtils.i("zip file already exist, delete it to zip again");
            zipFile.delete();
        }

        boolean ret = false;
        ZipOutputStream outputStream = null;
        try {
            String filePath = strZipFile.substring(0, strZipFile.lastIndexOf("/"));
            touchPath(filePath);
            outputStream = new ZipOutputStream(new FileOutputStream(strZipFile));
            for (int i=0; i<files.length; i++) {
                if (files[i] != null) {
                    zipFile(outputStream, files[i], "");
                    ret = true;
                }
            }
        }
        catch (Exception ex) {
            LogUtils.e("zip Exception: " + ex.toString());
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        catch (IOException ex) {
            LogUtils.e("zip IOException: " + ex.toString());
        }

        return ret;
    }

    public static boolean zipFiles(String strZipFile, List<String> fileAbsoluteList) {
        LogUtils.d("ZipUtils.zip: strZipFile=" + strZipFile);

        if (TextUtils.isEmpty(strZipFile)) {
            LogUtils.e("zip output file not valid");
            return false;
        }

        if (fileAbsoluteList.size() == 0) {
            LogUtils.e("no file to zip");
            return false;
        }

        File zipFile = new File(strZipFile);
        if (zipFile != null && zipFile.exists()) {
            LogUtils.i("zip file already exist, delete it to zip again");
            zipFile.delete();
        }

        boolean ret = false;
        ZipOutputStream outputStream = null;
        try {
            //create output file path if not exist
            String filePath = strZipFile.substring(0, strZipFile.lastIndexOf("/"));
            touchPath(filePath);

            //zip files one by one
            outputStream = new ZipOutputStream(new FileOutputStream(strZipFile));
            for (int i = 0; i< fileAbsoluteList.size(); i++) {
                File file = new File(fileAbsoluteList.get(i));
                if (file != null) {
                    zipFile(outputStream, file, "");
                    ret = true;
                }
            }
        }
        catch (Exception ex) {
            LogUtils.e("zip Exception: " + ex.toString());
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        catch (IOException ex) {
            LogUtils.e("zip IOException: " + ex.toString());
        }

        return ret;
    }

    /**
     * zip the given file or dir to output stream
     * out: output stream to zip with
     * f: file or dir obj
     * path: current path of the file/dir
     */
    private static void zipFile(ZipOutputStream out, File f, String path) throws Exception{
        final int READ_BUFFER_SIZE = 2048000;
        String fn = f.getName();
        if (f.isDirectory()) {
            //LogUtils.e("zip dir: " + fileName);
            File[] files = f.listFiles();
            for (int i=0; i<files.length; i++) {
                //recursive zip
                zipFile(out, files[i], path + fn + "/");
            }
        }
        else {
            //LogUtils.e("zip file: " + fileName);
            out.putNextEntry(new ZipEntry(path + fn));

            int bytes;
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            FileInputStream in = new FileInputStream(f);
            while ((bytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
            }
            out.closeEntry();
            in.close();
        }
    }

    private static void touchPath(String strPath) {
        File fPath = new File(strPath);
        if (!fPath.exists()) {
            fPath.mkdirs();
        }
    }
}
