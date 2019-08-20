package com.liz.androidutils;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

@SuppressWarnings("unused, WeakerAccess")
public class NumUtils {

    public static String formatShow(int n) {
        DecimalFormat df =  new DecimalFormat("###,##0");
        return df.format(n);
    }

    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        byteNum[0] = (byte)((num >> 24) & 0xff);
        byteNum[1] = (byte)((num >> 16) & 0xff);
        byteNum[2] = (byte)((num >> 8) & 0xff);
        byteNum[3] = (byte)(num & 0xff);
        return byteNum;
    }

    public static int bytes2Int(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < 4; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static void printBytes(@NonNull byte[] bytes) {
        for (int i=0; i<bytes.length; i++) {
            System.out.println("bytes["+i+"]=" + (int)bytes[i]);
        }
    }

    public static byte int2OneByte(int num) {
        return (byte) (num & 0x000000ff);
    }

    public static int oneByte2Int(byte byteNum) {
        return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        printBytes(int2Bytes(65536));

//        test_formatShow(456);
//        test_formatShow(1456);
//        test_formatShow(13456);
//        test_formatShow(123456);
    }

    public static void test_formatShow(int n) {
        System.out.println("formatShow(" + n + ") = " + formatShow(n));
    }

    public static void test_number() {
        int num = 129;
        System.out.println("测试的int值为:" + num);

        byte[] int2bytes = int2Bytes(num);
        System.out.print("int转成bytes: ");
        for (int i = 0; i < 4; ++i) {
            System.out.print(int2bytes[i] + " ");
        }
        System.out.println();

        int bytes2int = bytes2Int(int2bytes);
        System.out.println("bytes转行成int: " + bytes2int);

        byte int2OneByte = int2OneByte(num);
        System.out.println("int转行成one byte: " + int2OneByte);

        int oneByte2Int = oneByte2Int(int2OneByte);
        System.out.println("one byte转行成int: " + oneByte2Int);
        System.out.println();

        long longNum = 100000;
        System.out.println("测试的long值为：" + longNum);

//        byte[] long2Bytes = long2Bytes(longNum);
//        System.out.print("long转行成bytes: ");
//        for (int ix = 0; ix < long2Bytes.length; ++ix) {
//            System.out.print(long2Bytes[ix] + " ");
//        }
//        System.out.println();
//
//        long bytes2Long = bytes2Long(long2Bytes);
//        System.out.println("bytes转行成long: " + bytes2Long);
    }
}
