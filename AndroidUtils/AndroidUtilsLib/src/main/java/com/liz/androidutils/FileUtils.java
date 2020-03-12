package com.liz.androidutils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

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

/**
 * FileUtils:
 * Created by liz on 2019/1/14.
 *
 * NOTE: don'trace using LogUtils, since LogUtils has function which called this file
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
     * @return replace current file extension name with new one
     * such as:
     * replaceFileExtension("/home/liz/aaa.pcm", "wav") = /home/liz/aaa.wav
     */
    public static String replaceFileExtension(String fileAbsolute, String extName) {
        final String FILE_EXT_SEPARATOR = ".";
        return getFilePathNeat(fileAbsolute) + FILE_EXT_SEPARATOR + extName;
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
     * @return get file neat name from file path without path and extension name
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

    /**
     * @return get file neat name from file path without extension name, but have path
     * such as:
     * /home/liz/aaa.txt -> /home/liz/aaa
     * aaa.txt -> aaa
     */
    public static String getFilePathNeat(String filePath) {
        final String FILE_EXT_SEPARATOR = ".";
        int index = filePath.lastIndexOf(FILE_EXT_SEPARATOR);
        if (index == -1) {
            return filePath;  //no ext, take all path as neat
        }
        else {
            return filePath.substring(0, index);
        }
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

    public static void mv(String fromFilePath, String toFilePath) {
        // first delete toFile if exists
        File toFile = new File(toFilePath);
        if (toFile.exists()) {
            if (!toFile.delete()) {
                System.out.println("Delete toFile " + toFilePath + " failed.");
                return;
            }
        }

        // rename fromFile as toFile
        File fromFile = new File(fromFilePath);
        if (fromFile.exists()) {
            if (!fromFile.renameTo(toFile)) {
                System.out.println("Rename file to " + toFilePath + " failed.");
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

    public static ArrayList<File> getFileArrayList(String filePath, int order) {
        return new ArrayList<>(Arrays.asList(getFileList(filePath, order)));
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

    public static String getFileSizeFormat(String fileAbsolutePath) {
        return formatFileSize(FileUtils.getFileSize(fileAbsolutePath));
    }

    public static String getFileSizeFormat(File f) {
        return formatFileSize(FileUtils.getSingleFileSize(f));
    }

    /**
     * @param filePath: file name with full path
     * return: long, file size, unit by bytes
     */
    public static long getSingleFileSize(String filePath) {
        return getSingleFileSize(new File(filePath));
    }

    /**
     * @param file:
     * return: long, file size, unit by bytes
     */
    public static long getSingleFileSize(File file) {
        if (file == null) {
            System.out.println("ERROR: getSingleFileSize: file null");
            return -1;
        }
        if (!file.exists()) {
            System.out.println("ERROR: getSingleFileSize: file \"" + file.getAbsolutePath() + "\" not exist");
            return -2;
        }
        if (!file.isFile()) {
            System.out.println("ERROR: getSingleFileSize: \"" + file.getAbsolutePath() + "\" not a file");
            return -3;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            return raf.length();
        }
        catch(Exception e) {
            System.out.println("ERROR: getSingleFileSize: \"" + file.getAbsolutePath() + "\" failed with ex: " + e.toString());
            return -4;
        }
        finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception e) {
                    System.out.println("ERROR: getSingleFileSize: close \"" + file.getAbsolutePath() + "\" exception: " + e.toString());
                }
            }
        }
    }

    /**
     * @param filePath: file name with full path
     * return: long, file size, unit by bytes
     */
    public static long getFolderSize(String filePath) {
        return getFolderSize(new File(filePath));
    }

    public static long getFolderSize(File dir) {
        if (dir == null) {
            System.out.println("ERROR: getFolderSize: dir null");
            return -1;
        }
        if (!dir.exists()) {
            System.out.println("ERROR: getFolderSize: dir \"" + dir.getAbsolutePath() + "\" not exist");
            return -2;
        }
        if (!dir.isDirectory()) {
            System.out.println("ERROR: getFolderSize: \"" + dir.getAbsolutePath() + "\" not a dir");
            return -3;
        }
        long size = 0;
        File[] fileList = dir.listFiles();
        if (fileList != null) {
            for (File f : fileList) {
                long s = 0;
                if (f.isDirectory()) {
                    s += getFolderSize(f);
                } else {
                    s += getSingleFileSize(f);
                }
                if (s < 0) {
                    System.out.println("ERROR: getFolderSize: get size failed on file \"" + f.getAbsolutePath() + "\"");
                    return -4;
                }
                size += s;
            }
        }
        return size;
    }

    public static long getFileSize(String filePath) {
        return getFileSize(new File(filePath));
    }

    public static long getFileSize(File f) {
        if (f == null) {
            System.out.println("ERROR: getFileSize: file null");
            return -1;
        }
        if (!f.exists()) {
            System.out.println("ERROR: getFileSize: file \"" + f.getAbsolutePath() + "\" not exist");
            return -2;
        }
        try {
            long size;
            if (f.isDirectory()) {
                size = getFolderSize(f);
            } else {
                size = getSingleFileSize(f);
            }
            return size;
        } catch (Exception e) {
            System.out.println("ERROR: getFileSize: \"" + f.getAbsolutePath() + "\" exception: " + e.toString());
            return -3;
        }
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

    /**
     * An simple method to add headerBytes to source file by just copy to buffer
     *
     * @param srcFilePath: source file with full path
     * @param headerBytes:  buffer for headerBytes bytes
     *
     * NOTE:
     * Max File Size: 256M
     */
    public static boolean addHeaderSimple(String srcFilePath, byte[] headerBytes) {
        final long MAX_FILE_SIZE = 256 * 1024 * 1024L;
        RandomAccessFile srcFile = null;
        try {
            srcFile = new RandomAccessFile(srcFilePath, "rw");
            long srcSize = srcFile.length();
            if (srcSize > MAX_FILE_SIZE) {
                System.out.println("ERROR: add header to " + srcFilePath + " failed, file size " + srcSize + " exceed max " + MAX_FILE_SIZE);
                return false;
            }
            byte[] buf = new byte[(int) srcSize];
            srcFile.read(buf, 0, (int) srcSize);
            srcFile.seek(0);
            srcFile.write(headerBytes);
            srcFile.seek(headerBytes.length);
            srcFile.write(buf);
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: addHeaderSimple: add header to " + srcFilePath + " failed, ex = " + e.toString());
            return false;
        } finally {
            try {
                if (srcFile != null) {
                    srcFile.close();
                }
            }
            catch(Exception e) {
                System.out.println("ERROR: addHeaderSimple: close exception, ex = " + e.toString());
            }
        }
    }

    /**
     * Add headerBytes to source file
     *
     * @param srcFilePath: source file with full path
     * @param headerBytes:  buffer for headerBytes bytes
     *
     */
    public static boolean addHeader(String srcFilePath, byte[] headerBytes) {
        String dstFilePath = srcFilePath + ".dst";
        if (addHeader(srcFilePath, dstFilePath, headerBytes)) {
            mv(dstFilePath, srcFilePath);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Add headerBytes to source file, save to dstFilePath
     *
     * @param srcFilePath: source file with full path
     * @param dstFilePath: destination file with full path
     * @param headerBytes:  buffer for headerBytes bytes
     *
     */
    public static boolean addHeader(String srcFilePath, String dstFilePath, byte[] headerBytes) {
        final int DATA_BUF_SIZE = 2 * 1024 * 1024;
        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[DATA_BUF_SIZE];
        try {
            in = new FileInputStream(srcFilePath);
            out = new FileOutputStream(dstFilePath);
            out.write(headerBytes);
            int readLen;
            while ((readLen = in.read(data)) != -1) {
                out.write(data, 0, readLen);
            }
            in.close();
            in = null;
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: addHeader: add header to " + srcFilePath + " failed, ex = " + e.toString());
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                System.out.println("ERROR: addHeader: close exception, ex = " + e.toString());
            }
        }
    }

    /* ERROR: it not work for can't close file!
    public static void BigFileAddHead() {
        // 将282兆的文件内容头部添加一行字符  "This is a head!"
        String strHead = "This is a head!"; // 添加的头部内容
        String srcFilePath = "big_file"; // 原文件路径
        String destFilePath = "big_file_has_head"; // 添加头部后文件路径 （最终添加头部生成的文件路径）
        long startTime = System.currentTimeMillis();
        try {
            // 映射原文件到内存
            RandomAccessFile srcRandomAccessFile = new RandomAccessFile(srcFilePath, "r");
            FileChannel srcAccessFileChannel = srcRandomAccessFile.getChannel();
            long srcLength = srcAccessFileChannel.size();
            System.out.println("src file size:" + srcLength);  // src file size:296354010
            MappedByteBuffer srcMap = srcAccessFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, srcLength);

            // 映射目标文件到内存
            RandomAccessFile destRandomAccessFile = new RandomAccessFile(destFilePath, "rw");
            FileChannel destAccessFileChannel = destRandomAccessFile.getChannel();
            long destLength = srcLength + strHead.getBytes().length;
            System.out.println("dest file size:" + destLength);  // dest file size:296354025
            MappedByteBuffer destMap = destAccessFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, destLength);

            // 开始文件追加 : 先添加头部内容，再添加原来文件内容
            destMap.position(0);
            destMap.put(strHead.getBytes());
            destMap.put(srcMap);
            destAccessFileChannel.close();
            System.out.println("dest real file size:" + new RandomAccessFile(destFilePath, "r").getChannel().size());
            System.out.println("total time :" + (System.currentTimeMillis() - startTime));// 貌似时间不准确，异步操作？
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //*/

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

    public static boolean appendTxtFile(@NonNull String fileAbsolute, @NonNull String content) {
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
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: appendTxtFile: exception = " + e.toString());
            return false;
        }
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
        if (files != null) {
            for (File f : files) {
                String absolutePath = f.getAbsolutePath();
                if (f.isFile()) {
                    flag = deleteFile(absolutePath);
                } else if (f.isDirectory()) {
                    flag = deleteDirectory(f.getAbsolutePath());
                } else {
                    System.out.println("ERROR: FileUtils.deleteDirectory: " + absolutePath + " not file/dir");
                    flag = false;
                    break;
                }
                if (!flag) {
                    System.out.println("ERROR: FileUtils.deleteDirectory: delete " + absolutePath + " failed");
                    break;
                }
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

    public static int test_try_catch_finally() {
        try {
            System.out.println("try E...");
            File file = new File("d:\\temp\\aaa\\bbb");
            FileOutputStream out = new FileOutputStream(file);
            out.write("content".getBytes());
            out.flush();
            out.close();
            System.out.println("try X.");
            return 0;
        } catch (Exception e) {
            System.out.println("catch ex = " + e.toString());
            return -1;
        } finally {
            System.out.println("finally...");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST MAIN

    public static void main(String[] args) {

        System.out.println("\n***Test Begin...");
        //System.out.println("test_try_catch_finally=" + test_try_catch_finally());

        AssertUtils.Assert(replaceFileExtension("/home/liz/aaa.pcm", "wav").equals("/home/liz/aaa.wav"));
        AssertUtils.Assert(replaceFileExtension("/home/liz/aaa.", "wav").equals("/home/liz/aaa.wav"));
        AssertUtils.Assert(replaceFileExtension("/home/liz/aaa", "wav").equals("/home/liz/aaa.wav"));
        AssertUtils.Assert(replaceFileExtension("aaa.ccc", "wav").equals("aaa.wav"));
        AssertUtils.Assert(replaceFileExtension("aaa.c", "wav").equals("aaa.wav"));

        AssertUtils.Assert(getFilePathNeat("/home/liz/aaa.txt").equals("/home/liz/aaa"));
        AssertUtils.Assert(getFilePathNeat("/home/liz/aaa.trace").equals("/home/liz/aaa"));
        AssertUtils.Assert(getFilePathNeat("/home/liz/aaa.").equals("/home/liz/aaa"));
        AssertUtils.Assert(getFilePathNeat("/home/liz/aaa").equals("/home/liz/aaa"));
        AssertUtils.Assert(getFilePathNeat("aaa.txt").equals("aaa"));

        AssertUtils.Assert(writeTxtFile("D:\\Temp\\test.txt", "aaa\n"));
        AssertUtils.Assert(writeTxtFile("D:\\Temp\\test.txt", "bbb\n"));
        AssertUtils.Assert(writeTxtFile("D:\\Temp\\test.txt", "ccc\n", true, false));
        AssertUtils.Assert(appendTxtFile("D:\\Temp\\test.txt", "ddd\n"));
        AssertUtils.Assert(appendTxtFile("D:\\Temp\\test.txt", "eee\n"));

        AssertUtils.Assert(addHeaderSimple("D:\\Temp\\test.txt", "addHeaderSimple1\n".getBytes()));
        AssertUtils.Assert(addHeaderSimple("D:\\Temp\\test.txt", "addHeaderSimple2\n".getBytes()));
        AssertUtils.Assert(addHeaderSimple("D:\\Temp\\test.mp4", "addHeaderSimple\n".getBytes()));

        AssertUtils.Assert(addHeader("D:\\Temp\\test.txt", "addHeader1111\n".getBytes()));
        AssertUtils.Assert(addHeader("D:\\Temp\\test.txt", "addHeader2222\n".getBytes()));
        AssertUtils.Assert(addHeader("D:\\Temp\\test.mp4", "addHeader1111\n".getBytes()));
        AssertUtils.Assert(addHeader("D:\\Temp\\aaa.apk", "addHeader1111\n".getBytes()));
        AssertUtils.Assert(addHeader("D:\\Temp\\test.wav", "addHeader1111\n".getBytes()));

        //assert true
        AssertUtils.Assert(getFileExtension("/home/liz/aaa.txt").equals("txt"));
        AssertUtils.Assert(getFileExtension("/home/liz/aaa.trace").equals("trace"));
        AssertUtils.Assert(getFileExtension("/home/liz/aaa").equals(""));

        AssertUtils.Assert(formatDirSeparator("/home/liz/aaa").equals("/home/liz/aaa" + File.separator));
        AssertUtils.Assert(formatDirSeparator("/home/liz/aaa" + File.separator).equals("/home/liz/aaa" + File.separator));

        AssertUtils.Assert(getFileSize("D:\\Temp\\20.0310.184415.wav") == 385459244);
        AssertUtils.Assert(getFileSize("D:\\Media\\_cd\\midway2019.mkv") == 2923737914L);
        AssertUtils.Assert(getFileSize("D:\\Software\\android\\android-sdk\\platforms\\android-24\\data\\res\\values-ja\\_incljju\\a\\_jp\\1\\tjkadxn\\tjkadxn\\1.mp4") == 5199565620L);
        AssertUtils.Assert(getSingleFileSize("D:\\Temp\\20.0310.184415.wav") == 385459244);
        AssertUtils.Assert(getSingleFileSize("D:\\Media\\_cd\\midway2019.mkv") == 2923737914L);
        AssertUtils.Assert(getSingleFileSize("D:\\Software\\android\\android-sdk\\platforms\\android-24\\data\\res\\values-ja\\_incljju\\a\\_jp\\1\\tjkadxn\\tjkadxn\\1.mp4") == 5199565620L);

        //AssertUtils.Assert(getFileSize("D:\\Media") == 12274233649L);
        //AssertUtils.Assert(getFileSize("D:\\Temp") == 560574893L);

        /*
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
            String fileAbs = "D:\\home\\liz\\aaa.trace";  //for windows
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
            String fileAbs = "D:\\home\\liz\\aaa.trace";  //for windows
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

        System.out.println("***Test Successfully.");
    }
}
