package com.liz.androidutils.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class AppCompatActivityEx extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 2297;

    private PermissionCallback mPermissionCallback;

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public void requestPermissions(String[] permissions, PermissionCallback callback) {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (allPermissionsGranted) {
            if (callback != null) {
                callback.onPermissionGranted();
            }
        }
        else {
            mPermissionCallback = callback;
            ActivityCompat.requestPermissions(this, permissions,
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
                }
            }
        }
        if (grantedAll) {
            if (mPermissionCallback != null) {
                mPermissionCallback.onPermissionGranted();
            }
        }
        else {
            Toast.makeText(this, "Request Permissions Failed", Toast.LENGTH_LONG).show();
            if (mPermissionCallback != null) {
                mPermissionCallback.onPermissionDenied();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeUITimer();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Timer
    private Timer mUITimer;
    public void setUITimer(long timerDelay, long timerPeriod) {
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                AppCompatActivityEx.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, timerDelay, timerPeriod);
    }
    public void removeUITimer() {
        if (mUITimer != null) {
            mUITimer.cancel();
            mUITimer = null;
        }
    }

    protected void updateUI() {
        //TODO: override
    }

    // UI Timer
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
