package com.liz.imagetools;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.liz.imagetools.utils.ComUtils;
import com.liz.imagetools.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String TITLE = "ImageTools";

    public final static String NV21_FILES_PATH = "/sdcard/camera"; ///data/misc/camera/";
    public final static String JPG_FILES_PATH = "/sdcard/camera/jpg"; ///data/misc/camera/";

    public final static String MANUAL_STRING = "[STEPS]:\n"
            + "1. create dirs if not exist:\n"
            + "    adb root\n"
            + "    adb shell mkdir " + NV21_FILES_PATH + "\n"
            + "    adb shell mkdir " + JPG_FILES_PATH + "\n"
            + "2. copy nv21 files to sdcard:\n"
            + "    adb shell cp /data/misc/camera/*.nv21 " + NV21_FILES_PATH + "\n"
            + "3. click the button below to convert, the jpg files will be saved\n"
            + "    to " + JPG_FILES_PATH + ".\n"
            + "4. then you can pull out the jpg files to view on PC:\n"
            + "    adb pull " + JPG_FILES_PATH + " ./"
            ;

    private Button mBtnConvert;
    private TextView mTextManual;
    private TextView mTextStatic;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;
    private List<File> mNV21Files = new ArrayList<>();
    private Timer mFileDetectTimer;

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
        this.setTitle(TITLE + " " + ComUtils.getAppVersion(this));

        mTextManual = (TextView) findViewById(R.id.textManual);
        mTextManual.setText(MANUAL_STRING);

        mTextStatic = (TextView) findViewById(R.id.textStatic);
        mTextStatic.setText("");

        mTextProgress = (TextView) findViewById(R.id.textProgress);
        mTextProgress.setText("");
        mScrollProgress = (ScrollView) findViewById(R.id.scrollview);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

        mBtnConvert = (Button) findViewById(R.id.btnConvert);
        mBtnConvert.setOnClickListener(this);

        startDetectTimer();
    }

    private void startDetectTimer() {
        //detect and update NV21 files of /sdcard/camera
        mFileDetectTimer = new Timer();
        mFileDetectTimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateStaticInfo();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopDetectTimer() {
        if (mFileDetectTimer != null) {
            mFileDetectTimer.cancel();
            mFileDetectTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        else {
            updateStaticInfo();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConvert:
                onConvert();
                break;
            default:
                //LogUtils.d("MainActivity.onClick: ignored click on " + v.getId());
                break;
        }
    }

    protected void updateStaticInfo() {
        String info = "";
        mNV21Files.clear();

        File[] files = new File(NV21_FILES_PATH).listFiles();
        if (files == null) {
            LogUtils.d("files null, please check if path " + NV21_FILES_PATH + " exist?");
            info = "No file found, please check if path \"" + NV21_FILES_PATH + "\" exist.";
        }
        else {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    int pos = file.getName().lastIndexOf(".nv21");
                    int posSuffix = fileName.length() - 5;
                    if (pos >= 0 && posSuffix >= 0 && pos == posSuffix) {
                            mNV21Files.add(file);
                    }
                }
                else {
                    LogUtils.d("skip dir");
                }
            }

            info = "NV21 Files in " + NV21_FILES_PATH + ": " + mNV21Files.size();
        }

        mTextStatic.setText(info);
    }

    protected void onConvert() {
        stopDetectTimer();
        updateStaticInfo();

        if (mNV21Files.size() == 0) {
            updateStaticInfo();
        }

        if (mNV21Files.size() > 0) {
            new Thread() {
                public void run() {
                    runConvert();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            startDetectTimer();
                        }
                    });
                }
            }.start();
        }
        else {
            showProgress("No nv21 files found in path \"" + NV21_FILES_PATH + "\"");
        }
    }

    protected void showProgress(final String msg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mTextProgress.append(msg + "\n");
                mScrollProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollProgress.smoothScrollTo(0, mTextProgress.getBottom());
                    }
                });
            }
        });
    }

    protected void runConvert() {
        Log.d("ImageTools", "runConvert: Enter...");
        int count = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        try {
            int fileNumber = mNV21Files.size();
            showProgress("***Start Convert...");
            for (File file : mNV21Files) {
                count ++;
                showProgress("#" + count + "/" + fileNumber + ": file=" + file.getName());
                if (convertNV21FiletoJPG(file)) {
                    totalSuccess++;
                } else {
                    totalFailed++;
                }
            }
        }
        catch (Exception e) {
            Log.d("ImageTools", "runConvert Exception: " + e.toString());
            showProgress("Exception: " + e.toString());
        }

        Log.d("ImageTools", "runConvert: Exit.");
        showProgress("***Convert Finished: total=" + count + ", Success=" + totalSuccess + ", Failed=" + totalFailed);
    }

    protected boolean convertNV21FiletoJPG(File file) {
        String fileName = file.getName();
        try {
            Log.d("ImageTools", "convertNV21FiletoJPG: fileName=" + fileName);

            //parse width and height from name
            //aux_1920x1440.nv21
            //input_0_4096x3040_20180825062649.nv21
            //out_1920x1440.nv21
            //aux_3840x2160_1006.nv21
            int xCharPos = fileName.lastIndexOf('x');
            if (xCharPos < 0) {
                showProgress("ERROR: No 'x' found in fileName: " + fileName);
                return false;
            }

            int rightDotCharPos = fileName.indexOf('.', xCharPos);
            if (rightDotCharPos < 0) {
                showProgress( "ERROR: No '.' found after 'x' in fileName: " + fileName);
                return false;
            }

            int rightCharPos = rightDotCharPos;
            int rightUnderLineCharPos = fileName.indexOf('_', xCharPos);
            if (rightUnderLineCharPos != -1) {
                Log.d("ImageTools", "get rightUnderLineCharPos: " + rightUnderLineCharPos);
                rightCharPos = Math.min(rightDotCharPos, rightUnderLineCharPos);
            }

            if (rightCharPos <= 0) {
                showProgress ("ERROR: invalid rightCharPos: " + rightCharPos + " by fileName: " + fileName);
                return false;
            }

            String heightString = fileName.substring(xCharPos + 1, rightCharPos);
            int height = Integer.parseInt(heightString);
            if (height <= 0) {
                showProgress ("ERROR: invalid height " + height + " by fileName: " + fileName);
                return false;
            }

            int leftCharPos = fileName.lastIndexOf('_', xCharPos);
            if (leftCharPos <= 0) {
                showProgress ("ERROR: No '_' before 'x' found by fileName: " + fileName);
                return false;
            }
            String widthString = fileName.substring(leftCharPos + 1, xCharPos);
            int width = Integer.parseInt(widthString);
            if (width <= 0) {
                showProgress ("ERROR: invalid height " + width + " by fileName: " + fileName);
                return false;
            }

            //Toast.makeText(this, "NV21toJPG: file=" + fileName +", " + width + "x" + height, Toast.LENGTH_LONG).show();
            Log.d("ImageTools", "NV21toJPG: file=" + file.getAbsolutePath() + ", width=" + width + ", height=" + height);
            showProgress("width=" + width + ", height=" + height);
            String res = ImageUtils.NV21toJPG(file, width, height, JPG_FILES_PATH, 100);
            showProgress(res);
            return res.equals(ImageUtils.SUCCESS);
        }
        catch (Exception e) {
            Log.d("ImageTools", "convertNV21FiletoJPG Exception: " + e.toString());
            showProgress ("ERROR: convertNV21FiletoJPG Exception: " + e.toString() + " for fileName: " + fileName);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    updateStaticInfo();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mTextStatic.setText("no sdcard permission");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
