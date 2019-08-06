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


    public void test() {
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


//        File f = new File(imageFileName);
//        if (f.exists()) {
//            System.out.println("image file " + imageFileName + " already exist, delete it");
//            f.delete();
//        }
        //ImageUtils.saveByteBuffer2JPGFile(bb, imageFileName, imageWidth, imageHeight, pixelPadding);

        int scale = 4;
        int newHeight = totalHeight / scale;
        ByteBuffer bbNew = ByteBuffer.allocate(rowStride * newHeight);
        bb.flip();
        bbNew.flip();
        LogUtils.d("###@: newHeight=" + newHeight);
        LogUtils.d("###@: bbNew.capacity()=" + bbNew.capacity());
        LogUtils.d("###@: bbNew.position()=" + bbNew.position());
        LogUtils.d("###@: bbNew.limit()=" + bbNew.limit());
        LogUtils.d("###@: bb.capacity()=" + bb.capacity());
        LogUtils.d("###@: bb.position()=" + bb.position());
        LogUtils.d("###@: bb.limit()=" + bb.limit());

        for (int i = 0; i < newHeight; i ++) {
            bb.position(i * scale * rowStride);
            bb.limit((i * scale + 1) * rowStride);
            bbNew.position(i * rowStride);
            bbNew.limit((i + 1) * rowStride);
            LogUtils.d("###@: i=" + i + ": bb.position/limit/capacity=" + bb.position() + "/" + bb.limit() + "/" + bb.capacity()
                    + ": bbNew.position/limit/capacity=" + bbNew.position() + "/" + bbNew.limit() + "/" + bbNew.capacity());
            bbNew.put(bb.slice());
        }
    }
}
