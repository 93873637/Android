package com.liz.screenhelper.utils;

import android.graphics.Bitmap;
import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;


public class ImageUtils {

    public static int saveImage(Image image, String fileAbsolute) {
        LogUtils.d("saveImage: fileAbsolute = " + fileAbsolute + "...");

        File filePath = new File(FileUtils.getFilePath(fileAbsolute));
        if (!filePath.exists()) {
            LogUtils.i("saveImage: create image path: " + filePath);
            if (!filePath.mkdirs()) {
                LogUtils.e("ERROR: saveImage: create image path \"" + filePath + "\" failed.");
                return -1;
            }
        }

        int width = image.getWidth();
        int height = image.getHeight();
        LogUtils.d("saveImage: image size = " + width + "x" + height);

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

        if (bitmap == null) {
            LogUtils.d("ERROR: saveImage: bitmap null");
            return -2;
        }

        try {
            File fileImage = new File(fileAbsolute);
            if (!fileImage.exists()) {
                LogUtils.i("saveImage: create image file: " + fileAbsolute);
                fileImage.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(fileImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  //using JPEG to fast save
            out.flush();
            out.close();
            LogUtils.i("saveImage: screen image saved to " + fileAbsolute);
        } catch (Exception e) {
            LogUtils.d("ERROR: saveImage: save to file failed, exception=" + e.toString());
            e.printStackTrace();
        }

        LogUtils.d("saveImage: exit.");
        return 0;
    }
}
