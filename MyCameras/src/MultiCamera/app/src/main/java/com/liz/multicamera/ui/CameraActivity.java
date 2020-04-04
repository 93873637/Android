package com.liz.multicamera.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.androidutils.BitmapUtils;
import com.liz.androidutils.LogUtils;
import com.liz.multicamera.R;
import com.liz.multicamera.album.AlbumHelper;
import com.liz.multicamera.album.ImageItem;
import com.liz.multicamera.app.ThisApp;
import com.liz.multicamera.camera.CameraManager;
import com.liz.multicamera.logic.ComDef;
import com.liz.multicamera.logic.DataLogic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private TextView mProgressInfo;
    private TextView mCameraInfo;
    private ImageView mPictureView;
    private ImageView mNightScene;
    private ResizableSurfaceView mSurfaceView;
    AlbumHelper helper;

    private int shot_count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_camera);

        mProgressInfo = findViewById(R.id.tv_progress_info);
        mProgressInfo.setText(DataLogic.getProgressInfo());

        mCameraInfo = findViewById(R.id.tv_camera_info);
        mCameraInfo.setText(DataLogic.getCameraInfo());

        mPictureView = findViewById(R.id.picture_view);
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());
        List<ImageItem> list = helper.getImagesList();
        if (list != null && list.size() != 0) {
            mPictureView.setImageBitmap(BitmapUtils.createCaptureBitmap(list.get(0).imagePath));
            mPictureView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mPictureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open system album
                    Intent intent = new Intent(Intent.ACTION_DEFAULT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivity(intent);
                }
            });
        }

        mNightScene = findViewById(R.id.scene_mode_night);
        mNightScene.setImageResource(DataLogic.isNightMode()?R.drawable.ic_scene_mode_night_on:R.drawable.ic_scene_mode_night_off);
        mNightScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataLogic.switchNightMode();
                mNightScene.setImageResource(DataLogic.isNightMode()?R.drawable.ic_scene_mode_night_on:R.drawable.ic_scene_mode_night_off);
                mProgressInfo.setText(DataLogic.getProgressInfo());
            }
        });

        mSurfaceView = findViewById(R.id.surface_camera);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);

        ShutterButton shutterButton = findViewById(R.id.shutter_button);
        shutterButton.setOnClickListener(this);

        DataLogic.mUpdateUiCallback = new DataLogic.UpdateUiCallback() {
            @Override
            public void onPictureTaken(final byte[] jpegData, final CameraManager.CameraProxy camera, final String saveFileName) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        LogUtils.e("onPictureTaken: jpegData size = " + jpegData.length);
                        mProgressInfo.setText(DataLogic.getProgressInfo());
                        if (TextUtils.isEmpty(saveFileName)) {
                            LogUtils.e("CameraActivity.onPictureTaken: no image file saved.");
                        }
                        else {
                            mPictureView.setImageBitmap(BitmapUtils.createCaptureBitmap(saveFileName));
                        }
                    }
                });
            }
        };

        startUiTimer();
        EnumDeviceCameras(CameraActivity.this);
    }

    private void EnumDeviceCameras(@org.jetbrains.annotations.NotNull Context context) {
       // CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

//        CameraManager manager = new CameraManager(this); //context.getSystemService(Context.CAMERA_SERVICE);
//
//
//                // Get all front, back and external cameras in 3 separate lists
//                val cameraIds = cameraManager.cameraIdList
//
//
//                val backCameras = filterCameraIdsFacing(
//                cameraIds, cameraManager, CameraMetadata.LENS_FACING_BACK)
//                val frontCameras = filterCameraIdsFacing(
//                cameraIds, cameraManager, CameraMetadata.LENS_FACING_FRONT)
//                val externalCameras = filterCameraIdsFacing(
//                cameraIds, cameraManager, CameraMetadata.LENS_FACING_EXTERNAL)
//
//                // The recommended order of iteration is: all external, first back, first front
//                val allCameras = (externalCameras + listOf(
//                backCameras.firstOrNull(), frontCameras.firstOrNull())).filterNotNull()
//
//                // Get the index of the currently selected camera in the list
//                val cameraIndex = allCameras.indexOf(currCameraId)
//
//        // The selected camera may not be on the list, for example it could be an
//        // external camera that has been removed by the user
//        return if (cameraIndex == -1) {
//            // Return the first camera from the list
//            allCameras.getOrNull(0)
//        } else {
//            // Return the next camera from the list, wrap around if necessary
//            allCameras.getOrNull((cameraIndex + 1) % allCameras.size)
//        }
//    }

    }

    private int mSurfaceWidth = ComDef.SURFACE_SIZE_WIDTH;
    private int mSurfaceHeight = ComDef.SURFACE_SIZE_HEIGHT;

    private void startUiTimer() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, ComDef.UI_TIMER_DELAY, ComDef.UI_TIMER_PERIOD);
    }

    private void updateUI() {
        //LogUtils.d("CameraActivity.updateUI");
        mCameraInfo.setText(DataLogic.getCameraInfo());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.d("CameraActivity.surfaceCreated");
        mSurfaceView.resize(mSurfaceWidth, mSurfaceHeight);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        LogUtils.d("CameraActivity.surfaceChanged: w=" + w + ", h=" + h + ", format=" + format);
        DataLogic.setPreview(this, holder);
        DataLogic.startPreview();

        //####@:
        DataLogic.onShutter();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.d("CameraActivity.surfaceDestroyed");
        DataLogic.stopPreview();
        DataLogic.closeCamera();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        mCamera.stopPreview();
//        mCamera.release();
//        finish();
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shutter_button:
                DataLogic.onShutter();
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        ThisApp.exit();
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            //onTestFail();
//            return true;
//        }
//        return false;
//    }
}
