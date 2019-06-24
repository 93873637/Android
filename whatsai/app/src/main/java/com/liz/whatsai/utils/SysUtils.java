package com.liz.whatsai.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.liz.whatsai.app.WhatsaiReceiver;
import com.liz.whatsai.logic.ComDef;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

/**
 * SysUtils.java
 * Created by liz on 2018/3/2.
 */

@SuppressWarnings("unused")
public class SysUtils {

    /*
    //Calendar Usages
    Calendar CD = Calendar.getInstance();
    int YY = CD.get(Calendar.YEAR) ;
    int MM = CD.get(Calendar.MONTH)+1;
    int DD = CD.get(Calendar.DATE);
    int HH = CD.get(Calendar.HOUR);
    int NN = CD.get(Calendar.MINUTE);
    int SS = CD.get(Calendar.SECOND);
    int MI = CD.get(Calendar.MILLISECOND);
    Calendar cal = Calendar.getInstance();
    //当前年
    int year = cal.get(Calendar.YEAR);
    //当前月
    int month = (cal.get(Calendar.MONTH))+1;
    //当前月的第几天：即当前日
    int day_of_month = cal.get(Calendar.DAY_OF_MONTH);
    //当前时：HOUR_OF_DAY-24小时制；HOUR-12小时制
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    //当前分
    int minute = cal.get(Calendar.MINUTE);
    //当前秒
    int second = cal.get(Calendar.SECOND);
    //0-上午；1-下午
    int ampm = cal.get(Calendar.AM_PM);
    //当前年的第几周
    int week_of_year = cal.get(Calendar.WEEK_OF_YEAR);
    //当前月的第几周
    int week_of_month = cal.get(Calendar.WEEK_OF_MONTH);
    //当前年的第几天
    int day_of_year = cal.get(Calendar.DAY_OF_YEAR);
    //*/

    public static void setDailyAlarm(Context context, String tag, int hour, int minute, int second) {
        LogUtils.d("setDailyAlarm \"" + tag + "\" at " + hour + ":" + minute + ":" + second);
        Calendar c = Calendar.getInstance();
        long currentTimeMillis = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        long triggerAtMillis = c.getTimeInMillis();
        LogUtils.d("setDailyAlarm: currentTimeMillis=" + currentTimeMillis + ", triggerAtMillis=" + triggerAtMillis);
        if (triggerAtMillis < currentTimeMillis) {
            triggerAtMillis += 24 * 60 * 60 * 1000;  //delay one day later
        }
        Intent intent = new Intent(context, WhatsaiReceiver.class);
        intent.setAction(ComDef.WHATSAI_ACTION_DAILY_ALARM);
        intent.putExtra(ComDef.ALARM_TAG, tag);
        setElapsedAlarm(context, triggerAtMillis, intent);
    }

    /*
     *  setElapsedAlarm: set alarm from current time by elapsed hour:minute:second
     */
    public static void setElapsedAlarm(Context context, int hour, int minute, int second) {
        LogUtils.d("setElapsedAlarm after " + hour + ":" + minute + ":" + second);
        long timeElapsed = hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000;
        long triggerAtMillis = System.currentTimeMillis() + timeElapsed;
        Intent intent = new Intent(context, WhatsaiReceiver.class);
        intent.setAction(ComDef.WHATSAI_ACTION_ELAPSED_ALARM);
        setElapsedAlarm(context, triggerAtMillis, intent);
    }

    public static void setDailyElapsedAlarm(Context context, Intent intent) {
        LogUtils.v("setDailyElapsedAlarm");
        long dayMillis = 24 * 60 * 60 * 1000;
        long triggerAtMillis = System.currentTimeMillis() + dayMillis;
        SysUtils.setElapsedAlarm(context, triggerAtMillis, intent);
    }

    private static void setElapsedAlarm(Context context, long triggerAtMillis, Intent intent) {
        LogUtils.d("setAlarm after triggerAtMillis=" + triggerAtMillis);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            LogUtils.e("ERROR: setAlarm: get alarm service failed.");
        } else {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ComDef.WHATSAI_REQUEST_CODE, intent, FLAG_ONE_SHOT);
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    public static void playRingTone(Context context) {
        playRingTone(context, RingtoneManager.TYPE_ALARM);
    }

    private static Ringtone rt = null;

    public static void playRingTone(Context context, int ringType) {
        if (rt == null) {
            Uri notification = RingtoneManager.getDefaultUri(ringType);
            rt = RingtoneManager.getRingtone(context, notification);
        }
        rt.play();
    }

    public static void stopRingTone() {
        if (rt != null) {
            rt.stop();
        }
    }

    /*
    public static String getIMEI(Context context) {
        return SysUtils.getSystemProperty(context, "gsm.imei.sub0", "000000000000000");
    }

    public static String getSystemProperty(Context context, String propName, String defaultValue) {
        final String PROP_NAME_SYSTEM = "android.os.SystemProperties";
        final String METHOD_GET = "get";

        String propValue = defaultValue;

        try {
            Class<?> SystemProperties = context.getClassLoader().loadClass(PROP_NAME_SYSTEM);
            Method methodGet = SystemProperties.getMethod(METHOD_GET, new Class[]{
                    String.class, String.class
            });
            propValue = (String) methodGet.invoke(null, new Object[]{
                    propName, defaultValue
            });
        } catch (Exception ex) {
            LogUtils.e("SysUtils:getSystemProperty:Exception: " + ex.toString());
        }

        if (propValue != null) {
            return propValue;
        } else {
            return defaultValue;
        }
    }

    public static boolean setSystemProperty(Context context, String propName, String propValue) {
        final String PROP_NAME_SYSTEM = "android.os.SystemProperties";
        final String METHOD_SET = "set";

        try {
            Class<?> SystemProperties = context.getClassLoader().loadClass(PROP_NAME_SYSTEM);
            Method methodSet = SystemProperties.getMethod(METHOD_SET, new Class[]{
                    String.class, String.class
            });
            methodSet.invoke(null, new Object[]{
                    propName, propValue
            });
        } catch (Exception ex) {
            LogUtils.e("SysUtils:setSystemProperty:Exception: " + ex.toString());
            return false;
        }

        return true;
    }
    */

    //##@:TODO: add common funtions later
//
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//
//    // Storage Permissions
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//
//    public static void checkAndRequestPermissions(Activity activity, final @NonNull String[] permissions) {
//
//        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }
}
