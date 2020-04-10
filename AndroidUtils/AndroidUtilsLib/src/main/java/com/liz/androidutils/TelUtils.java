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

    @TargetApi(23)
    public static String startCall(Context context, String telNum) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNum));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
            return "OK";
        }
        else {
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

    public static boolean isCalling(Context context){
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
