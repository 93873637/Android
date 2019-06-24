package com.liz.testcamera.camera;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.liz.testcamera.app.ThisApp;
import com.liz.testcamera.utils.LogUtils;

import java.util.Calendar;

public class SensorControler implements SensorEventListener {

    public static final int DELEY_DURATION = 500;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_STATIC = 1;
    public static final int STATUS_MOVE = 2;

    public static double MOVING_DISTANCE_THRESHOLD = 1.5;

    public static final String TAG = "SensorControler";
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int mX, mY, mZ;
    private long lastStaticStamp = 0;
    Calendar mCalendar;

    boolean isFocusing = false;
    boolean canFocusIn = false;  //内部是否能够对焦控制机制
    boolean canFocus = false;

    private int mStatus = STATUS_NONE;
    private int foucsing = 1;  //1 表示没有被锁定 0表示被锁定

    private static SensorControler mSensorInstance;
    public static SensorControler getInstance() {
        if (mSensorInstance == null) {
            mSensorInstance = new SensorControler();
        }
        return mSensorInstance;
    }

    private SensorControler() {
        mSensorManager = (SensorManager) ThisApp.getAppContext().getSystemService(Activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  //TYPE_GRAVITY
    }

    private onSensorChangedCallback mSensorChangedCallback;
    public void setOnSensorChangedCallback(onSensorChangedCallback mSensorChangedCallback) {
        this.mSensorChangedCallback = mSensorChangedCallback;
    }
    public interface onSensorChangedCallback {
        void onPositionChanged();
    }

    public void startSensor() {
        restParams();
        canFocus = true;
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSensor() {
        mSensorManager.unregisterListener(this, mSensor);
        canFocus = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //LogUtils.d("onSensorChanged: event=" + event.toString());
        if (event.sensor == null) {
            LogUtils.d("onSensorChanged: event sensor null");
            return;
        }

        if (isFocusing) {
            LogUtils.d("onSensorChanged: isFocusing");
            restParams();
            return;
        }

        //LogUtils.d("onSensorChanged: type=" + event.sensor.getType());
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            LogUtils.d("onSensorChanged: unhandled sensor type " + event.sensor.getType());
        }
        else {
            //LogUtils.d("onSensorChanged: TYPE_ACCELEROMETER");
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            
            mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis();
            if (mStatus == STATUS_NONE) {
                lastStaticStamp = stamp;
                mStatus = STATUS_STATIC;
            }
            else {
                int dx = Math.abs(mX - x);
                int dy = Math.abs(mY - y);
                int dz = Math.abs(mZ - z);
                //LogUtils.d("onSensorChanged: TYPE_ACCELEROMETER: dx:" + dx + ", dy:" + dy + ", dz:" + dz + ", stamp:" + stamp);
                double value = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (value >= MOVING_DISTANCE_THRESHOLD) {
                    LogUtils.d("onSensorChanged: TYPE_ACCELEROMETER: moving on");
                    mStatus = STATUS_MOVE;
                }
                else {
                    //LogUtils.d("onSensorChanged: TYPE_ACCELEROMETER: moving static");
                    if (mStatus == STATUS_MOVE) {
                        //record last time moving time point
                        lastStaticStamp = stamp;
                        canFocusIn = true;
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELEY_DURATION) {
                            if (!isFocusing) {
                                LogUtils.d("stay a while after moving, detect device position changed");
                                canFocusIn = false;
                                if (mSensorChangedCallback != null) {
                                    mSensorChangedCallback.onPositionChanged();
                                }
                            }
                        }
                    }

                    mStatus = STATUS_STATIC;
                }
            }

            mX = x;
            mY = y;
            mZ = z;
        }
    }

    private void restParams() {
        mStatus = STATUS_NONE;
        canFocusIn = false;
        mX = 0;
        mY = 0;
        mZ = 0;
    }

    public boolean isFocusLocked() {
        if(canFocus) {
            return foucsing <= 0;
        }
        return false;
    }

    public void lockFocus() {
        isFocusing = true;
        foucsing--;
        Log.i(TAG, "lockFocus");
    }

    public void unlockFocus() {
        isFocusing = false;
        foucsing++;
        Log.i(TAG, "unlockFocus");
    }

    public void restFoucs() {
        foucsing = 1;
    }
}
