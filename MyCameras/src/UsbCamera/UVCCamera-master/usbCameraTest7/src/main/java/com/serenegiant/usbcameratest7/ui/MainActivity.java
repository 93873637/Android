/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.usbcameratest7.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameratest7.R;
import com.serenegiant.usbcameratest7.app.MyApp;
import com.serenegiant.usbcameratest7.logic.ComDef;
import com.serenegiant.usbcameratest7.utils.LogUtils;
import com.serenegiant.usbcameratest7.utils.UVCUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.serenegiant.usbcameratest7.ui.USBCameraView.CAMERA_VIEW_ACTION_OPEN_LIST;

/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */
public final class MainActivity extends BaseActivity implements CameraListDialog.CameraDialogParent {

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;

    private ArrayList<USBCameraView> mCameraViewList = new ArrayList<>();
    private USBCameraView mFocusView = null;

    private LinearLayout mLayoutTitleBar;
    private TextView mTextCameraInfo;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions Check & Require

    /**
     * add checkPermissions on Activity::onCreate
     */

    // Permissions Request
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    protected void checkPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (!allPermissionsGranted) {
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
        if (!grantedAll) {
            LogUtils.e("Request Permissions Failed");
            Toast.makeText(this, "No Permission", Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
            MyApp.exitApp();
        }
    }

    // Permissions Check & Require
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //NOTE: following statements must be called before setContentView!
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        //layout 4x2
        addCamView("11", R.id.camera_view_L, R.id.capture_button_L, R.id.record_button_L, R.id.info_button_L, R.id.text_camera_L);
        addCamView("12", R.id.camera_view_R, R.id.capture_button_R, R.id.record_button_R, R.id.info_button_R, R.id.text_camera_R);
        addCamView("21", R.id.camera_view_L2, R.id.capture_button_L2, R.id.record_button_L2, R.id.info_button_L2, R.id.text_camera_L2);
        addCamView("22", R.id.camera_view_R2, R.id.capture_button_R2, R.id.record_button_R2, R.id.info_button_R2, R.id.text_camera_R2);
        addCamView("31", R.id.camera_view_L3, R.id.capture_button_L3, R.id.record_button_L3, R.id.info_button_L3, R.id.text_camera_L3);
        addCamView("32", R.id.camera_view_R3, R.id.capture_button_R3, R.id.record_button_R3, R.id.info_button_R3, R.id.text_camera_R3);
        addCamView("41", R.id.camera_view_L4, R.id.capture_button_L4, R.id.record_button_L4, R.id.info_button_L4, R.id.text_camera_L4);
        addCamView("42", R.id.camera_view_R4, R.id.capture_button_R4, R.id.record_button_R4, R.id.info_button_R4, R.id.text_camera_R4);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        startUITimer();

        mLayoutTitleBar = findViewById(R.id.layout_title_bar);
        mTextCameraInfo = findViewById(R.id.text_camera_info);
        mLayoutTitleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "click on title", Toast.LENGTH_SHORT).show();
                switchAllOnOff();
            }
        });

        findViewById(R.id.RelativeLayout1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click on layout", Toast.LENGTH_SHORT).show();
            }
        });

        checkPermissions();
    }

    private boolean mAllOpened = false;
    private void switchAllOnOff() {
        if (!mAllOpened) {
            openAllCamera();
            startCAPTimer();
        }
        else {
            closeAllCamera();
            stopCAPTimer();
        }
        mAllOpened = !mAllOpened;
    }

    private void openAllCamera() {
        for (USBCameraView view : mCameraViewList) {
            view.openCamera();
        }
    }

    private void closeAllCamera() {
        for (USBCameraView view : mCameraViewList) {
            view.closeCamera();
        }
    }

    private void addCamView(String id, int viewId, int captureButtonId, int recordButtonId, int infoButtonId, int textInfoId) {
        mCameraViewList.add(new USBCameraView(id,
                this,
                findViewById(viewId),
                findViewById(captureButtonId),
                findViewById(recordButtonId),
                findViewById(infoButtonId),
                findViewById(textInfoId),
                mOnClickListener));
    }

    private void setTitleText() {
        String info = ComDef.APP_NAME;
        info += " - " + mCameraViewList.size() + "/" + getOpenCameraCount() + "/" + getUsbCameraCount();
        mTextCameraInfo.setText(info);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Timer
    private static final long UI_TIMER_DELAY = 0L;
    private static final long UI_TIMER_PERIOD = 2000L;
    private Timer mUITimer;
    private void startUITimer() {
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        setTitleText();
                        for (USBCameraView camView : mCameraViewList) {
                            camView.setCameraView();
                        }
                    }
                });
            }
        }, UI_TIMER_DELAY, UI_TIMER_PERIOD);
    }
    private void stopUITimer() {
        if (mUITimer != null) {
            mUITimer.cancel();
            mUITimer = null;
        }
    }
    // UI Timer
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CAP Timer
    private static final long CAP_TIMER_DELAY = 15000L;
    private Timer mCAPTimer;
    private void startCAPTimer() {
        mCAPTimer = new Timer();
        mCAPTimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        vendingCapOnce();
                    }
                });
            }
        }, CAP_TIMER_DELAY);
    }
    private void stopCAPTimer() {
        if (mCAPTimer != null) {
            mCAPTimer.cancel();
            mCAPTimer = null;
        }
    }
    // CAP Timer
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String TAG = "USB MultiCam Test";

    public static void logd(String msg) {
        Log.d(TAG, msg);
    }

    public static void logi(String msg) {
        Log.e(TAG, msg);
    }

    public static void loge(String msg) {
        Log.e(TAG, msg);
    }

    private void vendingCapOnce() {
        logi("TIME@: vendingCapOnce: start...");
        for (USBCameraView view : mCameraViewList) {
            view.captureOnce();
        }
    }

    public List<UsbDevice> getCameraDevices() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(MainActivity.this, com.serenegiant.uvccamera.R.xml.device_filter);
        return mUSBMonitor.getDeviceList(filter.get(0));
    }

    private List<UsbDevice> getAvailableCameraDevices() {
        List<UsbDevice> usbDevices = getCameraDevices();
        List<UsbDevice> usbDevicesAvailable = new ArrayList<UsbDevice>();
        if (usbDevices != null && usbDevices.size() > 0) {
            for (UsbDevice dev : usbDevices) {
                if (!isDeviceAttached(dev)) {
                    usbDevicesAvailable.add(dev);
                }
            }
        }
        return usbDevicesAvailable;
    }

    private boolean isDeviceAttached(UsbDevice device) {
        for (USBCameraView cameraView : mCameraViewList) {
            if (cameraView.isMyDevice(device)) {
                return true;
            }
        }
        return false;
    }

    private int getUsbCameraCount() {
        return UVCUtils.getUsbDeviceCount(this, mUSBMonitor);
    }

    private int getOpenCameraCount() {
        int count = 0;
        for (USBCameraView cameraView : mCameraViewList) {
            if (cameraView.isCameraOpened()) {
                count ++;
            }
        }
        return count;
    }

    private boolean isAllDeviceOpened() {
        return getOpenCameraCount() == getUsbCameraCount();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            for (USBCameraView cameraView : mCameraViewList) {
                int ret = cameraView.onClickCameraView(view);
                if (ret > 0) {
                    mFocusView = cameraView;
                    if (ret == CAMERA_VIEW_ACTION_OPEN_LIST) {
                        List<UsbDevice> list = getAvailableCameraDevices();
                        if (list != null && list.size() > 0) {
                            CameraListDialog.showDialog(MainActivity.this, list, cameraView.getViewID());
                        } else {
                            Toast.makeText(MainActivity.this, "No camera device available", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        for (USBCameraView view : mCameraViewList) {
            view.onStart();
        }
    }

    @Override
    protected void onStop() {
        for (USBCameraView view : mCameraViewList) {
            view.onStop();
        }
        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopUITimer();
        for (USBCameraView view : mCameraViewList) {
            view.onDestroy();
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        super.onDestroy();
    }

    private void showShortMsg(String msg) {
        LogUtils.d(msg);
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            showShortMsg("onAttach: " + device.getDeviceId());
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            showShortMsg("onConnect: " + device.getDeviceId());
            if (mFocusView != null) {
                mFocusView.onConnect(device, ctrlBlock, createNew);
            }
            else {
                showShortMsg("WARNING: No focus view? choose one");
                for (USBCameraView view : mCameraViewList) {
                    if (view.onConnect(device, ctrlBlock, createNew)) {
                        break;
                    }
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            showShortMsg("onDisconnect: " + device.getDeviceId());
            for (USBCameraView view : mCameraViewList) {
                if (view.onDisconnect(device, ctrlBlock)) {
                    break;
                }
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            showShortMsg("onDettach: " + device.getDeviceId());
        }

        @Override
        public void onCancel(final UsbDevice device) {
             showShortMsg("onCancel: " + device.getDeviceId());
        }
    };

    //####@: diu
//    /**
//     * to access from CameraDialog
//     *
//     * @return
//     */
//    @Override
//    public USBMonitor getUSBMonitor() {
//        return mUSBMonitor;
//    }

    @Override
    public void onDialogResult(boolean canceled, UsbDevice device) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCameraButton();
                }
            }, 0);
        }
        else {
            if (device != null) {
                mUSBMonitor.requestPermission(device);
            }
            else {
                LogUtils.e("ERROR: select device null");
            }
        }
    }

    private void setCameraButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (USBCameraView view : mCameraViewList) {
                    view.setCameraView();
                }
            }
        });
    }
}
