package com.liz.testcamera.logic;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.liz.testcamera.app.ThisApp;
import com.liz.testcamera.camera.CameraManager;
import com.liz.testcamera.camera.CameraManagerFactory;
import com.liz.testcamera.camera.CameraSettings;
import com.liz.testcamera.camera.Exif;
import com.liz.testcamera.camera.SensorControler;
import com.liz.testcamera.camera.Storage;
import com.liz.testcamera.exif.ExifInterface;
import com.liz.testcamera.utils.LogUtils;

import org.codeaurora.snapcam.wrapper.ParametersWrapper;

import java.io.File;
import java.text.DecimalFormat;

import static com.liz.testcamera.camera.CameraSettings.KEY_AUTO_ISO;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic extends ComDef {

    private static CameraManager.CameraProxy mCameraDevice;
    public static int mCameraLensId = ComDef.CAMERA_ID_DEFAULT;

    private static int mCameraStatus;
    private static int mCameraMode;
    private static int mTotalPictureTaken;
    private static int mPictureSaved;

    private static Activity mActivity;
    private static SurfaceHolder mSurfaceHolder;

    private static final ShutterCallback mShutterCallback = new ShutterCallback();
    private static final RawPictureCallback mRawPictureCallback = new RawPictureCallback();
    private static final PostViewPictureCallback mPostViewPictureCallback = new PostViewPictureCallback();
    private static final JpegPictureCallback mJpegPictureCallback = new JpegPictureCallback();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INTERFACES

    public static void init() {
        LogUtils.d("DataLogic.init");
        initCamera();
    }

    private static void initCamera() {
        LogUtils.d("DataLogic.initCamera");
        mCameraLensId = ComDef.CAMERA_ID_DEFAULT;
        mCameraStatus = ComDef.CAMERA_STATUS_IDLE;
        mCameraMode = ComDef.CAMERA_MODE_NORMAL;
        mTotalPictureTaken = 0;
        mPictureSaved = 0;

        //reset night mode shooting params in case exception
        NightMode.onStopShooting();

        SensorControler.getInstance().setOnSensorChangedCallback(new SensorControler.onSensorChangedCallback() {
            @Override
            public void onPositionChanged() {
                //need re-focus camera
                autoFocus();
            }
        });

        /*
        int  mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        Camera.CameraInfo  mInfo[] = new Camera.CameraInfo[mNumberOfCameras];
            for (int i = 0; i < mNumberOfCameras; i++) {
                mInfo[i] = new Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
            }
        int mBackCameraId = -1;
            int mFrontCameraId = -1;
        // get the first (smallest) back and first front camera id
        for (int i = 0; i < mNumberOfCameras; i++) {
            if (mBackCameraId == -1 && mInfo[i].facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            } else if (mFrontCameraId == -1 && mInfo[i].facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
        }
        //*/
    }

    public static boolean isNightMode() {
        return mCameraMode == ComDef.CAMERA_MODE_NIGHT;
    }

    public static void switchNightMode() {
        if (mCameraMode == ComDef.CAMERA_MODE_NIGHT) {
            mCameraMode = ComDef.CAMERA_MODE_NORMAL;
        }
        else {
            mCameraMode = ComDef.CAMERA_MODE_NIGHT;
            NightMode.init();
        }
    }

    public static boolean isShooting() {
        return mCameraStatus == ComDef.CAMERA_STATUS_SHOOTING;
    }

    public static int getStatus() {
        return mCameraStatus;
    }

    public static void setStatus(int status) {
        mCameraStatus = status;
    }

    public static String getProgressInfo() {
        if (DataLogic.isNightMode()) {
            return NightMode.getProgressInfo(isShooting());
        }
        else {
            return mPictureSaved + "/" + mTotalPictureTaken;
        }
    }

    public static boolean openCamera() {
        LogUtils.d("DataLogic.openCamera: mCameraDevice=" + mCameraDevice + ", mCameraLensId=" + mCameraLensId);
        if (mCameraDevice == null) {
            mCameraDevice = CameraManagerFactory
                    .getAndroidCameraManager().cameraOpen(null, mCameraLensId, null);
        }
        return (mCameraDevice != null);
    }

    public static void closeCamera() {
        LogUtils.d("DataLogic.closeCamera: mCameraDevice=" + mCameraDevice);
        if (mCameraDevice != null) {
            mCameraDevice.getCamera().release();
            mCameraDevice = null;
        }
    }

    public static void setPreview(Activity activity, SurfaceHolder holder) {
        mActivity = activity;
        mSurfaceHolder = holder;
    }

    public static void startPreview() {
        LogUtils.d("DataLogic.startPreview");

        if (!openCamera()) {
            LogUtils.d("DataLogic.startPreview: open camera failed.");
            return;
        }

        try {
            setCameraDisplayOrientation(mActivity);

            Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
            Camera.Size prevSize = parameters.getPreviewSize();
            Camera.Size picSize = parameters.getPictureSize();
            LogUtils.d("startPreview: prevSize(w,h)=(" + prevSize.width + ", " + prevSize.height + ")");
            LogUtils.d("startPreview: picSize(w,h)=(" + picSize.width + ", " + picSize.height + ")");
            parameters.setPreviewSize(ComDef.PREVIEW_SIZE_WIDTH, ComDef.PREVIEW_SIZE_HEIGHT);
            parameters.setPictureSize(ComDef.PICTURE_SIZE_WIDTH, ComDef.PICTURE_SIZE_HEIGHT);

            ParametersWrapper.setZSLMode(parameters, "on");
            ParametersWrapper.setISOValue(parameters, CameraSettings.KEY_AUTO_ISO);
            parameters.set(CameraSettings.KEY_EXPOSURE_TIME, "0");
            mCameraDevice.getCamera().setParameters(parameters);

            mSurfaceHolder.setFixedSize(ComDef.PREVIEW_SIZE_WIDTH, ComDef.PREVIEW_SIZE_HEIGHT);
            mCameraDevice.getCamera().setPreviewDisplay(mSurfaceHolder);

            mCameraDevice.getCamera().startPreview();

            autoFocus();
            SensorControler.getInstance().startSensor();
        } catch (Exception e) {
            LogUtils.e("ERROR: startPreview exception: " + e.toString());
        }
    }

    public static void stopPreview() {
        LogUtils.d("DataLogic.stopPreview");
        try {
            mCameraDevice.getCamera().stopPreview();
            SensorControler.getInstance().stopSensor();
        } catch (Exception e) {
            LogUtils.e("ERROR: stopPreview exception: " + e.toString());
        }
    }

    public static void onShutter() {
        LogUtils.d("DataLogic.onShutter");
        if (getStatus() != CAMERA_STATUS_IDLE) {
            LogUtils.i("DataLogic.onShutter: can't start capture in status " + getStatus());
        }
        else {
            setStatus(CAMERA_STATUS_SHOOTING);

            if (DataLogic.isNightMode()) {
                NightMode.onStartShooting();
                setManualExposure();
                setupExposureParams();
            }

            //first time we must call auto focus to take picture
            autoFocus();
        }
    }

    public static void onPictureTaken() {
        LogUtils.d("DataLogic.onPictureTaken");

        if (!DataLogic.isNightMode()) {
            setStatus(CAMERA_STATUS_IDLE);

//            //######@:
//            closeCamera();
//            mCameraLensId = CAMERA_ID_BACK_SUB;
//            openCamera();
//            startPreview();
//            doCapture();
        }
        else {
//        //check if parameter changed
//        Camera.Parameters parametersc = mCameraDevice.getCamera().getParameters();
//        final String currentISO = parametersc.get(CameraSettings.KEY_CURRENT_ISO);
//        final String currentExpTime = parametersc.get(CameraSettings.KEY_CURRENT_EXPOSURE_TIME);
//        LogUtils.d("currentISO=" + currentISO + ", currentExpTime=" + currentExpTime
//                + ", shouldbe: currentISO=" + NightMode.getCurrentISO() + ", currentExpTime=" + NightMode.getCurrentExposureTime());
            int action = NightMode.getNextAction();
            LogUtils.d("DataLogic.onPictureTaken: action=" + NIGHT_MODE_ACTION_NAME[action]);
            switch (action) {
                case NIGHT_MODE_SHOT_FINISHED:
                case NIGHT_MODE_SHOT_ERROR:
                    LogUtils.d("DataLogic.stopNightModeShooting");
                    NightMode.onStopShooting();
                    setStatus(CAMERA_STATUS_IDLE);
                    setAutoExposure();
                    setAutoFocus();
                    break;
                case NIGHT_MODE_SHOT_CONTINUE:
                    //do nothing just waiting for picture burst over on low layer
                    break;
                case NIGHT_MODE_SHOT_CONTINUE_NEXT_EXPO:
                    doNightCapture();
                    break;
                default:
                    LogUtils.e("ERROR: DataLogic.onPictureTaken: unsupported action " + action);
                    break;
            }
        }
    }

    private static void setAutoExposure() {
        if (mCameraDevice.getCamera() == null) {
            LogUtils.e("DataLogic.setManualExposure: null camera");
            return;
        }
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        ParametersWrapper.setISOValue(parameters, KEY_AUTO_ISO);
        parameters.set(CameraSettings.KEY_EXPOSURE_TIME, "0");
        mCameraDevice.getCamera().setParameters(parameters);
    }

    private static void setManualExposure() {
        if (mCameraDevice.getCamera() == null) {
            LogUtils.e("DataLogic.setManualExposure: null camera");
            return;
        }
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        ParametersWrapper.setISOValue(parameters, CameraSettings.KEY_MANUAL_ISO);
        mCameraDevice.getCamera().setParameters(parameters);
    }

    private static void setAutoFocus() {
        if (mCameraDevice.getCamera() == null) {
            LogUtils.e("DataLogic.setAutoFocus: null camera");
            return;
        }
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCameraDevice.getCamera().setParameters(parameters);
    }

    private static void setFixedFocus() {
        if (mCameraDevice.getCamera() == null) {
            LogUtils.e("DataLogic.setFixedFocus: null camera");
            return;
        }
        mCameraDevice.getCamera().cancelAutoFocus();
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        mCameraDevice.getCamera().setParameters(parameters);
    }

    public static String getCameraInfo() {

        StringBuilder sb = new StringBuilder();
        if (mCameraDevice == null) {
            sb.append("no camera device.");
            return sb.toString();
        }

        if (mCameraDevice.getCamera() == null) {
            sb.append("no camera.");
            return sb.toString();
        }

        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        if (parameters == null) {
            sb.append("no parameters.");
            return sb.toString();
        }

        sb.append(ComDef.CAMERA_STATUS_NAME[DataLogic.getStatus()]);
        sb.append("  ");

        final String isoValue = ParametersWrapper.getISOValue(parameters);  //auto or manual
        final int minISO = parameters.getInt(CameraSettings.KEY_MIN_ISO);
        final int maxISO = parameters.getInt(CameraSettings.KEY_MAX_ISO);
        final String currentISO = parameters.get(CameraSettings.KEY_CURRENT_ISO);
        final String minExpTime = parameters.get(CameraSettings.KEY_MIN_EXPOSURE_TIME);
        final String maxExpTime = parameters.get(CameraSettings.KEY_MAX_EXPOSURE_TIME);
        final String currentExpTime = parameters.get(CameraSettings.KEY_CURRENT_EXPOSURE_TIME);

        sb.append(isoValue);
        sb.append("  ");

        sb.append(currentISO);
        sb.append("(");
        sb.append(minISO);
        sb.append(", ");
        sb.append(maxISO);
        sb.append(")");
        sb.append("  ");

        sb.append(formatExpTime(currentExpTime));
        sb.append("(");
        sb.append(formatExpTime(minExpTime));
        sb.append(", ");
        sb.append(formatExpTime(maxExpTime));
        sb.append(")");

        return sb.toString();
    }

    public static String formatExpTime(String expTimeStr) {
        if (expTimeStr == null) {
            //LogUtils.e("DataLogic.formatExpTime: null exposure time string");
            return  "?";
        }
        //LogUtils.d("DataLogic.formatExpTime: expTimeStr=" + expTimeStr);

        double expTime = Double.parseDouble(expTimeStr);
        if (expTime >= 100) {
            return "" + (int)expTime;
        }
        else if (expTime >= 10) {
            DecimalFormat df = new DecimalFormat("#.0");
            return df.format(expTime);
        }
        else if (expTime >= 1) {
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(expTime);
        }
        else if (expTime > 0){
            DecimalFormat df = new DecimalFormat("#.000");
            return "0" + df.format(expTime);
        }
        else {
            return expTimeStr;
        }
    }

    // INTERFACES
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void setupExposureParams() {
        try {
            Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.set(CameraSettings.KEY_CONTINUOUS_ISO, NightMode.getCurrentISO());

            float expTime = NightMode.getCurrentExposureTime();
            if (expTime > MAX_NORMAL_EXPOSURE_TIME) {
                NightMode.openLongExposureMode(expTime);
            }
            else {
                NightMode.closeLongExposureMode();
                parameters.set(CameraSettings.KEY_EXPOSURE_TIME, "" + NightMode.getCurrentExposureTime());
            }

            mCameraDevice.getCamera().setParameters(parameters);
            LogUtils.d("DataLogic.setupExposureParams: SUCCESS, iso=" + NightMode.getCurrentISO() + ", exp_time=" + NightMode.getCurrentExposureTime());
        }
        catch (RuntimeException ex) {
            LogUtils.e("DataLogic.setupExposureParams: FAILED, iso=" + NightMode.getCurrentISO() + ", exp_time=" + NightMode.getCurrentExposureTime()
                    + ", ex=" + ex.toString());
        }
    }

    private static void autoFocus() {
        LogUtils.d("DataLogic.autoFocus");
        mCameraDevice.getCamera().autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    LogUtils.d("onAutoFocus succeed...");
                    if (getStatus() == CAMERA_STATUS_SHOOTING) {
                        if (DataLogic.isNightMode()) {
                            setFixedFocus();
                            //####@: stopPreview();  //can't call????
                        }
                        doCapture();
                    }
                } else {
                    LogUtils.d("onAutoFocus failed...");
                }
            }
        });
    }

    private static void doNightCapture() {
        //fetch exposure parameter in trun
        setupExposureParams();

        //delay sometime incase exposure parameter not take effect
        new Handler().postDelayed(new Runnable(){
            public void run() {
                doCapture();
            }
        }, TIME_DELAY_FOR_EXPOSURE_CHANGE);
    }

    private static void doCapture() {
        LogUtils.d("DataLogic.doCapture");
        mCameraDevice.takePicture(ThisApp.getMainHandler(),
                mShutterCallback,
                mRawPictureCallback,
                mPostViewPictureCallback,
                mJpegPictureCallback);
    }

    private static void setCameraDisplayOrientation(Activity activity) {
        LogUtils.d("DataLogic.setCameraDisplayOrientation");

        //catch exception in case sub camera
        Camera.CameraInfo info = new Camera.CameraInfo();
        try {
            Camera.getCameraInfo(mCameraLensId, info);
            LogUtils.d("DataLogic.setCameraDisplayOrientation: camera facing=" + info.facing + ", orientation=" +  + info.orientation);
        } catch (Exception e) {
            LogUtils.e("ERROR: setCameraDisplayOrientation exception: " + e.toString());
            return;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        LogUtils.d("DataLogic.setCameraDisplayOrientation: activity rotation=" + rotation);

        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }
        LogUtils.d("DataLogic.setCameraDisplayOrientation: activity degree=" + degree);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }
        else {
            LogUtils.d("DataLogic.setCameraDisplayOrientation: back-facing");
            result = (info.orientation - degree + 360) % 360;
        }

        LogUtils.d("DataLogic.setCameraDisplayOrientation: result=" + result);
        mCameraDevice.getCamera().setDisplayOrientation(result);
    }

    public static void focusOnArea(Point point) {
        LogUtils.d("DataLogic.focusOnArea: point=" + point.x + "," + point.y);
        //###@: it seems not work, to do later...

//        Camera camera = mCameraDevice.getCamera();
//        if (camera == null) {
//            LogUtils.d("DataLogic.focusOnArea: camera null");
//            return false;
//        }
//
//        final int AREA_SIZE = 320;
//        int left = point.x - AREA_SIZE/2;
//        int top = point.y - AREA_SIZE/2;
//        int right = point.x + AREA_SIZE/2;
//        int bottom = point.y + AREA_SIZE/2;
//        List<Camera.Area> areas = new ArrayList<Camera.Area>();
//        areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
//
//        try {
//            Camera.Parameters cameraParameters = camera.getParameters();
//            cameraParameters.setFocusAreas(areas);
//            camera.setParameters(cameraParameters);
//        }
//        catch (Exception e) {
//            LogUtils.e("DataLogic.focusOnArea: set camera area failed with exception: " + e.toString());
//            return false;
//        }
//
//        //autoFocus();
//        return true;
//
////
////        parameters.setFocusAreas(areas);
////            try {
////                mCamera.setParameters(parameters);
////            } catch (Exception e) {
////                // TODO: handle exception
////                e.printStackTrace();
////                return false;
////            }
////        }
////
////
////    mCameraParameters = camera.getParameters();
////
////        mCameraDevice.getCamera().setParameters(mCameraParameters);
////
////        return focus(callback);
    }

    private static final class ShutterCallback implements CameraManager.CameraShutterCallback {
        @Override
        public void onShutter(CameraManager.CameraProxy camera) {
            LogUtils.d("ShutterCallback.onShutter");
        }
    }

    private static final class PostViewPictureCallback implements CameraManager.CameraPictureCallback {
        @Override
        public void onPictureTaken(byte [] data, CameraManager.CameraProxy camera) {
            LogUtils.d("PostViewPictureCallback.onPictureTaken");
        }
    }

    private static final class RawPictureCallback implements CameraManager.CameraPictureCallback {
        @Override
        public void onPictureTaken(byte [] rawData, CameraManager.CameraProxy camera) {
            LogUtils.d("RawPictureCallback.onPictureTaken");
        }
    }

    private static final class JpegPictureCallback implements CameraManager.CameraPictureCallback {
        public static final int GDEPTH_SIZE = 1280 * 960;
        byte[] mBokeh;
        byte[] mOrigin;
        byte[] mDepth;
        int mCallTime = 0;

        @Override
        public void onPictureTaken(byte [] jpegData, CameraManager.CameraProxy camera) {
            LogUtils.d("JpegPictureCallback.onPictureTaken");

            mTotalPictureTaken ++;
            String fileName = saveImage(jpegData);
            if (!TextUtils.isEmpty(fileName)) {
                mPictureSaved ++;
            }
            mUpdateUiCallback.onPictureTaken(jpegData, camera, fileName);

            //to do if there is next action
            DataLogic.onPictureTaken();
        }
    }

    public static UpdateUiCallback mUpdateUiCallback = null;
    public interface UpdateUiCallback {
        public void onPictureTaken(final byte [] jpegData, final CameraManager.CameraProxy camera, final String saveFileName);
    }

    public static String saveImage(byte [] jpegData) {
        LogUtils.d("Storage.saveImage");

        ExifInterface exif = Exif.getExif(jpegData);
        LogUtils.d("Storage.saveImage: get exif=" + exif.toString());

        String fileName = Environment.getExternalStorageDirectory().toString()
                + File.separator
                + "DCIM"
                + File.separator
                + "Camera"
                + File.separator
                + genNewImageFileName();
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        if (Storage.writeFile(fileName, jpegData, exif, "jpeg") > 0) {
            return fileName;
        }
        else {
            return "";
        }
    }

    private static final String TIME_STAMP_NAME = "yyyyMMdd_HHmmss";

    private static String genNewImageFileName() {
        StringBuilder sb = new StringBuilder();
        long currentTimeMillis = System.currentTimeMillis();

        sb.append("IMG_");
        //sb.append(new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(currentTimeMillis)));

//        if (DataLogic.isNightMode()) {
//            sb.append("_");
//            sb.append(NightMode.getPictureIndex());
//        }
//        else {
//            sb.append(".");
//            long ms = currentTimeMillis % 1000;
//            if (ms < 10)
//                sb.append("00");
//            else if (ms < 100)
//                sb.append("0");
//            sb.append(ms);
//        }

        sb.append("_");
        sb.append(DataLogic.mCameraLensId==ComDef.CAMERA_ID_BACK_MAIN?"DOWN":"UP");

        sb.append(".jpg");
        return  sb.toString();
    }
}
