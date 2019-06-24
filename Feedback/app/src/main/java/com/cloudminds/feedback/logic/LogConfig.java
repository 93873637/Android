package com.cloudminds.feedback.logic;

import android.text.TextUtils;

import com.cloudminds.feedback.app.ThisApp;
import com.cloudminds.feedback.utils.LogUtils;
import com.cloudminds.feedback.utils.SysUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * LogConfig.java: process with log config and daemon app(cmlogd)
 * Created by liz on 2018/4/12.
 */

public class LogConfig {

    //delay sometime for cmlogd from stop to start
    final static int LOGD_RESTART_SPAN = 2000;  //unit by milliseconds

    //log flag of the modules, true if enable, or false
    public static boolean app_flag = false;
    public static boolean modem_flag = false;
    public static boolean net_flag = false;
    public static boolean kernel_flag = false;
    public static boolean gps_flag = false;
    public static boolean wlan_flag = false;
    public static boolean sysprof_flag = false;
    public static boolean charge_flag = false;
    public static boolean sensor_flag = false;

    //config string from property
    //"persist.sys.cmlogd.list","0:logcat;0:qxdm;0:tcpdump;0:kmsg;0:wlan;0:charge;0:sysprof"
    private static String mConfigString = "";

    public static void init() {
        loadConfig();
        if (ComDef.isUserExperienceEnabled(ThisApp.getAppContext())) {
            enableDefaultLog();
        }
    }

    private static void loadConfig() {
        //"persist.sys.cmlogd.list","0:logcat;0:qxdm;0:tcpdump;0:kmsg;0:wlan;0:charge;0:sysprof"
        mConfigString = SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_CONFIG_LIST, ComDef.PROP_SYS_LOG_CONFIG_LIST_DEFAULT);
        LogUtils.e("LogConfig:loadConfig: get config: \"" + mConfigString + "\"");

        String[] items = mConfigString.split(";");
        for (String item : items) {
            String[] values = item.split(":");
            if (values.length != 2) {
                LogUtils.e("ERROR: invalid item: " + item);
            }
            else {
                switch (values[1]) {
                    case "logcat":
                        app_flag = values[0].equals("1");
                        break;
                    case "qxdm":
                        parseQxdmFlags(values[0]);
                        break;
                    case "tcpdump":
                        net_flag = values[0].equals("1");
                        break;
                    case "kmsg":
                        kernel_flag = values[0].equals("1");
                        break;
                    case "wlan":
                        wlan_flag = values[0].equals("1");
                        break;
                    case "charge":
                        charge_flag = values[0].equals("1");
                        break;
                    case "sysprof":
                        sysprof_flag = values[0].equals("1");
                        break;
                   default:
                        LogUtils.e("ERROR: unknown item name: " + values[1]);
                        break;
                }
            }
        }

        LogUtils.e("LogConfig:loadConfig: parse config: app_flag=" + app_flag + ", modem_flag=" + modem_flag + ", net_flag=" + net_flag + ", kernel_flag=" + kernel_flag + ", gps_flag=" + gps_flag
                     + ", wlan_flag=" + wlan_flag + ", sysprof_flag=" + sysprof_flag + ", charge_flag=" + charge_flag + ", sensor_flag=" + sensor_flag);
    }

    public static void updateConfig() {
        //"persist.sys.cmlogd.list","0:logcat;0:qxdm;0:tcpdump;0:kmsg;0:wlan;0:charge;0:sysprof"
        String newConfigString = (app_flag?"1":"0") + ":logcat";
        newConfigString += ";" + buildQxdmValue() + ":qxdm";
        newConfigString += ";" + (net_flag?"1":"0") + ":tcpdump";
        newConfigString += ";" + (kernel_flag?"1":"0") + ":kmsg";
        newConfigString += ";" + (wlan_flag?"1":"0") + ":wlan";
        newConfigString += ";" + (charge_flag?"1":"0") + ":charge";
        newConfigString += ";" + (sysprof_flag?"1":"0") + ":sysprof";
        LogUtils.e("LogConfig:updateConfig: build config: \"" + newConfigString + "\"");

        if (newConfigString.equals(mConfigString)) {
            LogUtils.d("config not change");
        }
        else {
            mConfigString = newConfigString;

            //update config prop with new string for cmlogd
            SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_CONFIG_LIST, newConfigString);

            //restart cmlogd if running(NOTE: current log will be clear)
            if (isLogdRunning()) {
                restartLogd();
            }
        }
    }

    public static void startLogd() {
        LogUtils.d("LogConfig.startLogd");
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_STATE, "1");
    }

    public static void stopLogd() {
        LogUtils.d("LogConfig.stopLogd");
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_STATE, "0");
    }

    public static void restartLogd() {
        LogUtils.d("LogConfig.restartLogd");
        stopLogd();

        //delay a while for cmlogd process exit
        new Timer().schedule(new TimerTask() {
            public void run() {
                startLogd();
            }
        }, LOGD_RESTART_SPAN);
    }

    public static boolean isLogdRunning() {
        String logState = SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_STATE, "0");
        return TextUtils.equals(logState, "1");
    }

    public static void syncSysFiles(boolean bWait) {
        LogUtils.d("LogConfig.syncSysFiles");
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_SAVE, "1");

        if (bWait) {
            LogUtils.d("wait for cmlogd finish copy anr/log files...");
            final int MAX_WAIT_COUNT = 6;
            final int WAIT_TIME_SPAN_FOR_COPY = 500;  //unit by milliseconds
            int count = 0;
            while(count < MAX_WAIT_COUNT) {
                String saveFlag = SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_SYS_LOG_SAVE, "1");
                if (TextUtils.equals(saveFlag, "2")) {
                    LogUtils.d("cmlogd have finished copy anr/log files.");
                    break;
                }
                else {
                    count ++;
                    LogUtils.d("copy anr/log files not completed, sleep #" + count + " to wait...");
                    try {
                        Thread.currentThread().sleep(WAIT_TIME_SPAN_FOR_COPY);
                    } catch (Exception e) {
                        LogUtils.e("syncSysFiles: sleep exception: " + e.toString());
                    }
                }
            }  //while
            if (count == MAX_WAIT_COUNT) {
                LogUtils.w("wait count exceed max(" + MAX_WAIT_COUNT + "), anr/tombstone maybe not copied.");
            }
        }
    }

    private static void parseQxdmFlags(String strVal) {
        int intVal = Integer.parseInt(strVal);
        if (intVal < 0 || intVal > 7) {
            LogUtils.e("ERROR: invalid qxdm val: " + intVal);
        }
        else {
            modem_flag = ((intVal & 0x1) == 1);
            gps_flag = ((intVal & 0x2) == 2);
            sensor_flag = ((intVal & 0x4) == 4);
        }
    }

    private static String buildQxdmValue() {
        int modem = modem_flag ? 1 : 0;
        int gps = gps_flag ? 1 : 0;
        int sensor = sensor_flag ? 1 : 0;
        int qxdm = sensor*4 + gps*2 + modem;
        return "" + qxdm;
    }

    public static void enableDefaultLog() {
        app_flag = true;
        kernel_flag = true;
        updateConfig();
    }
}
