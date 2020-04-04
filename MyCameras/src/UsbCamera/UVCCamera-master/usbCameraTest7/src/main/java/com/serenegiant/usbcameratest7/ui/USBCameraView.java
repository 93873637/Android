package com.serenegiant.usbcameratest7.ui;

import android.app.AlertDialog;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameratest7.logic.ComDef;
import com.serenegiant.usbcameratest7.utils.LogUtils;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import org.webrtc.GlUtil;

import java.util.List;


public class USBCameraView {

    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

    private String mViewID;
    private BaseActivity mActivityParent;
    private UVCCameraHandler mHandler;
    private CameraViewInterface mViewInterface;
    private ImageButton mCaptureButton;
    private ImageButton mRecordButton;
    private ImageButton mInfoButton;
    private Surface mPreviewSurface;
    private TextView mCameraText;
    private UsbDevice mUsbDevice;
    private USBMonitor.UsbControlBlock mUsbControlBlock;

    public USBCameraView(String id, BaseActivity activityParent, CameraViewInterface viewInterface,
                         ImageButton captureButton, ImageButton recordButton, ImageButton infoButton, TextView cameraText,
                         View.OnClickListener onClickListener) {
        mViewID = id;
        initView(activityParent, viewInterface, captureButton, recordButton, infoButton, cameraText, onClickListener);
        showMsg("Create");
    }

    private void showMsg(String msg) {
        showShortMsg("UVC#" + mViewID + ": " + msg);
    }

