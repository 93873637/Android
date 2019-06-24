package com.liz.testcamera.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.liz.testcamera.R;

public class MainActivity extends AppCompatActivity {

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // For Permissions

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static String[] PERMISSIONS_REQUEST = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean checkPermissions(Activity activity) {
        return (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            //all request permissions should be granted
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "No Permissions, TestCamera Exit.", Toast.LENGTH_LONG).show();
                    MainActivity.this.finish();
                    return;
                }
            }

            //all request permissions have been granted
            startCameraActivity();
        }
    }

    // For Permissions
    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermissions(this)) {
            startCameraActivity();
        }
        else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUEST, REQUEST_CODE_PERMISSIONS);
        }
    }

    protected void startCameraActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }
}
