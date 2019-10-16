package com.liz.androidutils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

public class TelUtils {

    public static String startCall(Context context, String telNum) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNum));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
            return "startCall: " + telNum;
        }
        else {
            return "ERROR: startCall: No permission of Manifest.permission.CALL_PHONE";
        }
    }

    public static String endCall(Context context) {
        try {
            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
            return "endCall";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: endCall: exception: " + e.toString();
        }
    }
}
