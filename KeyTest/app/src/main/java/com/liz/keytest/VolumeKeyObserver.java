package com.liz.keytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Objects;

public class VolumeKeyObserver {
    private String TAG_VOLUME = "VOLUME";
    private Context mContext;
    private IntentFilter mIntentFilter;
    private VolumeKeyObserver.OnVolumeKeyListener mOnVolumeKeyListener;
    private VolumeKeyObserver.VolumeKeyBroadcastReceiver mVolumeKeyBroadcastReceiver;

    public VolumeKeyObserver(Context context) {
        this.mContext = context;
    }

    //注册广播接收者
    public void startListen(){
        //mIntentFilter = new IntentFilter(Intent.ACTION_VOICE_COMMAND);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        mVolumeKeyBroadcastReceiver=new VolumeKeyObserver.VolumeKeyBroadcastReceiver();
        mContext.registerReceiver(mVolumeKeyBroadcastReceiver, mIntentFilter);
        System.out.println("VolumeKey----> 开始监听");
    }

    //取消广播接收者
    public void stopListen(){
        if (mVolumeKeyBroadcastReceiver!=null) {
            mContext.unregisterReceiver(mVolumeKeyBroadcastReceiver);
            System.out.println("VolumeKey----> 停止监听");
        }
    }

    // 对外暴露接口
    public void setVolumeKeyListener(VolumeKeyObserver.OnVolumeKeyListener VolumeKeyListener) {
        mOnVolumeKeyListener = VolumeKeyListener;
    }

    // 回调接口
    public interface OnVolumeKeyListener {
        void onVolumeKeyPressed();
    }

    //广播接收者
    class VolumeKeyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "android.media.VOLUME_CHANGED_ACTION")) {
                Log.i(TAG_VOLUME, "VolumeKey----> 监听到了音量调节");
                System.out.println("VolumeKey----> 监听到了音量调节");
            }
        }
    }
}
