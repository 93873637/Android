package com.liz.androidutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@SuppressWarnings("unused")
public class ComUtils {

    public static ByteBuffer byteArr2Buf(byte[] arr) {
        return ByteBuffer.wrap(arr);
    }

    public static byte[] byteBuf2Arr(ByteBuffer buf) {
        int len = buf.capacity();
        byte[] arr = new byte[len];
        buf.get(arr, 0, arr.length);
        return arr;
    }

    public static boolean saveByteBufferToFile(ByteBuffer bb, String fileAbsolute) {
        try {
            FileChannel fc = new FileOutputStream(fileAbsolute).getChannel();
            fc.write(bb);
            fc.close();
        } catch (Exception e) {
            System.out.println("ERROR: save ByteBuffer to file exception: " + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static ByteBuffer readByteBufferFromFile(String fileAbsolute) {
        ByteBuffer byteBuffer = null;
        FileInputStream fis = null;
        FileChannel fc = null;
        try {
            File f = new File(fileAbsolute);
            if (!f.exists()) {
                System.out.println("ERROR: read ByteBuffer from un-exist file " + fileAbsolute);
                return null;
            }
            fis = new FileInputStream(f);
            fc = fis.getChannel();
            byteBuffer = ByteBuffer.allocate((int) fc.size());
            while (fc.read(byteBuffer) > 0) {
                // do nothing
                // System.out.println("reading");
            }
        } catch (Exception e) {
            System.out.println("ERROR: read ByteBuffer from file exception: " + e.toString());
            e.printStackTrace();
        }

        try {
            if (fc != null) {
                fc.close();
            }
        } catch (IOException e) {
            System.out.println("ERROR: close file channel exception: " + e.toString());
            e.printStackTrace();
        }
        try {
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e) {
            System.out.println("ERROR: close file channel exception: " + e.toString());
            e.printStackTrace();
        }

        return byteBuffer;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {

    }

    //NOTE: this function can only run on android
    public void test_save() {
        //String dataFileName = "D:/Temp/data.txt";
        String dataFileName = "/sdcard/data.txt";
        String imageFileName = dataFileName + ".jpg";

        ByteBuffer bb = ComUtils.readByteBufferFromFile(dataFileName);
        if (bb == null) {
            System.out.println("read failed");
            return;
        }
        else {
            System.out.println("read ok, buffer size = " + bb.capacity());
        }

        //RGBA_8888

        ////////////////////////////////////////////////////////////////////
        //image Size: 1440 x 2392
        //total Size: 1472 x 2392
        //pixelStride = 4
        //rowStride = 5888 (64*23)
        //rowPadding = 128
        //buffer capacity = 14084096 = 5888*2392 = (1440 + 32) * 4 * 2392
        ////////////////////////////////////////////////////////////////////

        int imageWidth = 1440;
        int imageHeight = 2392;

        int pixelStride = 4;
        int rowPadding = 128;

        int pixelPadding = rowPadding / pixelStride;  //32

        int totalWidth = imageWidth + pixelPadding;  //1472
        int totalHeight = imageHeight;

        int rowStride = totalWidth * pixelStride;  //5888

        //bb.flip();
        ImageUtils.saveByteBuffer2JPGFile(bb, imageFileName, imageWidth, imageHeight, pixelPadding);
    }

    public static void test_buffer_convert() {
        byte[] bytes = {1,2,3,4,5,6,7,8,9,10};
        LogUtils.printBytes(bytes);
        System.out.println();

        //ByteBuffer bb = ByteBuffer.wrap(bytes);
        ByteBuffer bb = byteArr2Buf(bytes);
        System.out.println(bb.toString());

        byte[] bytes2 = byteBuf2Arr(bb);
        LogUtils.printBytes(bytes2);
    }
}
