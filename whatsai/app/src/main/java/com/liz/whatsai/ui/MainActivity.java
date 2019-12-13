package com.liz.whatsai.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.app.ThisApp;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;

import androidx.appcompat.app.AppCompatActivity;

import java.security.Permissions;

public class MainActivity extends AppCompatActivity {

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d("MainActivity:onCreate: ThreadID = " + Thread.currentThread().getId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
    }

    protected void checkPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            openAppActivity();
        }
        else {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions,
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean grantedAll = true;
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.length == permissions.length) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                    LogUtils.e("NO granted on " + grantResult);
                }
            }
        }

        if (grantedAll) {
            openAppActivity();
        }
        else {
            LogUtils.e("Request Permissions Failed");
            MainActivity.this.finish();
            ThisApp.exitApp();
        }
    }

    protected void openAppActivity() {
        LogUtils.d("MainActivity:openAppActivity");
        DataLogic.init();
        tryOpenAppActivity();
    }

    protected  void tryOpenAppActivity() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                switch (DataLogic.getInitStatus()) {
                    case ComDef.INIT_STATUS_INITING:
                        tryOpenAppActivity();
                        break;
                    case ComDef.INIT_STATUS_OK:
                        startActivity(new Intent(MainActivity.this, WhatsaiActivity.class));
                        MainActivity.this.finish();
                        break;
                    case ComDef.INIT_STATUS_FAILED:
                        Toast.makeText(MainActivity.this, "ERROR: INITIALIZE FAILED, APP EXIT", Toast.LENGTH_LONG).show();
                        MainActivity.this.finish();
                        // delay some time for toast show
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                ThisApp.exitApp();
                            }
                        }, 3000L);
                        break;
                }
            }
        }, 1000L);
    }
}
