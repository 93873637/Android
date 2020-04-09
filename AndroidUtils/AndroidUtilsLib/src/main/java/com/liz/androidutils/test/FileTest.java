package com.liz.androidutils.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileTest {
    public static void main(String[] args) throws IOException {
        //读写的数据源
        File f = new File("D:\\Temp\\RAF.txt");

        //指定文件不存在就创建同名文件
        if (!f.exists()) {
            f.createNewFile();
        }

        //rw : 设为读写模式
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        System.out.println("当前记录指针位置：" + raf.getFilePointer());

        //记录指针与文件内容长度相等
        raf.seek(raf.length());
        System.out.println("当前文件长度：" + raf.length());
        System.out.println("当前记录指针位置：" + raf.getFilePointer());

        //以字节形式写入字符串
        for (int i=0; i<3; i++) {
            raf.write("1234567890".getBytes());
            System.out.println("当前记录指针位置：" + raf.getFilePointer());
        }

        long fp = raf.getFilePointer();
        raf.seek(7);
        System.out.println("当前记录指针位置：" + raf.getFilePointer());
        raf.write("0000".getBytes());
        System.out.println("当前记录指针位置：" + raf.getFilePointer());

        raf.seek(fp);
        System.out.println("当前记录指针位置：" + raf.getFilePointer());
        raf.write("55555".getBytes(), 0, 3);

        raf.close();
    }
}
