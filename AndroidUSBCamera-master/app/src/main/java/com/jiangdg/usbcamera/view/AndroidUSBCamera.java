package com.jiangdg.usbcamera.view;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.jiangdg.usbcamera.R;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * UVCCamera use demo
 * <p>
 * Created by jiangdongguo on 2017/9/30.
 */

public class AndroidUSBCamera {
    private static final String TAG = "Debug";
    @BindView(R.id.camera_view)
    public View mTextureView;
    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.seekbar_brightness)
    public SeekBar mSeekBrightness;
    @BindView(R.id.seekbar_contrast)
    public SeekBar mSeekContrast;
    @BindView(R.id.switch_rec_voice)
    public Switch mSwitchVoice;

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AlertDialog mDialog;

    private boolean isRequest;
    private boolean isPreview;

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                showShortMsg("check no usb camera");
                return;
            }
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                            mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                            mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    private void showShortMsg(String msg) {
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onCreate(Activity activity) {
        // step.1 initialize UVCCameraHelper
        mUVCCameraView = (CameraViewInterface) mTextureView;
        //####@: mUVCCameraView.setCallback(context);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(activity, mUVCCameraView, listener);

        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                Log.d("AndroidUSBCamera2222", "onPreviewResult...");
            }
        });

        mCameraHelper.registerUSB();
    }

    protected void onStop() {
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    public boolean isCameraOpened() {
        return mCameraHelper.isCameraOpened();
    }

    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }
}
