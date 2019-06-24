package com.liz.puretorch.app;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import com.liz.puretorch.ui.FlashActivity;

public class FlashCloseReceiver extends BroadcastReceiver {
    CameraManager mCameraManager = null;
    String[] mCameraIds = null;

    public FlashCloseReceiver() {
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("onReceiver");
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager != null) {
            try {
                mCameraIds = mCameraManager.getCameraIdList();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        String action = intent.getAction();
        if (action.equals(FlashActivity.CLOSE_FLASH_ACTION)) {
            if (mCameraManager != null && mCameraIds != null && mCameraIds.length != 0) {
                try {
                    System.out.println("setTorchMode");
                    mCameraManager.setTorchMode(mCameraIds[0], false);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
