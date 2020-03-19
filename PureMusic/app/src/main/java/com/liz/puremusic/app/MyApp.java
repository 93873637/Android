package com.liz.puremusic.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.liz.androidutils.LogUtils;
import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.ui.PlayNotifier;

/**
 * MyApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class MyApp extends Application {
    private static MyApp mAppInst;
    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;
        mAppVersion = "";
        mAppVersionShow = "";

        LogUtils.setTag(ComDef.APP_NAME);
        DataLogic.init();
        PureMusicReceiver.init(this);
        PlayNotifier.onCreate(this);

        //##@: just for test only
        /*
        {
            for (int i=0; i<100; i++) {
                Random random = new Random();
                int mPlayListSize = 23;
                int mCurrentPos = Math.abs(random.nextInt()) % mPlayListSize;
                LogUtils.d("mCurrentPos=" + mCurrentPos);
            }
        }
        //*/
        /*
        String src = "hello world!";
        //System.out.println("sim=" + SimilarityUtils.similarity(src, tar));
        String res = StrUtils.getMaxSubString("66 Revelation (12).mp3", "66 Revelation (5).mp3");
        String tar = "hello";
        //*/

        /*
        {
            String a = "66 Revelation (12).mp3";
            String b = "66 Revelation (5).mp3";
            String[] diff = StrUtils.getDiff(a, b);
            String diff_a = diff[0].trim();
            String diff_b = diff[1].trim();
            LogUtils.d("diffa=" + diff_a);
            LogUtils.d("diff_b=" + diff_b);
        }
        {
            String a = "feefs.mp3";
            String b = "66 Revelation (5).mp3";
            String[] diff = StrUtils.getDiff(a, b);
            String diff_a = diff[0].trim();
            String diff_b = diff[1].trim();
            LogUtils.d("diffa=" + diff_a);
            LogUtils.d("diff_b=" + diff_b);
        }
        //*/
        /*
        {
            String a = "66 Revelation (12).mp3";
            Pattern pattern1 = Pattern.compile(".*?\\((.*?)\\).*?");
            Matcher matcher1 = pattern1.matcher(a);
            if (matcher1.matches()) {
                String findStr = matcher1.group(1);
                System.out.println(matcher1.group(1));
            }
        }
        {
            String a = "66 Revelation (5).mp3";
            Pattern pattern1 = Pattern.compile(".*?\\((.*?)\\).*?");
            Matcher matcher1 = pattern1.matcher(a);
            if (matcher1.matches()) {
                String findStr = matcher1.group(1);
                System.out.println(matcher1.group(1));
            }
        }
        //*/
        /*
        String dur = MediaUtils.getMediaDuration("/sdcard/0.sd/Music/music.mp3");
        LogUtils.d("dur=" + dur);
        //*/
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static void onExitApp() {
        LogUtils.d("onExitApp");
        PureMusicReceiver.release(getAppContext());
        PlayNotifier.onDestory(getAppContext());
        int pid = android.os.Process.myPid();
        LogUtils.d("onExitApp: pid=" + pid);
        android.os.Process.killProcess(pid);
    }

    @Override
    public void onTerminate() {
        LogUtils.d("onTerminate");
        PureMusicReceiver.release(this);
        PlayNotifier.onDestory(this);
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        LogUtils.d("onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtils.d("onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtils.d("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
}
