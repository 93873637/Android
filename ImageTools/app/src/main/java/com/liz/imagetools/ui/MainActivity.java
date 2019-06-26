package com.liz.imagetools.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liz.imagetools.R;
import com.liz.imagetools.logic.ComDef;
import com.liz.imagetools.utils.ComUtils;
import com.liz.imagetools.utils.FileUtils;
import com.liz.imagetools.utils.ImageUtils;
import com.liz.imagetools.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String TITLE = "ImageTools";

    public final static String MANUAL_STRING = ""
            + "1. adb shell cp /data/misc/camera/*.nv21 " + ComDef.NV21_FILES_PATH + "\n"
            + "2. click button of NV21->JPG\n"
            + "3. adb pull " + ComDef.JPG_FILES_PATH + " ./"
            ;

    private TextView mTextNV21Files;
    private TextView mTextJPGFiles;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;
    private List<File> mNV21Files = new ArrayList<>();
    private Timer mUpdateUITimer;

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

        TextView textManual = findViewById(R.id.textManual);
        textManual.setText(MANUAL_STRING);

        mTextNV21Files = findViewById(R.id.textNV21Files);
        mTextNV21Files.setText("");

        mTextJPGFiles = findViewById(R.id.textJPGFiles);
        mTextJPGFiles.setText("");

        mTextProgress = findViewById(R.id.textProgress);
        mTextProgress.setText("");
        mScrollProgress = (ScrollView) findViewById(R.id.scrollview);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

        findViewById(R.id.btnConvertN21toJPG).setOnClickListener(this);
        findViewById(R.id.clearNV21Files).setOnClickListener(this);
        findViewById(R.id.clearJPGFiles).setOnClickListener(this);
        startUITimer();
    }

    private void startUITimer() {
        //detect and update NV21 files of /sdcard/camera
        mUpdateUITimer = new Timer();
        mUpdateUITimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopUITimer() {
        if (mUpdateUITimer != null) {
            mUpdateUITimer.cancel();
            mUpdateUITimer = null;
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
            updateUI();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConvertN21toJPG:
                onConvertNV21toJPG();
                break;
            case R.id.clearNV21Files:
                onClearNV21Files();
                break;
            case R.id.clearJPGFiles:
                onClearJPGFiles();
                break;
            default:
                //LogUtils.d("MainActivity.onClick: ignored click on " + v.getId());
                break;
        }
    }

    protected void updateUI() {
        String nv21Info = "NV21 Files: ";
        if (FileUtils.listFiles(ComDef.NV21_FILES_PATH, ".nv21", mNV21Files)) {
            nv21Info += mNV21Files.size();
        }
        else {
            nv21Info += "-1";
        }
        mTextNV21Files.setText(nv21Info);

        String jpgInfo = "JPG Files: " + FileUtils.getFileNumber(ComDef.JPG_FILES_PATH, ".jpg");
        mTextJPGFiles.setText(jpgInfo);

//        mNV21Files.clear();
//
//        File[] files = new File(ComDef.NV21_FILES_PATH).listFiles();
//        if (files == null) {
//            LogUtils.d("files null, please check if path " + ComDef.NV21_FILES_PATH + " exist?");
//            nv21Info = "No file found, please check if path \"" + ComDef.NV21_FILES_PATH + "\" exist.";
//        }
//        else {
//            for (File file : files) {
//                if (file.isFile()) {
//                    String fileName = file.getName();
//                    int pos = file.getName().lastIndexOf(".nv21");
//                    int posSuffix = fileName.length() - 5;
//                    if (pos >= 0 && posSuffix >= 0 && pos == posSuffix) {
//                            mNV21Files.add(file);
//                    }
//                }
//                else {
//                    LogUtils.d("skip dir");
//                }
//            }
//
//            nv21Info = "NV21 Files: " + mNV21Files.size();
//        }

    }

    protected void onConvertNV21toJPG() {
        stopUITimer();
        updateUI();

        if (mNV21Files.size() == 0) {
            updateUI();
        }

        if (mNV21Files.size() > 0) {
            new Thread() {
                public void run() {
                    runConvert();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            startUITimer();
                        }
                    });
                }
            }.start();
        }
        else {
            showProgress("No nv21 files found in path \"" + ComDef.NV21_FILES_PATH + "\"");
        }
    }

    protected void onClearNV21Files() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure to clear all NV21 files?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FileUtils.clearDirectory(ComDef.NV21_FILES_PATH);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    protected void onClearJPGFiles() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure to clear all JPG files?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FileUtils.clearDirectory(ComDef.JPG_FILES_PATH);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
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
            Log.d("ImageTools", "NV21toJPG: file=" + file.getAbsolutePath() + ", size=" + width + "x" + height);
            //showProgress("width=" + width + ", height=" + height);
            String res = ImageUtils.NV21toJPG(file, width, height, ComDef.JPG_FILES_PATH, 100);
            showProgress(res);
            showProgress("");
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
                    updateUI();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mTextNV21Files.setText("no sdcard permission");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
