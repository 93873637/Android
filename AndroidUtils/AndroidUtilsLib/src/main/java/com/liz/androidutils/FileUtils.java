package com.liz.androidutils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

/**
 * FileUtils:
 * Created by liz on 2019/1/14.
 */

@SuppressWarnings("unused, WeakerAccess")
public class FileUtils {
    /**
     * @return get file extension name from file absolute path
     * such as: /home/liz/aaa.txt -> txt
     */
    public static String getFileExtension(String fileAbsolute) {
        final String FILE_EXT_SEPARATOR = ".";
        int index = fileAbsolute.lastIndexOf(FILE_EXT_SEPARATOR);
        if (index == -1) {
            return "";
        }
        return fileAbsolute.substring(index + 1);
    }

    /**
     * @return get file path from file absolute path
     * such as: /home/liz/aaa.txt -> /home/liz
     */
    public static String getFilePath(String fileAbsolute) {
        int index = fileAbsolute.lastIndexOf(File.separator);
        if (index == -1) {
            return "";
        }
        return fileAbsolute.substring(0, index);
    }

    /**
     * @return get file name from file path
     * such as:
     * /home/liz/aaa.txt -> aaa.txt
     * aaa.txt -> aaa.txt
     */
    public static String getFileName(String filePath) {
        int index = filePath.lastIndexOf(File.separator);
        if (index == -1) {
            return filePath;
        }
        return filePath.substring(index + 1);
    }

    /**
     * @return get file neat name from file path
     * such as:
     * /home/liz/aaa.txt -> aaa
     * aaa.txt -> aaa
     */
    public static String getFileNeatName(String filePath) {
        String fileName = getFileName(filePath);
        final String FILE_EXT_SEPARATOR = ".";
        int index = fileName.lastIndexOf(FILE_EXT_SEPARATOR);
        if (index == -1) {
            return "";
        }
        return fileName.substring(0, index);
    }

    public static void removeFile(String fileName) {
        removeFile(new File(fileName));
    }

    public static void removeFile(File f) {
        if (f == null) {
            System.out.println("ERROR: removeFile: file null");
            return;
        }
        if (!f.exists()) {
            System.out.println("ERROR: removeFile: file \"" + f.getAbsolutePath() + "\" not exist.");
            return;
        }
        if (!f.delete()) {
            System.out.println("ERROR: removeFile: delete file \"" + f.getAbsolutePath() + "\" failed.");
        }
    }

    public static void mv(String filePathFrom, String filePathTo) {
        //delete old file
        File fileTo = new File(filePathTo);
        if (fileTo.exists()) {
            if (!fileTo.delete()) {
                System.out.println("Delete to file " + filePathTo + " failed.");
                return;
            }
        }

        //rename file
        File fileFrom = new File(filePathFrom);
        if (fileFrom.exists()) {
            if (!fileFrom.renameTo(fileTo)) {
                System.out.println("Rename file to " + filePathTo + " failed.");
            }
        }
    }

    public static boolean touchDir(String filePath) {
        File path = new File(filePath);
        if (!path.exists()) {
            if (!path.mkdirs()) {
                LogUtils.e("touchDir: create path \"" + filePath + "\" failed.");
                return false;
            }
        }
        return true;
    }

    public static String dirSeparator(String dir) {
        if (dir.endsWith("/")) {
            return dir;
        }
        else {
            return dir + "/";
        }
    }

    public static final int ORDER_BY_DATE = 0;
    public static final int ORDER_BY_DATE_DESC = 1;

    public static File[] getFileList(String filePath) {
        File path = new File(filePath);
        if (!path.exists()) {
            LogUtils.e("getFileList: file path \"" + filePath + "\" not exists.");
            return new File[0];
        }
        File[] files = path.listFiles();
        if (files == null) {
            LogUtils.e("getFileList: file path \"" + filePath + "\" list files null.");
            return new File[0];
        }
        else {
            return files;
        }
    }

    public static File[] getFileList(String filePath, int order) {
        File[] files = getFileList(filePath);
        orderByDate(files, order == ORDER_BY_DATE_DESC);
        return files;
    }

