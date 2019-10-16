package com.liz.androidutilstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.liz.androidutils.ComUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.MailSender;
import com.liz.androidutils.SysTools;
import com.liz.androidutils.SysUtils;
import com.liz.androidutils.FileUtils;
import com.liz.androidutils.TimeChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

@SuppressWarnings("unused")
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

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Test Functions

    public void test() {
        test_fileReadLines();
        //test_fileSort();
        //test_SysTools();
        //test_image();
        //test_SysUtils();
        //test_saveByteBufferToFile();
        //test_image_scale_by_buffer();
        //test_image_scale_by_bitmap();
        //LogUtils.d("###@: time=" + System.currentTimeMillis());
    }

    public void test_fileReadLines() {
        ArrayList<String> lineList = FileUtils.readTxtFileLines("/sdcard/tellist.txt");
        for (int i=0; i<lineList.size(); i++) {
            LogUtils.d("#" + (i+1) + ": " + lineList.get(i));
        }
    }

    public void test_fileSort() {
        File[] files = FileUtils.getFileList("/sdcard/0.sd/whatsai/audio", FileUtils.ORDER_BY_DATE_DESC);
        for (int i=0; i<files.length; i++) {
            LogUtils.d("###@: " + files[i].getAbsolutePath());
        }
    }

    public void test_SysTools() {
        if (SysTools.checkRootExecutable()) {
            Toast.makeText(this, "ROOTED!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "NOT ROOT!", Toast.LENGTH_LONG).show();
        }
    }

    public void test_mail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //MailSender.test_send_mail_simple();
                //MailSender.test_send_mail_multiple();
                //MailSender.test_send_mail_multiple_cc();
                //MailSender.test_send_mail_content_subject();
                //MailSender.test_send_mail_content_subject_attach();
            }
        }).start();
    }

    public static void test_saveByteBufferToFile() {
        //String filePath = "D:\\temp\\aa.bin";
        String filePath = "/storage/emulated/0/Pictures/ScreenShots/ScreenShot_190815.152758.287.bin";
        CharBuffer charBuffer=CharBuffer.allocate(1024);
        charBuffer.put("123456789012345678901234567890");
        charBuffer.flip();
        Charset charset=Charset.defaultCharset();
        ByteBuffer byteBuffer=charset.encode(charBuffer);
        boolean ret = saveByteBufferToFile(byteBuffer, filePath);
        System.out.println("test_saveByteBufferToFile: file="+ filePath + ", size=" + byteBuffer.capacity() + ", ret=" + ret);
    }

    public static boolean saveByteBufferToFile(ByteBuffer byteBuffer, String fileAbsolute) {
        try {
//            FileChannel fc = new FileOutputStream(fileAbsolute).getChannel();
//            fc.write(bb.array());
//            fc.close();
//            File file = new File(fileAbsolute);

//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileOutputStream fos = new FileOutputStream(file, true);
//            fos.write(bb.array());
//            fos.flush();
//            fos.close();

            FileOutputStream outputStream=new FileOutputStream(new File(fileAbsolute));
            FileChannel fileChannel=outputStream.getChannel();
            //cont write all data
            while(byteBuffer.hasRemaining()){
                fileChannel.write(byteBuffer);
            }
            fileChannel.close();
            outputStream.close();
        } catch (Exception e) {
            System.out.println("ERROR: save ByteBuffer to file exception: " + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void test_SysUtils() {
        LogUtils.d("test_SysUtils: appVersion = " + SysUtils.getAppVersion(MainActivity.this));
    }

    public void test_image() {
        String dataFileName = "/sdcard/Pictures/Screenshots/ScreenShot_190815.154153.357.bin";
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

    public void test_image_scale_by_buffer() {
        String dataFileName = "/sdcard/images/ScreenShot_190815.154153.357.bin";

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

        LogUtils.d("***test_image_scale_by_buffer: begin...");
        int imageWidth = 1440;
        int imageHeight = 2392;
        int pixelStride = 4;
        int rowPadding = 128;
        int pixelPadding = rowPadding / pixelStride;  //32
        int totalWidth = imageWidth + pixelPadding;  //1472
        int totalHeight = imageHeight;
        int rowStride = totalWidth * pixelStride;  //5888
        //ImageUtils.saveByteBuffer2JPGFile(byteBuffer, imageFileName, imageWidth, imageHeight, pixelPadding);

        int scale = 4;
        int imageHeight2 = totalHeight / scale;
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(rowStride * imageHeight2);
        byteBuffer.flip();
        byteBuffer2.flip();
//        LogUtils.d("###@: imageHeight2=" + imageHeight2);
//        LogUtils.d("###@: bb.position/limit/capacity=" + byteBuffer.position() + "/" + byteBuffer.limit() + "/" + byteBuffer.capacity()
//                + ": byteBuffer2.position/limit/capacity=" + byteBuffer2.position() + "/" + byteBuffer2.limit() + "/" + byteBuffer2.capacity());

        for (int i = 0; i < imageHeight2; i ++) {
            byteBuffer.limit((i * scale + 1) * rowStride);
            byteBuffer.position(i * scale * rowStride);
            byteBuffer2.limit((i + 1) * rowStride);
            byteBuffer2.position(i * rowStride);
            //LogUtils.d("###@: i=" + i + ": bb.position/limit/capacity=" + bb.position() + "/" + bb.limit() + "/" + bb.capacity()
            //        + ": byteBuffer2.position/limit/capacity=" + byteBuffer2.position() + "/" + byteBuffer2.limit() + "/" + byteBuffer2.capacity());
            byteBuffer2.put(byteBuffer.slice());
        }

        //ImageUtils.saveByteBuffer2JPGFile(byteBuffer2, imageFileName2, imageWidth, imageHeight2, pixelPadding);

        int imageWidth2 = imageWidth / scale;
        int imageBytes = imageWidth2 * imageHeight2 * (pixelStride);
//        LogUtils.d("###@: imageWidth2=" + imageWidth2);
//        LogUtils.d("###@: imageHeight2=" + imageHeight2);
//        LogUtils.d("###@: imageBytes=" + imageBytes);

        byte[] imageArr = new byte[imageBytes];
        int offset;
        int offset2;
        for (int i = 0; i < imageHeight2; i ++) {
            offset = i * rowStride;
            byteBuffer2.limit(offset + rowStride);
            byteBuffer2.position(offset);
            byte[] arr = ComUtils.byteBuf2Arr(byteBuffer2.slice());
            //LogUtils.d("###@: arr=" + arr.length);
            offset2 = i * imageWidth2 * pixelStride;
            //LogUtils.d("###@: offset2=" + offset2);
            for (int j = 0; j < imageWidth2; j++) {
                int imgOff = offset2 + j * 4;
                int arrOff = j*4*4;
                imageArr[imgOff] = arr[arrOff];
                imageArr[imgOff+1] = arr[arrOff+1];
                imageArr[imgOff+2] = arr[arrOff+2];
                imageArr[imgOff+3] = arr[arrOff+3];
            }
        }
        LogUtils.d("***test_image_scale_by_buffer: end.");

        ByteBuffer byteBuffer3 = ComUtils.byteArr2Buf(imageArr);
        LogUtils.d("###@: byteBuffer3.position/limit/capacity=" + byteBuffer3.position() + "/" + byteBuffer3.limit() + "/" + byteBuffer3.capacity());
        ImageUtils.saveByteBuffer2JPGFile(byteBuffer3, imageFileName3, imageWidth2, imageHeight2, 0);
    }

    public void test_image_scale_by_bitmap() {
        String dataFileName = "/sdcard/images/ScreenShot_190815.154153.357.bin";
        String imageFileName = dataFileName + ".bmp.jpg";
        String imageFileName2 = dataFileName + ".arr.jpg";
        String imageArrFileName = dataFileName + ".arr.bin";

        FileUtils.removeFile(imageFileName);
        FileUtils.removeFile(imageFileName2);
        FileUtils.removeFile(imageArrFileName);

        ByteBuffer byteBuffer = ComUtils.readByteBufferFromFile(dataFileName);
        if (byteBuffer == null) {
            System.out.println("read failed");
            return;
        }
        System.out.println("read ok, buffer size = " + byteBuffer.capacity());

        //set the params
        int imageWidth = 1440;
        int imageHeight = 2392;
        int pixelStride = 4;
        int rowPadding = 128;
        int pixelPadding = rowPadding / pixelStride;  //32

        TimeChecker tc = new TimeChecker("ByteBuffer2JPG by Bitmap");
        Bitmap bmp = ImageUtils.byteBuffer2Bitmap(byteBuffer,imageWidth, imageHeight, pixelPadding, Bitmap.Config.ARGB_8888, 0.25);
        tc.checkPoint("byteBuffer2Bitmap");
        ImageUtils.saveBitmap2JPGFile(bmp, imageFileName, 100);
        tc.checkPoint("saveBitmap2JPGFile");

        {
            TimeChecker tc2 = new TimeChecker("ByteBuffer2JPG by scale");
            byte[] jpgArr = ImageUtils.byteBuffer2JPGArray(byteBuffer, imageWidth, imageHeight, pixelPadding, pixelStride, 4, 100);
            tc2.checkPoint("byteBuffer2Array bitmap2Bytes, size = " + jpgArr.length);
            ImageUtils.saveJPGArray2File(jpgArr, imageFileName2, 100);
        }
    }
}
