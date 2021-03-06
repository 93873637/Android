package com.liz.imagetools.utils;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.liz.imagetools.image.BMP;
import com.liz.imagetools.image.NV21;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by admin on 2018/8/15.
 */

public class ImageUtils {
    public static final String SUCCESS = "SUCCESS";

    public static void NV21toBMP(String fileName, int width, int height) {
        File file = new File(fileName); // The input NV21 file
        if (!file.exists())
            return;

        // BMP file info
        short pixelBits = 32;

        try {
            // Read all bytes
            byte[] bytes = FileUtils.readFile(file); //Files.readAllBytes(file.toPath());

            int[] data = NV21.yuv2rgb(bytes, width, height);
            BMP bmp = new BMP(width, height, pixelBits, data);
            bmp.saveBMP(fileName + ".bmp"); // The output BMP file

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(this, "Exception: " + e.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        System.out.println("Conversion is done.");
        //Toast.makeText(this, "Conversion is done", Toast.LENGTH_LONG).show();
    }

    public static String NV21toJPG(File nv21File, int width, int height, String outPath, int quality) {
        LogUtils.d("NV21toJPG: Enter...");
        if (!nv21File.exists()) {
            LogUtils.d("NV21toJPG: nv21File not exist.");
            return "ERR_NO_NV21_FILE";
        }

        File outFolder = new File(outPath);
        if (!outFolder.exists()) {
            LogUtils.d("NV21toJPG: outFolder \"" + outPath + "\" not exist.");
            return "ERR_NO_OUT_FOLDER: " + outPath;
        }

        File jpgFile = new File(outPath + "/" + nv21File.getName() + ".jpg");
        try {
            jpgFile.createNewFile();
            FileOutputStream filecon = new FileOutputStream(jpgFile);
            byte[] data = FileUtils.readFile(nv21File);
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
            yuvImage.compressToJpeg(
                    new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()),
                    quality, filecon);
        } catch (IOException e) {
            LogUtils.d("NV21toJPG: Exception: " + e.toString());
            return "ERR_EXCEPTION: " + e.toString();
        }

        return SUCCESS;
    }
}
