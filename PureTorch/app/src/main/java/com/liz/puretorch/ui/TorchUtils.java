package com.liz.puretorch.ui;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.liz.puretorch.utils.LogUtils;

/**
 * TorchUtils.java
 * Created by liz on 2018/10/1.
 */

public class TorchUtils {

    private static boolean mTorchEnabled = false;

    public static void enableTorch(Context context, boolean enable) {
        mTorchEnabled = enable;
        //Openshoudian(context);
//        try {
//            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//            if (manager == null) {
//                LogUtils.e("TorchUtils: get camera service failed");
//            }
//            else {
//                manager.setTorchMode("0", mTorchEnabled);
//            }
//        }
//        catch (Exception ex) {
//            LogUtils.e("TorchUtils: open torch exception: " + ex.toString());
//        }
    }

    public static void Openshoudian(Context context) {
        Camera camera=null;
        //异常处理一定要加，否则Camera打开失败的话程序会崩溃
        try {
            Log.d("smile","camera打开");
            camera = Camera.open();
        } catch (Exception e) {
            Log.d("smile","Camera打开有问题");
            Toast.makeText(context, "Camera被占用，请先关闭", Toast.LENGTH_SHORT).show();
        }

        if(camera != null)
        {
            //打开闪光灯
            camera.startPreview();
            Camera.Parameters parameter = camera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameter);
            Log.d("smile","闪光灯打开");


        }
    }

    public static void switchTorch(Context context) {
        mTorchEnabled = !mTorchEnabled;
        enableTorch(context, mTorchEnabled);
    }

    public static boolean isTorchEnabled() {
        return mTorchEnabled;
    }

    public static int getSystemBrightness(Context context) {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    public static void changeAppBrightness(Activity activity, int brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }

    /*before android api 23
    private Camera camera;
    {
                    /*
                    camera = Camera.open();
                    Camera.Parameters params = camera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
                    camera.startPreview();
                    //*/

    //}
    /*
    {

                    /*
                    camera.startPreview();
                    Camera.Parameters param = camera.getParameters();
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(param);
                    //*/
    //}
}
