package com.liz.androidutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * Created by liz on 18-1-30.
 */

@SuppressWarnings("unused, WeakerAccess")
public class ZipUtils {

     public static boolean zip(String zipFileAbsolute, String fileAbsolute) {

         File zipFile = new File(zipFileAbsolute);
         if (zipFile.exists()) {
             LogUtils.i("zip file \""+ zipFileAbsolute +"\" already exists, delete it to re-zip...");
             if (zipFile.delete()) {
                 LogUtils.e("delete zip file \"" + zipFileAbsolute + "\" failed.");
                 return false;
             }
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
             LogUtils.i("ERROR: zip failed for input file \"" + fileAbsolute + "\" not found.");
             return false;
         }

         ZipOutputStream outputStream;
         try {
             outputStream = new ZipOutputStream(new FileOutputStream(zipFileAbsolute));
             zipFile(outputStream, inputFile, "");
         }
         catch (Exception ex) {
             LogUtils.e("ERROR: zip Exception: " + ex.toString());
             return false;
         }
         try {
             outputStream.close();
         }
         catch (IOException ex) {
             LogUtils.e("outputStream close IOException: " + ex.toString());
         }

         return true;
    }

    public static boolean zipFiles(String strZipFile, File... files) {
        List<File> fileList = new ArrayList<>();
        for(File f : files) {
            if (f != null) {
                fileList.add(f);
            }
        }
        return zipFiles(strZipFile, fileList);
    }

    public static boolean zipFileAbsolutes(String strZipFile, String... fileAbsolutes) {
        List<File> fileList = new ArrayList<>();
        for(String fileAbsolute : fileAbsolutes) {
            File f = new File(fileAbsolute);
            fileList.add(f);
        }
        return zipFiles(strZipFile, fileList);
    }

    public static boolean zipFileAbsoluteList(String strZipFile, List<String> fileAbsoluteList) {
        List<File> fileList = new ArrayList<>();
        for(String fileAbsolute : fileAbsoluteList) {
            File f = new File(fileAbsolute);
            fileList.add(f);
        }
        return zipFiles(strZipFile, fileList);
    }

    public static boolean zipFiles(String strZipFile, @NonNull List<File> fileList) {
        LogUtils.d("ZipUtils: zipFiles: strZipFile=" + strZipFile + ", fileListSize=" + fileList.size());

        if (TextUtils.isEmpty(strZipFile)) {
            LogUtils.e("ZipUtils: zipFiles: zip file empty");
            return false;
        }

        if (fileList.size() == 0) {
            LogUtils.e("ZipUtils: zipFiles: No file to zip");
            return false;
        }

        File zipFile = new File(strZipFile);
        if (zipFile.exists()) {
            LogUtils.i("ZipUtils: zipFiles: Zip file already exist, delete it to zip again...");
            if (zipFile.delete()) {
                LogUtils.e("ZipUtils: zipFiles: Delete old zip file \"" + strZipFile + "\" failed.");
                return false;
            }
        }

        boolean ret = false;
        ZipOutputStream outputStream = null;
        try {
            String filePath = strZipFile.substring(0, strZipFile.lastIndexOf("/"));
            touchPath(filePath);
            outputStream = new ZipOutputStream(new FileOutputStream(strZipFile));
            for (int i = 0; i< fileList.size(); i++) {
                zipFile(outputStream, fileList.get(i), "");
                ret = true;
            }
        }
        catch (Exception ex) {
            LogUtils.e("zip Exception: " + ex.toString());
            return false;
        }
        finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ex) {
                LogUtils.e("zipFiles Exception: " + ex.toString());
            }
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
            if (files != null) {
                for (File file : files) {
                    zipFile(out, file, path + fn + "/");
                }
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

    private static boolean touchPath(String strPath) {
        File fPath = new File(strPath);
        if (!fPath.exists()) {
            return fPath.mkdirs();
        }
        return true;
    }
}
