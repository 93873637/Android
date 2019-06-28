package com.liz.imagetools.utils;

import com.liz.imagetools.logic.ComDef;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * FileUtils:
 * Created by liz on 2018/8/15.
 */

@SuppressWarnings("WeakerAccess, unused")
public class FileUtils {

    public static int getFileNumber(String filePath, String extName) {
        File[] files = new File(filePath).listFiles();
        if (files == null) {
            LogUtils.e("ERROR: files null, please check if path " + filePath + " exist?");
            return -1;
        }

        int fileNum = 0;
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (extName == null) {
                    fileNum ++;
                }
                else {
                    int pos = file.getName().lastIndexOf(extName);
                    int posSuffix = fileName.length() - extName.length();
                    if (pos >= 0 && posSuffix >= 0 && pos == posSuffix) {
                        fileNum ++;
                    }
                }
            } else {
                LogUtils.i("skip dir");
            }
        }

        return fileNum;
    }

    public static boolean listFiles(String filePath, String extName, List<File> list) {
        if (list == null) {
            LogUtils.e("ERROR: can't work with null list");
            return false;
        }

        list.clear();

        File[] files = new File(filePath).listFiles();
        if (files == null) {
            LogUtils.e("ERROR: files null, please check if path " + filePath + " exist?");
            return false;
        }

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (extName == null) {
                    list.add(file);
                }
                else {
                    int pos = file.getName().lastIndexOf(extName);
                    int posSuffix = fileName.length() - extName.length();
                    if (pos >= 0 && posSuffix >= 0 && pos == posSuffix) {
                        list.add(file);
                    }
                }
            } else {
                LogUtils.i("skip dir");
            }
        }

        return true;
    }

    public static byte[] readFile(File file) {
        // 需要读取的文件，参数是文件的路径名加文件名
        if (file.isFile()) {
            // 以字节流方法读取文件

            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                // 设置一个，每次 装载信息的容器
                byte[] buffer = new byte[8192];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // 开始读取数据
                int len;// 每次读取到的数据的长度
                while ((len = fis.read(buffer)) != -1) {// len值为-1时，表示没有数据了
                    // append方法往sb对象里面添加数据
                    outputStream.write(buffer, 0, len);
                }
                // 输出字符串
                return outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }
        return null;
    }

    public static boolean delete(String fileAbsolute) {
        File file = new File(fileAbsolute);
        if (!file.exists()) {
            return false;
        }
        else {
            if (file.isFile())
                return deleteSingleFile(fileAbsolute);
            else
                return deleteDirectory(fileAbsolute, true);
        }
    }

    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        if (file.exists() && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }

    public static boolean clearDirectory(String filePath) {
        return deleteDirectory(filePath, false);
    }

    public static boolean deleteDirectory(String filePath, boolean includeSelf) {
        //add separator if not
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }

        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            LogUtils.e("ERROR: deleteDirectory: dir \"" + filePath + "\" not exist");
            return false;
        }
        if (!dirFile.isDirectory()) {
            LogUtils.e("ERROR: deleteDirectory: \"" + filePath + "\" not a directory");
            return false;
        }

        boolean flag = true;

        File[] files = dirFile.listFiles();
        if (files == null) {
            LogUtils.e("ERROR: deleteDirectory: \"" + filePath + "\" list null");
            return false;
        }

        for (File file : files) {
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag) {
                    LogUtils.e("ERROR: deleteDirectory: deleteSingleFile \"" + file.getAbsolutePath() + "\" failed");
                    break;
                }
            }
            else if (file.isDirectory()) {
                flag = deleteDirectory(file.getAbsolutePath(), true);
                if (!flag) {
                    LogUtils.e("ERROR: deleteDirectory: deleteDirectory \"" + file.getAbsolutePath() + "\" failed");
                    break;
                }
            }
            else {
                LogUtils.w("WARNING: deleteDirectory: delete with unsupport file type either file or dir");
            }
        }

        if (!flag) {
            LogUtils.e("ERROR: deleteDirectory: failed on progress");
            return false;
        }

        if (includeSelf) {
            if (dirFile.delete()) {
                return true;
            } else {
                LogUtils.e("ERROR: deleteDirectory: delete self \"" + filePath + "\" failed");
                return false;
            }
        }

        return true;
    }
}
