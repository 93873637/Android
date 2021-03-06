package com.liz.wsrecorder.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liz.androidutils.LogUtils;
import com.liz.wsrecorder.R;
import com.liz.wsrecorder.app.MyApp;

public class MainActivity extends AppCompatActivity {

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

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
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.trace();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
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
            MyApp.exitApp();
        }
    }

    protected void openAppActivity() {
        LogUtils.trace();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAPTURE_AUDIO_OUTPUT) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "CAPTURE_AUDIO_OUTPUT granted OK!!!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "CAPTURE_AUDIO_OUTPUT NOT granted", Toast.LENGTH_LONG).show();
        }

        startActivity(new Intent(MainActivity.this, AudioRecordActivity.class));
        MainActivity.this.finish();
    }
}
