package com.liz.androidutils;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

@SuppressWarnings("unused, WeakerAccess")
public class NumUtils {

    public static final long K = 1024L;
    public static final long M = K * K;
    public static final long G = K * M;
    public static final long T = M * M;

    public static String formatShow(int n) {
        DecimalFormat df =  new DecimalFormat("###,##0");
        return df.format(n);
    }

    /**
     * a function as printf("%05d", value)
     * @param v: value
     * @param n: zero number
     * @return zero padding string, such as
     *   zeroPadding(6, 3) = "006"
     *   zeroPadding(56, 3) = "056"
     *   zeroPadding(156, 3) = "156"
     *   zeroPadding(2156, 3) = "2156"
     */
    public static String zeroPadding(int v, int n) {
        String vs = "" + v;
        int zeroNum = n - vs.length();
        if (zeroNum > 0) {
            for (int i=0; i<zeroNum; i++) vs = "0" + vs;
        }
        return vs;
    }

    public static String formatSize(long size) {
        final long K_V = 1024;  //real value
        final long K_C = 1000;  //compare value, less than 4 digits
        final long M_V = K_V * K_V;
        final long M_C = K_C * K_C;
        final long G_V = M_V * K_V;
        final long G_C = M_C * K_C;

        String sizeString;
        DecimalFormat df = new DecimalFormat("#.0");

        long absSize = Math.abs(size);
        if (absSize == 0) {
            sizeString = "0";
        }
        else if (absSize < K_C) {
            sizeString = df.format((double) absSize);
        }
        else if (absSize < M_C) {
            sizeString = df.format((double) absSize / K_V) + "K";
        }
        else if (absSize < G_C) {
            sizeString = df.format((double) absSize / M_V)+ "M";
        }
        else {
            sizeString = df.format((double) absSize / G_V) + "G";
        }

        if (size < 0) {
            sizeString = "-" + sizeString;
        }
        return sizeString;
    }

    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        byteNum[0] = (byte)((num >> 24) & 0xff);
        byteNum[1] = (byte)((num >> 16) & 0xff);
        byteNum[2] = (byte)((num >> 8) & 0xff);
        byteNum[3] = (byte)(num & 0xff);
        return byteNum;
    }

    /**
     * little endian
     */
    public static int bytes2Int(byte[] byteNum) {
        return bytes2Int(byteNum, 0);
    }

    /**
     * little endian
     */
    public static int bytes2Int(byte[] byteNum, int offset) {
        int num = 0;
        for (int ix = 0; ix < 4; ++ix) {
            num <<= 8;
            num |= (byteNum[offset + ix] & 0xff);
        }
        return num;
    }

    /**
     * big endian
     */
    public static short bytes2ShortB(byte[] byteNum) {
        return bytes2ShortB(byteNum, 0);
    }

    /**
     * big endian
     */
    public static short bytes2ShortB(byte[] byteNum, int offset) {
        short num = 0;
        for (int ix = 1; ix >=0; --ix) {
            num <<= 8;
            num |= (byteNum[offset + ix] & 0xff);
        }
        return num;
    }

    /**
     * big endian
     */
    public static int bytes2IntB(byte[] byteNum) {
        return bytes2IntB(byteNum, 0);
    }

    /**
     * big endian
     */
    public static int bytes2IntB(byte[] byteNum, int offset) {
        int num = 0;
        for (int ix = 3; ix >=0; --ix) {
            num <<= 8;
            num |= (byteNum[offset + ix] & 0xff);
        }
        return num;
    }

    public static String byte2Hex(byte b){
        String s = Integer.toHexString(b & 0xFF);
        return (s.length() == 1) ? ("0" + s) : s;
    }

    public static String bytes2Hex(byte[] bytes){
        String s;
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            s = Integer.toHexString(b & 0xFF);
            sb.append((s.length() == 1) ? "0" + s : s);
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static void printBytes(@NonNull byte[] bytes) {
        System.out.println(bytes2Hex(bytes));
    }

    public static byte int2OneByte(int num) {
        return (byte) (num & 0x000000ff);
    }

    public static int oneByte2Int(byte byteNum) {
        return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
    }

    public static byte[] long2Bytes(long num) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; ++i) {
            int offset = 64 - (i + 1) * 8;
            bytes[i] = (byte) ((num >> offset) & 0xff);
        }
        return bytes;
    }

    public static long bytes2Long(byte[] bytes) {
        long num = 0;
        for (byte b : bytes) {
            num <<= 8;
            num |= (b & 0xff);
        }
        return num;
    }

    /**
     * java only has signed data type, here we can get unsigned int value by bit operations
     */
    public static int unsigned(byte data){
        return data&0x0ff;
    }

    public static int unsigned(short data){
        return data&0x0ffff;
    }

    public static long unsigned(int data){
        return bytes2Long(int2Bytes(data));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST MAIN

    public static void main(String[] args) {

        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String tag = s[1].getClassName() + " " + s[1].getFileName() + "/" + s[1].getLineNumber();
        System.out.println("\n" + tag + ": Test Start...");

        AssertUtils.Assert(zeroPadding(0, 3).equals("000"));
        AssertUtils.Assert(zeroPadding(1, 3).equals("001"));
        AssertUtils.Assert(zeroPadding(10, 3).equals("010"));
        AssertUtils.Assert(zeroPadding(100, 3).equals("100"));
        AssertUtils.Assert(zeroPadding(123, 3).equals("123"));
        AssertUtils.Assert(zeroPadding(1234, 3).equals("1234"));

        AssertUtils.Assert(byte2Hex((byte)128).equals("80"));
        AssertUtils.Assert(byte2Hex((byte)160).equals("a0"));
        AssertUtils.Assert(unsigned((byte)-128) == 128);
        AssertUtils.Assert(unsigned((short)-10) == 65526);
        AssertUtils.Assert(unsigned(-1) == 4294967295L);

        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x00, (byte)0x00}) == 0);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x01, (byte)0x00}) == 1);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x02, (byte)0x00}) == 2);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x10, (byte)0x00}) == 16);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x00, (byte)0x01}) == 256);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x00, (byte)0x10}) == 4096);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0x00, (byte)0x80}) == -32768);
        AssertUtils.Assert(bytes2ShortB(new byte[]{(byte)0xff, (byte)0xff}) == -1);

//        byte[] bi = int2Bytes(-1);
//        printBytes(bi);

        //System.out.println("a = 0x" + Integer.toHexString(0x10000001));
        //System.out.println("a = 0x" + Integer.toHexString(0x8001));
        //printBytes(int2Bytes(65536));
//        test_formatShow(456);
//        test_formatShow(1456);
//        test_formatShow(13456);
//        test_formatShow(123456);

        System.out.println("\n" + tag + ": Test Successfully.");
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
