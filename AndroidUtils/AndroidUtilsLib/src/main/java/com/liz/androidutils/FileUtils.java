package com.liz.androidutils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
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
 *
 * NOTE: don't using LogUtils, since LogUtils has function which called this file
 * or you will get recursive error, lead to stack overflow
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
                System.out.println("touchDir: create path \"" + filePath + "\" failed.");
                return false;
            }
        }
        return true;
    }

    public static String formatDirSeparator(String dir) {
        if (dir.endsWith(File.separator)) {
            return dir;
        }
        else {
            return dir + File.separator;
        }
    }

    public static final int ORDER_BY_DATE = 0;
    public static final int ORDER_BY_DATE_DESC = 1;

    public static File[] getFileList(String filePath) {
        File path = new File(filePath);
        if (!path.exists()) {
            System.out.println("getFileList: file path \"" + filePath + "\" not exists.");
            return new File[0];
        }
        File[] files = path.listFiles();
        if (files == null) {
            System.out.println("getFileList: file path \"" + filePath + "\" list files null.");
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
            System.out.println("getFileList: file path \"" + filePath + "\" not exists.");
        }
        else {
            File[] files = path.listFiles();
            if (files == null) {
                System.out.println("getFileList: file path \"" + filePath + "\" list files null.");
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
            System.out.println("ERROR: getFileSize exception " + e.toString());
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
            System.out.println("getFileSize exception: " + e.toString());
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
            System.out.println("WARNING: formatFileSize: wrong size " + fileSize);
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
        if (!file.exists()) {
            System.out.println("ERROR: readTxtFileLines: file \"" + filePath + "\" not exists.");
            return null;
        }

        if (!file.isFile()) {
            System.out.println("ERROR: readTxtFileLines: file \"" + filePath + "\" is NOT a file.");
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
            System.out.println("ERROR: readTxtFileLines: FileNotFoundException of file \"" + filePath + "\".");
            return null;
        } catch (IOException e) {
            System.out.println("ERROR: readTxtFileLines: read exception of file \"" + filePath + "\", ex=" + e.toString());
            return null;
        }
    }

    public static boolean appendTxtFile(@NonNull String fileAbsolute, @NonNull String content)
    {
        try {
            File file = new File(fileAbsolute);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println("ERROR: appendTxtFile: FileNotFoundException of file \"" + fileAbsolute + "\".");
                    return false;
                }
            }

            if (!file.isFile()) {
                System.out.println("ERROR: appendTxtFile: file \"" + fileAbsolute + "\" is NOT a file!");
                return false;
            }

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(content.getBytes());
            raf.close();
        } catch (Exception e) {
            System.out.println("ERROR: appendTxtFile: exception = " + e.toString());
            return false;
        }

        return true;
    }

    public static boolean writeTxtFile(@NonNull final String fileAbsolute,@NonNull final String content) {
        return writeTxtFile(fileAbsolute, content, false, false);
    }
        /*
     * @param buffer   写入文件的内容
     * @param fileAbsolute   保存文件名, 全路径
     * @param append   是否追加写入，true为追加写入，false为重写文件
     * @param autoLine 针对追加模式，true为增加时换行，false为增加时不换行
     */
    public static boolean writeTxtFile(@NonNull final String fileAbsolute,@NonNull final String content,
                                       final boolean append, final boolean autoLine) {

        RandomAccessFile raf = null;
        FileOutputStream out = null;
        try {
            File file = new File(fileAbsolute);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println("ERROR: writeTxtFile: FileNotFoundException of file \"" + fileAbsolute + "\".");
                    return false;
                }
            }

            if (!file.isFile()) {
                System.out.println("ERROR: writeTxtFile: file \"" + fileAbsolute + "\" is NOT a file!");
                return false;
            }

            if (append) {
                raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(content.getBytes());
                if (autoLine) {
                    raf.write("\n".getBytes());
                }
            } else {
                out = new FileOutputStream(file);
                out.write(content.getBytes());
                out.flush();
            }

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: writeTxtFile: exception = " + e.toString());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.out.println("ERROR: writeTxtFile: close exception = " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public static boolean delete(String fileAbsolute) {
        File file = new File(fileAbsolute);
        if (!file.exists()) {
            System.out.println("ERROR: FileUtils.delete: " + fileAbsolute + " not exist");
            return false;
        } else {
            if (file.isFile()) {
                return deleteFile(fileAbsolute);
            }
            else if (file.isDirectory()){
                return deleteDirectory(fileAbsolute);
            }
            else {
                System.out.println("ERROR: FileUtils.delete: " + fileAbsolute + " not file/dir");
                return false;
            }
        }
    }

    public static boolean deleteFile(String fileAbsolute) {
        File file = new File(fileAbsolute);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                System.out.println("ERROR: FileUtils.deleteFile: delete " + fileAbsolute + " failed");
                return false;
            }
        } else {
            System.out.println("ERROR: FileUtils.deleteFile: " + fileAbsolute + " not exist");
            return false;
        }
    }

    public static boolean isExists(String fileAbsolute) {
        if (TextUtils.isEmpty(fileAbsolute)) {
            System.out.println("ERROR: isExist: fileAbsolute empty");
            return false;
        }
        return (new File(fileAbsolute)).exists();
    }

    public static boolean deleteDirectory(String dir) {
        //add separator on end of dir
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;

        File dirFile = new File(dir);
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("ERROR: FileUtils.deleteDirectory: " + dir + " not exist");
            return false;
        }

        boolean flag = true;

        // delete all sub files and dirs
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            String absolutePath = files[i].getAbsolutePath();
            if (files[i].isFile()) {
                flag = deleteFile(absolutePath);
            }
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i].getAbsolutePath());
            }
            else {
                System.out.println("ERROR: FileUtils.deleteDirectory: " + absolutePath + " not file/dir");
                flag = false;
                break;
            }
            if (!flag) {
                System.out.println("ERROR: FileUtils.deleteDirectory: delete " + absolutePath + " failed");
                break;
            }
        }
        if (!flag) {
            System.out.println("ERROR: FileUtils.deleteDirectory: delete dir " + dir + " failed");
            return false;
        }

        // finally, delete the empty dir
        if (!dirFile.delete()) {
            System.out.println("ERROR: FileUtils.deleteDirectory: dirFile delete of " + dir + " failed");
            return false;
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {

        AssertUtils.Assert(writeTxtFile("D:\\Temp\\test.txt", "aaa\n"));
        AssertUtils.Assert(writeTxtFile("D:\\Temp\\test.txt", "bbb\n"));
        AssertUtils.Assert(writeTxtFile("D:\\Temp\\test.txt", "ccc\n", true, false));
        //AssertUtils.Assert(appendTxtFile("D:\\Temp\\test.txt", "aaa\n"));
        //AssertUtils.Assert(appendTxtFile("D:\\Temp\\test.txt", "bbb\n"));

        /*
        //assert true
        AssertUtils.Assert(getFileExtension("/home/liz/aaa.txt").equals("txt"));
        AssertUtils.Assert(getFileExtension("/home/liz/aaa.t").equals("t"));
        AssertUtils.Assert(getFileExtension("/home/liz/aaa").equals(""));

        AssertUtils.Assert(formatDirSeparator("/home/liz/aaa").equals("/home/liz/aaa" + File.separator));
        AssertUtils.Assert(formatDirSeparator("/home/liz/aaa" + File.separator).equals("/home/liz/aaa" + File.separator));

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
        //*/
    }
}
