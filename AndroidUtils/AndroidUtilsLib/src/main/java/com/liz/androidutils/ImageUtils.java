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
            System.out.println("ERROR: saveImage2JPGFile: bitmap null");
            return -1;
        }
        if (saveBitmap2JPGFile(bitmap, fileAbsolute) < 0) {
            System.out.println("ERROR: saveImage2JPGFile: save bitmap failed.");
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
        System.out.println("saveImage2JPGFile: image size = " + width + "x" + height);

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

    public static Bitmap Image2Bitmap(Image image) {
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        int width = image.getWidth();
        int height = image.getHeight();
        //System.out.println("image Size: " + width + "x" + height);
        //System.out.println("image Format: " + image.getFormat());

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //System.out.println("buffer position=" + buffer.position());
        //System.out.println("buffer limit=" + buffer.limit());
        //System.out.println("buffer capacity=" + buffer.capacity());

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        //System.out.println("pixelStride=" + pixelStride);
        //System.out.println("rowStride=" + rowStride);

        int rowPadding = rowStride - pixelStride * width;
        //System.out.println("rowPadding=" + rowPadding);

        ////////////////////////////////////////////////////////////////////
        //image Size: 1440 x 2392
        //bmp Size: 1472 x 2392
        //pixelStride = 4
        //rowStride = 5888 (64*23)
        //rowPadding = 128
        //buffer capacity = 14084096 = 5888*2392 = (1440 + 32) * 4 * 2392
        ////////////////////////////////////////////////////////////////////

        int bmpWidth = width + rowPadding / pixelStride;
        //System.out.println("bmp Size: " + bmpWidth + "x" + height);

        Bitmap bitmap = Bitmap.createBitmap(bmpWidth, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
        //return Bitmap.createScaledBitmap(bmpScreen, width/4, height/4, true);
    }
}