    private void showShortMsg(String msg) {
        LogUtils.d(msg);
        if (mActivityParent != null) {
            Toast.makeText(mActivityParent, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(BaseActivity activityParent, CameraViewInterface viewInterface,
                          ImageButton captureButton, ImageButton recordButton, ImageButton infoButton, TextView cameraText,
                          View.OnClickListener onClickListener) {
        mActivityParent = activityParent;
        mViewInterface = viewInterface;
        mCaptureButton = captureButton;
        mRecordButton = recordButton;
        mInfoButton = infoButton;
        mCameraText = cameraText;
        mUsbDevice = null;

        mViewInterface.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mViewInterface).setOnClickListener(onClickListener);
        mCaptureButton.setOnClickListener(onClickListener);
        mRecordButton.setOnClickListener(onClickListener);
        mInfoButton.setOnClickListener(onClickListener);

        mHandler = UVCCameraHandler.createHandler(mActivityParent, mViewInterface,
                //####@: UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);
                1, ComDef.CAMERA_PREVIEW_WIDTH, ComDef.CAMERA_PREVIEW_HEIGHT, ComDef.CAMERA_FRAME_FORMAT, BANDWIDTH_FACTORS[0]);

        setCameraView();
    }

    public static final int CAMERA_VIEW_ACTION_OPEN_LIST = 1;

    public int onClickCameraView(final View view) {
        if (view == mViewInterface) {
            if (mHandler != null) {
                if (!mHandler.isOpened()) {
                    if (mUsbControlBlock == null) {
                        //open camera with dialog
                        return CAMERA_VIEW_ACTION_OPEN_LIST;
                    } else {
                        openCamera();
                    }
                } else {
                    mHandler.close();
                    setCameraView();
                }
            }
            return 2;
        }
        else if (view == mCaptureButton) {
            if (mHandler != null) {
                if (mHandler.isOpened()) {
                    //capture still image
                    String picName = mViewID + "_cap" + System.currentTimeMillis() + ".jpg";
                    String picPath = "/sdcard/multicam/" + picName;
                    mHandler.captureStill(picPath);  //just copy the image from screen preview
                    Toast.makeText(mActivityParent, "Capture picture to file " + picPath, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mActivityParent, "Camera closed, press view to open", Toast.LENGTH_LONG).show();
                }
            }
            return 3;
        }
        else if (view == mRecordButton) {
            if (mHandler != null) {
                if (mHandler.isOpened()) {
                    //start/stop recording
                    if (!mHandler.isRecording()) {
                        mRecordButton.setColorFilter(0xffff0000);  // turn red
                        mHandler.startRecording();
                    } else {
                        mRecordButton.setColorFilter(0);  //return to default color
                        mHandler.stopRecording();
                    }
                }
            }
            return 4;
        }
        else if (view == mInfoButton) {
            //Toast.makeText(mActivityParent, "click on info button", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivityParent);
            builder.setMessage(getCameraInfoFull());
            builder.setPositiveButton("OK", null);
            builder.show();
            return 5;
        }
        else {
            return 0;
        }
    }

    public void setCameraView() {
        mActivityParent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaptureButton.setVisibility(View.VISIBLE);
                mRecordButton.setVisibility(View.VISIBLE);
                mInfoButton.setVisibility(View.VISIBLE);
                /* ###@:
                if (mHandler.isOpened()) {
                    mCaptureButton.setVisibility(View.VISIBLE);
                    mRecordButton.setVisibility(View.VISIBLE);
                    mInfoButton.setVisibility(View.VISIBLE);
                }
                else {
                    mCaptureButton.setVisibility(View.INVISIBLE);
                    mRecordButton.setVisibility(View.INVISIBLE);
                    mInfoButton.setVisibility(View.INVISIBLE);
                }
                //*/
                mCameraText.setText(getCameraInfo());
            }
        });
    }

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

    private String getCameraInfo() {
        String text = " " + mViewID;
        String textLog = "" + mViewID;
        if (mUsbDevice != null) {
            text += ": " + mUsbDevice.getDeviceId() + " " + mUsbDevice.getProductName();
            textLog += ": " + mUsbDevice.getDeviceId() + " " + mUsbDevice.getProductName();
            if (mHandler != null) {
                int fps = mHandler.readFrameCount() / 2;  // note: can only get once!
                long dps = mHandler.readTotalSize() / 2;   // note: can only get once!
                text += "\n " + mHandler.getWidth() + "x" + mHandler.getHeight();
                text += "\n " + fps;
                text += "\n " + dps;
                textLog += " " + mHandler.getWidth() + "x" + mHandler.getHeight();
                textLog += " " + fps;
                textLog += " " + dps;
            }
            else {
                text += " Err No Handler";
                textLog += "Err No Handler";
                LogUtils.e("ERROR: No camera handler for the usb device");
            }
        }
        else {
            text += " No Device Attached";
            textLog += " No Device Attached";
        }
        logd(textLog);
        return text;
    }

    private String getCameraInfoFull() {
        String text = "VIEW #" + mViewID;
        if (mUsbDevice != null) {
            text += ": " + mUsbDevice.getDeviceId() + " " + mUsbDevice.getProductName();
            if (mHandler != null) {
                text += "\n*Current Preview: " + mHandler.getWidth() + "x" + mHandler.getHeight();

                // append supported preview size list
                text += "\n*Supported Preview Size: ";
                List<Size> sizes = mHandler.getSupportedPreviewSizes();
                if (sizes != null) {
                    text += "\n  ";
                    for (Size size : sizes) {
                        text += size.width + "x" + size.height + " ";
                    }
                } else {
                    text += "\n ERR No Sizes";
                }

                // append video format
                text += "\n*Preview Mode: " + mHandler.getPreviewModeString();
            }
            else {
                text += "\n Err No Handler";
                LogUtils.e("ERROR: No camera handler for the usb device");
            }
        }
        text += "\n";
        return text;
    }

    public void onStart() {
        showMsg("onStart");
        mViewInterface.onResume();
    }

    public void onStop() {
        showMsg("onStop");
        //###@: mViewInterface.onPause();
    }

    public void onDestroy() {
        showMsg("onDestroy");
		mViewInterface = null;
        mCaptureButton = null;
        mRecordButton = null;
        mInfoButton = null;
        mCameraText = null;
        mActivityParent = null;
    }

    private String getUsbDeviceInfo(UsbDevice device) {
        return device.getDeviceId() + " " + device.getProductName();
    }

    public void openCamera() {
        //###@: take first two view as face recog camera, others as vending camera
        //only face camera open always
        //vending camera open on timer
        if (!mViewID.equals("11") && !mViewID.equals("12")) {
            showMsg("openCamera: Not open vending camera(open on timer)");
            return;
        }

        if (mHandler != null && mUsbControlBlock != null) {
            showMsg("TIME@: Open device: " + getUsbDeviceInfo(mUsbDevice));
            mHandler.open(mUsbControlBlock);
            if (ComDef.OPEN_CAMERA_PREVIEW) {
                mHandler.startPreview(new Surface(mViewInterface.getSurfaceTexture()));
            } else {
                SurfaceTexture surfaceTexture = null;
                if (null == surfaceTexture) {
                    int oesTextureId = GlUtil.generateTexture(36198);
                    surfaceTexture = new SurfaceTexture(oesTextureId);
                }
                mHandler.startPreview(new Surface(surfaceTexture));
            }
            setCameraView();
        }
    }

    public void closeCamera() {
        if (mHandler != null) {
            mHandler.close();
            setCameraView();
        }
    }

    public void captureOnce() {
        //###@: take first two view as face recog camera, others as vending camera
        //only face camera open always
        //vending camera open on timer
        if (mViewID.equals("11") || mViewID.equals("12")) {
            showMsg("captureOnce: Not open face camera");
            return;
        }

        if (mHandler == null) {
            showMsg("captureOnce: No Handler");
            return;
        }

        if (mUsbControlBlock == null) {
            showMsg("captureOnce: No ControlBlock");
            return;
        }

        showMsg("TIME@: captureOnce: " + getUsbDeviceInfo(mUsbDevice));
        mHandler.open(mUsbControlBlock);
        if (ComDef.OPEN_CAMERA_PREVIEW) {
            mHandler.startPreview(new Surface(mViewInterface.getSurfaceTexture()));
        } else {
            SurfaceTexture surfaceTexture = null;
            if (null == surfaceTexture) {
                int oesTextureId = GlUtil.generateTexture(36198);
                surfaceTexture = new SurfaceTexture(oesTextureId);
            }
            mHandler.startPreview(new Surface(surfaceTexture));
        }
    }

    public boolean onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
        String devInfo = getUsbDeviceInfo(device);
        showMsg("onConnect: " + devInfo);
        if (mHandler.isOpened()) {
            showMsg("Already Opened with device: " + mUsbDevice.getDeviceId());
            return false;
        }
        else {
            mUsbDevice = device;
            mUsbControlBlock = ctrlBlock;

            /*###@:
            {
                showMsg("TIME@: Open device: " + devInfo);
                mHandler.open(ctrlBlock);
                if (OPEN_CAMERA_PREVIEW) {
                    mHandler.startPreview(new Surface(mViewInterface.getSurfaceTexture()));
                } else {
                    SurfaceTexture surfaceTexture = null;
                    if (null == surfaceTexture) {
                        int oesTextureId = GlUtil.generateTexture(36198);
                        surfaceTexture = new SurfaceTexture(oesTextureId);
                    }
                    mHandler.startPreview(new Surface(surfaceTexture));
                }
            }
            //*/

            setCameraView();
            return true;
        }
    }

    public boolean onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
        String devInfo = device.getDeviceId() + "\n" + device.getProductName();
        showMsg("onDisconnect: " + devInfo);
        if (mUsbDevice == null) {
            showMsg("onDisconnect: No device");
            return false;
        }
        else if (mUsbDevice.getDeviceId() != device.getDeviceId()) {
            showMsg("onDisconnect: i am " + mUsbDevice.getDeviceId() + ", not " + device.getDeviceId());
            return false;
        }
        else {
            //####@:
//            showMsg("onDisconnect: disconnect from device " + mUsbDevice.getDeviceId());
//            mActivityParent.queueEvent(new Runnable() {
//                @Override
//                public void run() {
//                    mHandler.close();
//                    if (mPreviewSurface != null) {
//                        mPreviewSurface.release();
//                        mPreviewSurface = null;
//                    }
//                    mUsbDevice = null;
//                    setCameraView();
//                }
//            }, 0);
            return true;
        }
    }

    public boolean isCameraOpened() {
        return mHandler != null && mHandler.isOpened();
    }

    public boolean isMyDevice(UsbDevice device) {
        return mUsbDevice != null && device.getDeviceId() == mUsbDevice.getDeviceId();
    }

    public String getViewID() {
        return mViewID;
    }
}
