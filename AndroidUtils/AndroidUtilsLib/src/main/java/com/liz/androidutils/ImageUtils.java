package com.liz.androidutils;

import android.graphics.Bitmap;
import android.media.Image;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;


@SuppressWarnings("unused")
public class ImageUtils {

    public static int saveBitmap2JPGFile(@NonNull Bitmap bitmap, String fileAbsolute) {
        try {
            File f = new File(fileAbsolute);
            if (f.exists()) {
                System.out.println("ERROR: saveBitmap2JPGFile: file " + fileAbsolute + " already exist.");
                return -1;
            }
            if (!f.createNewFile()) {
                System.out.println("ERROR: saveBitmap2JPGFile: create file " + fileAbsolute + " failed.");
                return -2;
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("ERROR: saveBitmap2JPGFile: exception=" + e.toString());
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    //
    //NOTE: this function can only run on android
    //
    public static int saveByteBuffer2JPGFile(@NonNull ByteBuffer byteBuffer, String fileAbsolute, int width, int height, int padding) {
        Bitmap bitmap = Bitmap.createBitmap(width + padding, height, Bitmap.Config.ARGB_8888);
        //byteBuffer.flip();
        byteBuffer.rewind();
        bitmap.copyPixelsFromBuffer(byteBuffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        if (bitmap == null) {
            System.out.println("ERROR: saveImage: bitmap null");
            return -1;
        }
        if (saveBitmap2JPGFile(bitmap, fileAbsolute) < 0) {
            System.out.println("ERROR: saveImage: save bitmap failed.");
            return -2;
        }
        return 0;
    }

    public static int saveImage(Image image, String fileAbsolute) {
        System.out.println("saveImage: fileAbsolute = " + fileAbsolute + "...");

        File filePath = new File(FileUtils.getFilePath(fileAbsolute));
        if (!filePath.exists()) {
            System.out.println("saveImage: create image path: " + filePath);
            if (!filePath.mkdirs()) {
                System.out.println("ERROR: saveImage: create image path \"" + filePath + "\" failed.");
                return -1;
            }
        }

        int width = image.getWidth();
        int height = image.getHeight();
        System.out.println("saveImage: image size = " + width + "x" + height);

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        int paddingWidth = rowPadding / pixelStride;

        if (saveByteBuffer2JPGFile(buffer, fileAbsolute, width, height, paddingWidth) < 0) {
            System.out.println("ERROR: saveImage: save bitmap failed.");
            return -2;
        }

        return 0;
    }
}
