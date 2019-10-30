package com.liz.autodialer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {


    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.CALL_PHONE
    };

    public static String startCall(Context context, String telNum) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + telNum));
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        if (context.checkSelfPermission("android.permission.CALL_PHONE") == 0) {
            context.startActivity(intent);
            return "OK";
        } else {
            return "ERROR: startCall: No permission of Manifest.permission.CALL_PHONE";
        }
    }

    public static String getITelephonyAllMethods(Context context) {
        String retStr = "getITelephonyAllMethods";
        try {
            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<TelephonyManager> c = TelephonyManager.class;
            retStr += "\n->methodGetITelephony";
            Method methodGetITelephony = c.getDeclaredMethod("getITelephony", (Class[]) null);
            retStr += "\n->setAccessible";
            methodGetITelephony.setAccessible(true);
            retStr += "\n->invoke";
            final Object obj = methodGetITelephony.invoke(telMag, (Object[]) null);
            int i = 0;
            for(Method method:obj.getClass().getDeclaredMethods()) {
                i ++;
                retStr += "\nDeclaredMethod #" + i + ": " + method.getName();
            }
            for(Method method:obj.getClass().getMethods()) {
                i ++;
                retStr += "\nMethod #" + i + ": " + method.getName();
            }
            retStr += "\n->OK";
        } catch (Exception e) {
            e.printStackTrace();
            retStr += "\n->ERROR: endCall: exception: " + e.toString();
        }
        return retStr;
    }

    public static String endCall(Context context) {
        String retStr = "endCall";
        try {
            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<TelephonyManager> c = TelephonyManager.class;
            retStr += "\n->methodGetITelephony";
            Method methodGetITelephony = c.getDeclaredMethod("getITelephony", (Class[]) null);
            retStr += "\n->setAccessible";
            methodGetITelephony.setAccessible(true);
            retStr += "\n->invoke";
            final Object obj = methodGetITelephony.invoke(telMag, (Object[]) null);
            retStr += "\n->getMethod";
            Method methodEndCall = obj.getClass().getMethod("endCall");
            retStr += "\n->setAccessible2";
            methodEndCall.setAccessible(true);
            retStr += "\n->invoke2";
            methodEndCall.invoke(obj);
            retStr += "\n->OK";
        } catch (Exception e) {
            e.printStackTrace();
            retStr += "\n->ERROR: endCall: exception: " + e.toString();
        }
        return retStr;
    }

    public static String endCallForSubscriber(Context context) {
        String retStr = "endCall";
        try {
            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<TelephonyManager> c = TelephonyManager.class;
            retStr += "\n->methodGetITelephony";
            Method methodGetITelephony = c.getDeclaredMethod("getITelephony", (Class[]) null);
            retStr += "\n->setAccessible";
            methodGetITelephony.setAccessible(true);
            retStr += "\n->invoke";
            final Object obj = methodGetITelephony.invoke(telMag, (Object[]) null);
            retStr += "\n->getMethod";
            Method endCallForSubscriber = obj.getClass().getMethod("endCallForSubscriber");
            retStr += "\n->setAccessible2";
            endCallForSubscriber.setAccessible(true);
            retStr += "\n->invoke2";
            for (int i = 0; i < 20; i++) {
                boolean callSuccess = (boolean)endCallForSubscriber.invoke(i);
                if (callSuccess) {
                    retStr += ("挂断卡:" + i + "成功");
                } else {
                    retStr += ("挂断卡:" + i + "失败");
                }
            }
            retStr += "\n->OK";
        } catch (Exception e) {
            e.printStackTrace();
            retStr += "\n->ERROR: endCall: exception: " + e.toString();
        }
        return retStr;
    }

    public static ITelephony getITelephony(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(TELEPHONY_SERVICE);
        Class c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony",
                    (Class[]) null); // 获取声明的方法
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(
                    mTelephonyManager, (Object[]) null); // 获取实例
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//
//        LogUtil.d("开始挂断卡x");
//        try {
//        Method method = Class.forName("android.os.ServiceManager")
//                .getMethod("getService", String.class);
//        IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
//        ITelephony telephony = ITelephony.Stub.asInterface(binder);
//        for (int i = 0; i < 20; i++) {
//            boolean callSuccess = telephony.endCallForSubscriber(i);
//            if (callSuccess) {
//                LogUtil.d("挂断卡:" + i + "成功");
//            } else {
//                LogUtil.d("挂断卡:" + i + "失败");
//            }
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//        LogUtil.d("挂断卡x出错" + e.getMessage());
//    }
//
//    public void hangUp1() throws Exception{
//        Toast.makeText(getApplicationContext(), "hangUp1", 0).show();
//        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        Class tmClazz = tm.getClass();
//        Method getITelephonyMethod = tmClazz.getDeclaredMethod("getITelephony", null);
//        getITelephonyMethod.setAccessible(true);
//        ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(tm, null);
//        iTelephony.endCall();
//    }
//
//    /**
//     * @param v
//     * @throws Exception
//     */
//    public void hangUp2() throws Exception{
//        Toast.makeText(getApplicationContext(), "hangUp1", 0).show();
//        Class clazz = Class.forName("android.os.ServiceManager");
//        Method getServiceMethod = clazz.getMethod("getService", String.class);
//        IBinder iBinder = (IBinder) getServiceMethod.invoke(null, TELEPHONY_SERVICE);
//        ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
//        iTelephony.endCall();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textCallInfo = findViewById(R.id.text_call_info);

        checkPermissions();

        String methods = getITelephonyAllMethods(MainActivity.this);
        textCallInfo.setText(methods);

        findViewById(R.id.btn_dial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //String ret = TelUtils.startCall(MainActivity.this, "17138807531");
                    String ret = startCall(MainActivity.this, "17138807531");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            //String retEndCall = TelUtils.endCall(MainActivity.this);
                            //String retEndCall = endCallForSubscriber(MainActivity.this);
                            String retEndCall = "";
                            getITelephony(MainActivity.this).endCall();
                            Toast.makeText(MainActivity.this, "EndCall: " + retEndCall, Toast.LENGTH_SHORT).show();
                            textCallInfo.setText(retEndCall);
                        }
                    }, 3000L);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "OnClick Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    protected void checkPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions,
                    REQUEST_CODE_PERMISSIONS);
        }
    }

}
