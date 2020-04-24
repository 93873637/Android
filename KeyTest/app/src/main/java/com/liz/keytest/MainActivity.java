package com.liz.keytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button mBtnWhite, mBtnStopWhite, mBtnExit;

    private final static String TAG = MainActivity.class.getSimpleName();
    private Intent whiteIntent;

    SettingsContentObserver mSettingsContentObserver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnWhite = findViewById(R.id.btn_white);
        mBtnStopWhite = findViewById(R.id.btn_stop_white);
        mBtnExit = findViewById(R.id.btn_exit);
        setListener();
        //###@:MonitorView.showWindow(this);
        //registerVolumeChangeReceiver();
        PowerSaveMonitorService.start(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //使用ContentProvide 监听数据库变化，来监听音量键变化
    //缺点:
    //1. 触发慢，设置过去需要等待约1.5秒才触发
    //2. 只有音量变化了才能触发，这就意味着音量到最大/最小值后再按vol+/vol-键不触发

    private void registerVolumeChangeReceiver() {
        mSettingsContentObserver = new SettingsContentObserver(this, null);
        this.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    private void unregisterVolumeChangeReceiver(){
        this.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    public class SettingsContentObserver extends ContentObserver {
        private int oldMediaVolume;


        public SettingsContentObserver(Context c, Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.d("###@:", "onChange");
//            if (mAudioUtil != null && isMediaVolumePowerSaveSettings) {
//                int currAudioVolume = mAudioUtil.getMediaVolume();
//
//                if (oldMediaVolume == currAudioVolume) {
//                    return;
//                }
//
//                oldMediaVolume = currAudioVolume;
//                Log.d(TAG, "onChange currMediaVolume = " + currAudioVolume);
//
//                if (currAudioVolume == mAudioUtil.getMaxMediaVolume()) {
//                    return;
//                }
//
//                if (AUDIO_ADJ - currAudioVolume > 0) {
//                    Log.d(TAG, "Down volume key resume");
//
//                }
//
//            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class ExitReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.finish();
        }
    }

    private void setListener(){
        OnClick onClick = new OnClick();
        mBtnWhite.setOnClickListener(onClick);
        mBtnStopWhite.setOnClickListener(onClick);
        mBtnExit.setOnClickListener(onClick);
    }

    private class  OnClick implements  View.OnClickListener{
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch(viewId){
                case R.id.btn_white:
                    Log.i(TAG, "MAIN: btn_white");
                    if(null == whiteIntent)
                        whiteIntent = new Intent(MainActivity.this, WhiteService.class);
                    startService(whiteIntent);
                    break;
                case R.id.btn_stop_white:
                    Log.i(TAG, "MAIN: btn_stop_white");
                    if(null != whiteIntent)
                        stopService(whiteIntent);
                    break;
                case R.id.btn_exit:
                    Log.i(TAG, "MAIN: btn_exit");
                    if(null != whiteIntent)
                        stopService(whiteIntent);
                    finish();
                    break;
            }
        }
    }

}
