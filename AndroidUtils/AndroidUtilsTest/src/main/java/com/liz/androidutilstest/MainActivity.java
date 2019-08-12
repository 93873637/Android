package com.liz.androidutilstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.liz.androidutils.ComUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;

import java.io.File;
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

    public void ddeleteFile(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            System.out.println("image file " + fileName + " already exist, delete it");
            f.delete();
        }
    }

    public void test() {
        String dataFileName = "/sdcard/data.txt";

        String imageFileName = dataFileName + ".jpg";
        String imageFileName2 = dataFileName + "2.jpg";
        String imageFileName3 = dataFileName + "3.jpg";

        ddeleteFile(imageFileName);
        ddeleteFile(imageFileName2);
        ddeleteFile(imageFileName3);

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

        ImageUtils.saveByteBuffer2JPGFile(bb, imageFileName, imageWidth, imageHeight, pixelPadding);


        int scale = 4;
        int imageHeight2 = totalHeight / scale;
        ByteBuffer bb2 = ByteBuffer.allocate(rowStride * imageHeight2);
        bb.flip();
        bb2.flip();
        LogUtils.d("###@: imageHeight2=" + imageHeight2);
        LogUtils.d("###@: bb.position/limit/capacity=" + bb.position() + "/" + bb.limit() + "/" + bb.capacity()
                + ": bb2.position/limit/capacity=" + bb2.position() + "/" + bb2.limit() + "/" + bb2.capacity());

        for (int i = 0; i < imageHeight2; i ++) {
            bb.limit((i * scale + 1) * rowStride);
            bb.position(i * scale * rowStride);
            bb2.limit((i + 1) * rowStride);
            bb2.position(i * rowStride);
            //LogUtils.d("###@: i=" + i + ": bb.position/limit/capacity=" + bb.position() + "/" + bb.limit() + "/" + bb.capacity()
            //        + ": bb2.position/limit/capacity=" + bb2.position() + "/" + bb2.limit() + "/" + bb2.capacity());
            bb2.put(bb.slice());
        }

        ImageUtils.saveByteBuffer2JPGFile(bb2, imageFileName2, imageWidth, imageHeight2, pixelPadding);

        int imageWidth2 = imageWidth / scale;
        int imageBytes = imageWidth2 * imageHeight2 * (pixelStride);
        LogUtils.d("###@: imageWidth2=" + imageWidth2);
        LogUtils.d("###@: imageHeight2=" + imageHeight2);
        LogUtils.d("###@: imageBytes=" + imageBytes);

        byte[] imageArr = new byte[imageBytes];
        int offset = 0;
        int offset2;
        for (int i = 0; i < imageHeight2; i ++) {
            offset = i * rowStride;
            bb2.limit(offset + rowStride);
            bb2.position(offset);
            byte[] arr = ComUtils.byteBuf2Arr(bb2.slice());
            LogUtils.d("###@: arr=" + arr.length);
            offset2 = i * imageWidth2 * pixelStride;
            LogUtils.d("###@: offset2=" + offset2);
            for (int j = 0; j < imageWidth2; j++) {
                imageArr[offset2+j*4] = arr[j*4*4];
                imageArr[offset2+j*4+1] = arr[j*4+1];
                imageArr[offset2+j*4+2] = arr[j*4+2];
                imageArr[offset2+j*4+3] = arr[j*4+3];
            }
        }

        ByteBuffer bb3 = ComUtils.byteArr2Buf(imageArr);
        LogUtils.d("###@: bb3.position/limit/capacity=" + bb3.position() + "/" + bb3.limit() + "/" + bb3.capacity());
        ImageUtils.saveByteBuffer2JPGFile(bb3, imageFileName3, imageWidth2, imageHeight2, 0);
    }
}