    public static void orderByDate(@NonNull File[] files, boolean desc) {
        final int compare_result = desc ? -1 : 1;
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return compare_result;
                else if (diff == 0)
                    return 0;
                else
                    return -compare_result;
            }
        });
    }

    public static List<String> getFileNameList(String filePath) {
        List<String> list = new ArrayList<>();
        File path = new File(filePath);
        if (!path.exists()) {
            LogUtils.e("getFileList: file path \"" + filePath + "\" not exists.");
        }
        else {
            File[] files = path.listFiles();
            if (files == null) {
                LogUtils.e("getFileList: file path \"" + filePath + "\" list files null.");
            }
            else {
                for (File f : files) {
                    list.add(f.getName());
                }
            }
        }
        return list;
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

    public static String getFormattedFileSize(String fileAbsolutePath) {
        return formatFileSize(FileUtils.getFileSize(fileAbsolutePath));
    }

    public static String getFormattedFileSize(File f) {
        return formatFileSize(FileUtils.getFileSize(f));
    }

    public static long getFileSize(File file) {
        long size = 0;
        try {
            if (file != null && file.exists()) {
                FileInputStream fis;
                fis = new FileInputStream(file);
                size = fis.available();
            }
        }
        catch(Exception e) {
            LogUtils.e("ERROR: getFileSize exception " + e.toString());
        }
        return size;
    }

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

    public static String formatFileSize(long fileSize) {
        final long KB_V = 1024;   //real value
        final long KB_C = 1000;  //compare value, less than 4 degits
        final long MB_V = KB_V * KB_V;
        final long MB_C = KB_C * KB_C;
        final long GB_V = MB_V * KB_V;
        final long GB_C = MB_C * KB_C;

        String sizeString = "";
        DecimalFormat df = new DecimalFormat("#.0");
        if (fileSize < 0) {
            LogUtils.w("formatFileSize: wrong size " + fileSize);
        }
        else if (fileSize == 0) {
            sizeString = "0 B";
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

    public static boolean sameFile(String filePath1, String filePath2) throws IOException {
        FileInputStream fis1 = new FileInputStream(filePath1);
        long size1 = fis1.available();
        String md51 = DigestUtils.md5Hex(IOUtils.toByteArray(fis1));
        IOUtils.closeQuietly(fis1);

        FileInputStream fis2 = new FileInputStream(filePath2);
        long size2 = fis2.available();
        String md52 = DigestUtils.md5Hex(IOUtils.toByteArray(fis2));
        IOUtils.closeQuietly(fis2);
        return (size1 == size2) && md51.equals(md52);
    }

    public static ArrayList<String> readTxtFileLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
        {
            LogUtils.e("readTxtFileLines: file \"" + filePath + "\" not exists.");
            return null;
        }

        if (!file.isFile()){
            LogUtils.e("readTxtFileLines: file \"" + filePath + "\" is NOT a file.");
            return null;
        }

        try {
            ArrayList<String> lineList = new ArrayList<>();
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufferReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufferReader.readLine()) != null) {
                lineList.add(line);
            }
            inputStream.close();
            return lineList;
        } catch (java.io.FileNotFoundException e) {
            LogUtils.e("readTxtFileLines: FileNotFoundException of file \"" + filePath + "\".");
            return null;
        } catch (IOException e) {
            LogUtils.e("readTxtFileLines: read exception of file \"" + filePath + "\", ex=" + e.toString());
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        AssertUtils.Assert(getFileExtension("/home/liz/aaa.txt").equals("txt"));
        AssertUtils.Assert(getFileExtension("/home/liz/aaa.t").equals("t"));
        AssertUtils.Assert(getFileExtension("/home/liz/aaa").equals(""));

        AssertUtils.Assert(dirSeparator("/home/liz/aaa").equals("/home/liz/aaa/"));
        AssertUtils.Assert(dirSeparator("/home/liz/aaa/").equals("/home/liz/aaa/"));


        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.txt";  //for windows
            System.out.println("getFilePath(\"" + fileAbs + "\")=\"" + getFilePath(fileAbs) + "\"");
        }
        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.txt";  //for windows
            System.out.println("getFileName(\"" + fileAbs + "\")=\"" + getFileName(fileAbs) + "\"");
        }
        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.t";  //for windows
            System.out.println("getFileName(\"" + fileAbs + "\")=\"" + getFileName(fileAbs) + "\"");
        }
        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.";  //for windows
            System.out.println("getFileName(\"" + fileAbs + "\")=\"" + getFileName(fileAbs) + "\"");
        }
        {
            String fileAbs = "aaa.txt";
            System.out.println("getFileName(\"" + fileAbs + "\")=\"" + getFileName(fileAbs) + "\"");
        }
        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.txt";  //for windows
            System.out.println("getFileNeatName(\"" + fileAbs + "\")=\"" + getFileNeatName(fileAbs) + "\"");
        }
        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.t";  //for windows
            System.out.println("getFileNeatName(\"" + fileAbs + "\")=\"" + getFileNeatName(fileAbs) + "\"");
        }
        {
            //String fileAbs = "/home/liz/aaa.txt";  //for unix
            String fileAbs = "D:\\home\\liz\\aaa.";  //for windows
            System.out.println("getFileNeatName(\"" + fileAbs + "\")=\"" + getFileNeatName(fileAbs) + "\"");
        }
        {
            String fileAbs = "aaa.txt";
            System.out.println("getFileNeatName(\"" + fileAbs + "\")=\"" + getFileNeatName(fileAbs) + "\"");
        }
        {
            String fileAbs = ".txt";
            System.out.println("getFileNeatName(\"" + fileAbs + "\")=\"" + getFileNeatName(fileAbs) + "\"");
        }

        assert false;
        //String path1="D:\\Temp\\whatsai.zip";
        //String path2="D:\\Temp\\whatsai_tmp.zip";
//        String path1="D:\\Temp\\whatsai\\whatsai.dat";
//        String path2="D:\\Temp\\whatsai_tmp\\whatsai.dat";
//        if (sameFile(path1, path2)) {
//            System.out.println("same file");
//        }
//        else {
//            System.out.println("different file");
//        }
//        String path1="D:\\Temp\\test2.jpg";
//        String path2="D:\\Temp\\test3.jpg";
        //mv(path2, path1);
    }
}
