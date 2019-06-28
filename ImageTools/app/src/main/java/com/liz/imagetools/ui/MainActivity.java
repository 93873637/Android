package com.liz.imagetools.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liz.imagetools.R;
import com.liz.imagetools.app.ThisApp;
import com.liz.imagetools.logic.ComDef;
import com.liz.imagetools.logic.DataLogic;
import com.liz.imagetools.utils.FileUtils;
import com.liz.imagetools.utils.ImageUtils;
import com.liz.imagetools.utils.LogUtils;
import com.liz.imagetools.utils.SysUtils;
import com.liz.imagetools.utils.TimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextNV21Files;
    private TextView mTextJPGFiles;
    private TextView mTextImageSize;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;
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
        LogUtils.d("MainActivity: onCreate");

        ThisApp.setMainActivity(this);

        setContentView(R.layout.activity_main);
        this.setTitle(ComDef.APP_NAME + " " + SysUtils.getVersionName(this));

        TextView textManual = findViewById(R.id.textManual);
        textManual.setText(ComDef.MANUAL_STRING);

        mTextNV21Files = findViewById(R.id.textNV21Files);
        mTextNV21Files.setText("");

        mTextJPGFiles = findViewById(R.id.textJPGFiles);
        mTextJPGFiles.setText("");

        mTextImageSize = findViewById(R.id.textImageSize);
        mTextImageSize.setText("");

        mTextProgress = findViewById(R.id.textProgress);
        mTextProgress.setText("");
        mScrollProgress = (ScrollView) findViewById(R.id.scrollview);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

        findViewById(R.id.btnConvertN21toJPG).setOnClickListener(this);
        findViewById(R.id.clearNV21Files).setOnClickListener(this);
        findViewById(R.id.clearJPGFiles).setOnClickListener(this);
        findViewById(R.id.modifyImageSize).setOnClickListener(this);

        DataLogic.setDataProcessListener(new DataLogic.DataProcessListener() {
            @Override
            public void onProgressMessage(String msg) {
                MainActivity.this.showProgress(msg);
            }
        });

        Intent intent = getIntent();
        String msg = intent.getStringExtra(ComDef.IMAGE_TOOLS_MSG);
        if (msg != null && msg.equals(ComDef.ACTION_IMAGE_TOOLS_NV21_TO_JPG)) {
            onConvertNV21toJPG();
        }
        else {
            startUITimer();
        }
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
        LogUtils.d("MainActivity: onResume");

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
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("MainActivity: onDestroy");
        stopUITimer();
        ThisApp.setMainActivity(null);
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
            case R.id.modifyImageSize:
                onModifyImageSize();
                break;
            default:
                LogUtils.d("MainActivity.onClick: ignored click on " + v.getId());
                break;
        }
    }

    protected void updateUI() {
        String nv21Info = "NV21 Files: ";
        List<File> nv21Files = new ArrayList<>();
        if (FileUtils.listFiles(ComDef.NV21_FILES_PATH, ComDef.NV21_FILE_EXTNAME, nv21Files)) {
            nv21Info += nv21Files.size();
        }
        else {
            nv21Info += "-1";
        }
        mTextNV21Files.setText(nv21Info);

        String jpgInfo = "JPG Files: " + FileUtils.getFileNumber(ComDef.JPG_FILES_PATH, ".jpg");
        mTextJPGFiles.setText(jpgInfo);

        String imageInfo = "Image Size: " + DataLogic.getImageWidth(this) + "x" + DataLogic.getImageHeight(this);
        mTextImageSize.setText(imageInfo);
    }

    protected void onConvertNV21toJPG() {
        new Thread() {
            public void run() {
                DataLogic.convertNV21toJPG(MainActivity.this);
            }
        }.start();
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

    protected void onModifyImageSize() {

        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.edit_image_size, null);
        final EditText editWdith = textEntryView.findViewById(R.id.editWdith);
        final EditText editHeight = textEntryView.findViewById(R.id.editHeight);
        editWdith.setText("" + DataLogic.getImageWidth(this));
        editHeight.setText("" + DataLogic.getImageHeight(this));

        AlertDialog.Builder ad1 = new AlertDialog.Builder(MainActivity.this);
        ad1.setTitle("Modify Image Size");
        ad1.setIcon(android.R.drawable.ic_dialog_info);
        ad1.setView(textEntryView);
        ad1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                DataLogic.setImageWidth(MainActivity.this, Integer.parseInt(editWdith.getText().toString()));
                DataLogic.setImageHeight(MainActivity.this, Integer.parseInt(editHeight.getText().toString()));
            }
        });
        ad1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
            }
        });

        ad1.show();
    }

    protected void showProgress(final String msg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                String logMsg = TimeUtils.getLogTime() + " - " + msg;
                mTextProgress.append(logMsg + "\n");
                mScrollProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollProgress.smoothScrollTo(0, mTextProgress.getBottom());
                    }
                });
            }
        });
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
