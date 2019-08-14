package com.liz.androidutilstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.liz.androidutils.ComUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.SysUtils;
import com.liz.androidutils.FileUtils;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        test();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Test Functions

    public void test() {
        test_image();
        //test_SysUtils();
    }

    public void test_SysUtils() {
        LogUtils.d("test_SysUtils: appVersion = " + SysUtils.getAppVersion(MainActivity.this));
    }

    public void test_image1() {
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

    public void test_image() {
        String dataFileName = "/sdcard/data.txt";

        String imageFileName = dataFileName + ".jpg";
        String imageFileName2 = dataFileName + "2.jpg";
        String imageFileName3 = dataFileName + "3.jpg";

        FileUtils.removeFile(imageFileName);
        FileUtils.removeFile(imageFileName2);
        FileUtils.removeFile(imageFileName3);

        ByteBuffer byteBuffer = ComUtils.readByteBufferFromFile(dataFileName);
        if (byteBuffer == null) {
            System.out.println("read failed");
            return;
        }
        else {
            System.out.println("read ok, buffer size = " + byteBuffer.capacity());
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

        ImageUtils.saveByteBuffer2JPGFile(byteBuffer, imageFileName, imageWidth, imageHeight, pixelPadding);


        int scale = 4;
        int imageHeight2 = totalHeight / scale;
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(rowStride * imageHeight2);
        byteBuffer.flip();
        byteBuffer2.flip();
        LogUtils.d("###@: imageHeight2=" + imageHeight2);
        LogUtils.d("###@: bb.position/limit/capacity=" + byteBuffer.position() + "/" + byteBuffer.limit() + "/" + byteBuffer.capacity()
                + ": byteBuffer2.position/limit/capacity=" + byteBuffer2.position() + "/" + byteBuffer2.limit() + "/" + byteBuffer2.capacity());

        for (int i = 0; i < imageHeight2; i ++) {
            byteBuffer.limit((i * scale + 1) * rowStride);
            byteBuffer.position(i * scale * rowStride);
            byteBuffer2.limit((i + 1) * rowStride);
            byteBuffer2.position(i * rowStride);
            //LogUtils.d("###@: i=" + i + ": bb.position/limit/capacity=" + bb.position() + "/" + bb.limit() + "/" + bb.capacity()
            //        + ": byteBuffer2.position/limit/capacity=" + byteBuffer2.position() + "/" + byteBuffer2.limit() + "/" + byteBuffer2.capacity());
            byteBuffer2.put(byteBuffer.slice());
        }

        ImageUtils.saveByteBuffer2JPGFile(byteBuffer2, imageFileName2, imageWidth, imageHeight2, pixelPadding);

        int imageWidth2 = imageWidth / scale;
        int imageBytes = imageWidth2 * imageHeight2 * (pixelStride);
        LogUtils.d("###@: imageWidth2=" + imageWidth2);
        LogUtils.d("###@: imageHeight2=" + imageHeight2);
        LogUtils.d("###@: imageBytes=" + imageBytes);

        byte[] imageArr = new byte[imageBytes];
        int offset;
        int offset2;
        for (int i = 0; i < imageHeight2; i ++) {
            offset = i * rowStride;
            byteBuffer2.limit(offset + rowStride);
            byteBuffer2.position(offset);
            byte[] arr = ComUtils.byteBuf2Arr(byteBuffer2.slice());
            LogUtils.d("###@: arr=" + arr.length);
            offset2 = i * imageWidth2 * pixelStride;
            LogUtils.d("###@: offset2=" + offset2);
            for (int j = 0; j < imageWidth2; j++) {
                int imgOff = offset2 + j * 4;
                int arrOff = j*4*4;
                imageArr[imgOff] = arr[arrOff];
                imageArr[imgOff+1] = arr[arrOff+1];
                imageArr[imgOff+2] = arr[arrOff+2];
                imageArr[imgOff+3] = arr[arrOff+3];
            }
        }

        ByteBuffer byteBuffer3 = ComUtils.byteArr2Buf(imageArr);
        LogUtils.d("###@: byteBuffer3.position/limit/capacity=" + byteBuffer3.position() + "/" + byteBuffer3.limit() + "/" + byteBuffer3.capacity());
        ImageUtils.saveByteBuffer2JPGFile(byteBuffer3, imageFileName3, imageWidth2, imageHeight2, 0);
    }
}
