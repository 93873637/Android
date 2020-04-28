package com.liz.androidutils;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;


@SuppressWarnings({"unused", "WeakerAccess"})
public class TelUtils {

    public static String getSimStateString(int simState) {
        switch (simState) {
            case android.telephony.TelephonyManager.SIM_STATE_ABSENT:
                return "ABSENT";
            case android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "PIN_REQUIRED";
            case android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "PUK_REQUIRED";
            case android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "NETWORK_LOCKED";
            case android.telephony.TelephonyManager.SIM_STATE_READY:
                return "READY";
            case android.telephony.TelephonyManager.SIM_STATE_NOT_READY:
                return "NOT_READY";
            case android.telephony.TelephonyManager.SIM_STATE_PERM_DISABLED:
                return "PERM_DISABLED";
            case android.telephony.TelephonyManager.SIM_STATE_CARD_IO_ERROR:
                return "CARD_IO_ERROR";
            case android.telephony.TelephonyManager.SIM_STATE_CARD_RESTRICTED:
                return "CARD_RESTRICTED";
            case android.telephony.TelephonyManager.SIM_STATE_UNKNOWN:
                return "UNKNOWN";
            default:
                return "ERROR UNKNOWN";
        }
    }

    public static String getSimStateString(Context context, int slotIndex) {
        return getSimStateString(getSimState(context, slotIndex));
    }

    /**
     * Returns a constant indicating the state of the device SIM card in a slot.
     *
     * @param slotIndex #SIM_STATE_UNKNOWN
     *                  #SIM_STATE_ABSENT
     *                  #SIM_STATE_PIN_REQUIRED
     *                  #SIM_STATE_PUK_REQUIRED
     *                  #SIM_STATE_NETWORK_LOCKED
     *                  #SIM_STATE_READY
     *                  #SIM_STATE_NOT_READY
     *                  #SIM_STATE_PERM_DISABLED
     *                  #SIM_STATE_CARD_IO_ERROR
     *                  #SIM_STATE_CARD_RESTRICTED
     */
    @TargetApi(26)
    public static int getSimState(Context context, int slotIndex) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                LogUtils.te2("get telephony manager null");
                return -1;
            }
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return telephonyManager.getSimState(slotIndex);
            } else {
                LogUtils.te2("no permission of Manifest.permission.READ_PHONE_STATE");
                return -2;
            }
        } catch (Exception e) {
            LogUtils.te2("get imsi failed, ex = " + e.toString());
            e.printStackTrace();
            return -3;
        }
    }

//
//    @TargetApi(23)
//    public static String getIMSI(Context context) {
//        try {
//            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            if (telephonyManager == null) {
//                LogUtils.te2("get telephony manager null");
//                return "";
//            }
//            if (context.checkSelfPermission(android..READ_PRIVILEGED_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                String imsi = telephonyManager.getSubscriberId();
//                if (null == imsi) {
//                    imsi = "";
//                }
//                return imsi;
//            }
//            else {
//                LogUtils.te2("no permission of Manifest.permission.READ_PHONE_STATE");
//                return "";
//            }
//        } catch (Exception e) {
//            LogUtils.te2("get imsi failed, ex = " + e.toString());
//            e.printStackTrace();
//            return "";
//        }
//    }

    @TargetApi(23)
    public static String startCall(Context context, String telNum) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNum));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
            return "OK";
        } else {
            return "ERROR: startCall: No permission of Manifest.permission.CALL_PHONE";
        }
    }

    public static String endCall(Context context) {
        try {
            TelephonyManager telMag = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            Class<TelephonyManager> c = TelephonyManager.class;
            //get obj ITelephony by reflect
            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
            //access permitted
            mthEndCall.setAccessible(true);
            final Object obj = mthEndCall.invoke(telMag, (Object[]) null);
            //call endCall method of ITelephony obj
            Method mt = obj.getClass().getMethod("endCall");
            //access permitted
            mt.setAccessible(true);
            mt.invoke(obj);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: endCall: exception: " + e.toString();
        }
    }

    public static boolean isCalling(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) ||
                (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING);
    }

    public static boolean isValidTelNumber(String strTelNumber) {
        if (strTelNumber == null || strTelNumber.isEmpty()) {
            return false;
        }
        String regex = "^[0-9]*$";
        return Pattern.matches(regex, strTelNumber);
    }

    public static ITelephony getITelephony(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        Class c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(telephonyManager, (Object[]) null);
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void testCheckTelNumber(String strTelNumber) {
        System.out.println("isValidTelNumber(\"" + strTelNumber + "\")=" + isValidTelNumber(strTelNumber));
    }

    public static void main(String[] args) {
        testCheckTelNumber("");
        testCheckTelNumber("10086");
        testCheckTelNumber("13910115737");
        testCheckTelNumber("13910 5737");
        testCheckTelNumber("13910a5737");
        testCheckTelNumber("+8613910115737");
    }
}
