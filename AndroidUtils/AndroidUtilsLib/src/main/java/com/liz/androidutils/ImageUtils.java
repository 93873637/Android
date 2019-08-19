package com.liz.androidutils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;


@SuppressWarnings("unused, WeakerAccess")
public class ImageUtils {

    public static byte[] bitmap2Bytes(Bitmap bmp, Bitmap.CompressFormat format, int quality){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(format, quality, baos);
        return baos.toByteArray();
    }

    public static byte[] pixelArr2JPGArr(byte[] pixelArr, int width, int height, int quality) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixelArr));
        return ImageUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.JPEG, quality);
    }

    public static Bitmap byteBuffer2Bitmap(@NonNull ByteBuffer byteBuffer, int width, int height, int padding) {
        Bitmap bitmap = Bitmap.createBitmap(width+ padding, height, Bitmap.Config.ARGB_8888);
        byteBuffer.rewind();
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    public static Bitmap byteBuffer2Bitmap(@NonNull ByteBuffer byteBuffer, int width, int height,
                                           int padding, @NonNull Bitmap.Config config) {
        Bitmap bitmap = Bitmap.createBitmap(width+ padding, height, config);
        byteBuffer.rewind();
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    public static Bitmap byteBuffer2Bitmap(@NonNull ByteBuffer byteBuffer, int width, int height,
                                           int padding, @NonNull Bitmap.Config config, double scale) {
        Bitmap bitmap = Bitmap.createBitmap(width+ padding, height, config);
        byteBuffer.rewind();
        bitmap.copyPixelsFromBuffer(byteBuffer);
        Bitmap bmp2 = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        return Bitmap.createScaledBitmap(bmp2, (int)(width*scale), (int)(height*scale), true);
    }

    public static byte[] byteBuffer2PixelArray(@NonNull ByteBuffer byteBuffer, int width, int height,
                                          int padding, int pixelStride, int scale) {
        int rowStride = (width + padding) * pixelStride;
        int scaleHeight = height / scale;
        int scareWidth = width / scale;
        byte[] pixelArr = new byte[scareWidth * scaleHeight * pixelStride];
        byte[] rowArr = new byte[rowStride];
        int i,j;
        for (i=0; i<scaleHeight; i++) {
            byteBuffer.limit((i * scale + 1) * rowStride);
            byteBuffer.position(i * scale * rowStride);
            byteBuffer.slice().get(rowArr, 0, rowStride);
            for (j = 0; j < scareWidth; j++) {
                System.arraycopy(rowArr, j * scale * pixelStride, pixelArr,
                        i * scareWidth * pixelStride + j * pixelStride, pixelStride);
            }
        }
        return pixelArr;
    }

    public static byte[] byteBuffer2JPGArray(@NonNull ByteBuffer byteBuffer, int width, int height,
                                             int padding, int pixelStride, int scale, int quality) {
        byte[] pixelArr = byteBuffer2PixelArray(byteBuffer, width, height, padding, pixelStride, scale);
        return pixelArr2JPGArr(pixelArr, width/scale, height/scale, quality);
    }

    /**
     * For A2H, general parameters' values are:
     * image Size: 1440 x 2392
     * bmp Size: 1472 x 2392
     * pixelStride = 4
     * rowStride = 5888 (64*23)
     * rowPadding = 128
     * pixelPadding = 32
     * buffer capacity = 14084096 = 5888*2392 = (1440 + 32) * 4 * 2392
    */
    public static Bitmap image2Bitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer byteBuffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        int pixelPadding = rowPadding / pixelStride;
        return byteBuffer2Bitmap(byteBuffer, width, height, pixelPadding, Bitmap.Config.ARGB_8888, 1);
    }

    public static int saveBitmap2JPGFile(@NonNull Bitmap bitmap, String fileAbsolute, int quality) {
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("ERROR: saveBitmap2JPGFile: exception=" + e.toString());
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    public static int saveJPGArray2File(@NonNull byte[] jpgArr, String fileAbsolute, int quality) {
        Bitmap bmp = BitmapFactory.decodeByteArray(jpgArr, 0, jpgArr.length);
        if (bmp == null) {
            System.out.println("ERROR: saveJPGArray2File: decode byte array for bmp failed.");
            return -1;
        }
        return saveBitmap2JPGFile(bmp, fileAbsolute, quality);
    }

    //
    //NOTE: this function can only run on android
    //
    public static int saveByteBuffer2JPGFile(@NonNull ByteBuffer byteBuffer, @NonNull String fileAbsolute, int width, int height, int padding) {
        Bitmap bitmap = byteBuffer2Bitmap(byteBuffer, width, height, padding, Bitmap.Config.ARGB_8888, 1.0);
        if (bitmap == null) {
            System.out.println("ERROR: saveByteBuffer2JPGFile: convert byte buffer to bitmap null");
            return -1;
        }
        if (saveBitmap2JPGFile(bitmap, fileAbsolute, 100) < 0) {
            System.out.println("ERROR: saveByteBuffer2JPGFile: save bitmap failed.");
            return -2;
        }
        return 0;
    }

    public static int saveImage2JPGFile(Image image, String fileAbsolute) {
        System.out.println("saveImage2JPGFile: fileAbsolute = " + fileAbsolute + "...");

        File filePath = new File(FileUtils.getFilePath(fileAbsolute));
        if (!filePath.exists()) {
            System.out.println("saveImage2JPGFile: create image path: " + filePath);
            if (!filePath.mkdirs()) {
                System.out.println("ERROR: saveImage2JPGFile: create image path \"" + filePath + "\" failed.");
                return -1;
            }
        }

        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        int paddingWidth = rowPadding / pixelStride;

        if (saveByteBuffer2JPGFile(buffer, fileAbsolute, width, height, paddingWidth) < 0) {
            System.out.println("ERROR: saveImage2JPGFile: save bitmap to file \"" + fileAbsolute + "\" failed.");
            return -2;
        }

        return 0;
    }

    public static int saveImageData2File(Image image, String fileAbsolute) {
        System.out.println("saveImageData2File: fileAbsolute = " + fileAbsolute + "...");

        //ensure file&path exist
        File filePath = new File(FileUtils.getFilePath(fileAbsolute));
        if (!filePath.exists()) {
            System.out.println("saveImageData2File: create image path: " + filePath);
            if (!filePath.mkdirs()) {
                System.out.println("ERROR: saveImageData2File: create image path \"" + filePath + "\" failed.");
                return -1;
            }
        }

        //save image buffer
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer byteBuffer = planes[0].getBuffer();
        byteBuffer.rewind();
        System.out.println("saveImageData2File: byteBuffer= " + byteBuffer.position() + "/" + byteBuffer.limit() + "/" + byteBuffer.capacity());
        if (!ComUtils.saveByteBufferToFile(byteBuffer, fileAbsolute)) {
            System.out.println("ERROR: saveImageData2File: save byte buffer to file \"" + fileAbsolute + "\" failed.");
            return -2;
        }

        return 0;
    }
}
