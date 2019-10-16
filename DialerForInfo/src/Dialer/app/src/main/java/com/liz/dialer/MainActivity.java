package com.liz.dialer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.TelUtils;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    EditText mEditPhoneNumber;
    TextView mTextInfo;

//
//    public static String startCall(Context context, String telNum) {
//        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNum));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
//            context.startActivity(intent);
//            return "startCall: " + telNum;
//        }
//        else {
//            return "ERROR: startCall: No permission of Manifest.permission.CALL_PHONE";
//        }
//    }
//
//    public static String endCall(Context context) {
//        try {
//            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            Class<TelephonyManager> c = TelephonyManager.class;
//            //get obj ITelephony by reflect
//            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
//            //access permitted
//            mthEndCall.setAccessible(true);
//            final Object obj = mthEndCall.invoke(telMag, (Object[]) null);
//            //call endCall method of ITelephony obj
//            Method mt = obj.getClass().getMethod("endCall");
//            //access permitted
//            mt.setAccessible(true);
//            mt.invoke(obj);
//            return "endCall";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "ERROR: endCall: exception: " + e.toString();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.CALL_PHONE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }

//        findViewById(R.id.btnDial).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_CALL);
//                Uri data = Uri.parse("tel:10086");
//                intent.setData(data);
//                startActivity(intent);
//            }
//        });

        mTextInfo = findViewById(R.id.text_info);
        mEditPhoneNumber = findViewById(R.id.editPhoneNumber);
        findViewById(R.id.btn_phone1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String strPhoneNumber = mEditPhoneNumber.getText().toString();

                    // 开始直接拨打电话
                    TelUtils.startCall(MainActivity.this, strPhoneNumber);
//                    Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strPhoneNumber));
//                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent2);
                    Toast.makeText(MainActivity.this, "拨打电话！", Toast.LENGTH_SHORT).show();
                    mTextInfo.append("dial to " + strPhoneNumber + "\n");

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            TelUtils.endCall(MainActivity.this);
//                            try {
//                                // 首先拿到TelephonyManager
//                                TelephonyManager telMag = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                                Class<TelephonyManager> c = TelephonyManager.class;
//
//                                // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
//                                Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
//                                //允许访问私有方法
//                                mthEndCall.setAccessible(true);
//                                final Object obj = mthEndCall.invoke(telMag, (Object[]) null);
//
//                                // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
//                                Method mt = obj.getClass().getMethod("endCall");
//                                //允许访问私有方法
//                                mt.setAccessible(true);
//                                mt.invoke(obj);
//                                Toast.makeText(MainActivity.this, "挂断电话！", Toast.LENGTH_SHORT).show();
//                                mTextInfo.append("End Call: " + strPhoneNumber + "\n");
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                mTextInfo.append("End Call Exception: " + e.toString() + "\n");
//                            }
                        }
                    }, 2 * 1000);  // 延迟n秒后自动挂断电话
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "OnClick Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    mTextInfo.append("OnClick Exception: " + e.toString() + "\n");
                }
            }
        });


//        btn_phone1 = (Button) findViewById(R.id.btn_phone1);
//        btn_phone1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    // 首先拿到TelephonyManager
//                    TelephonyManager telMag = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                    Class<TelephonyManager> c = TelephonyManager.class;
//
//                    // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
//                    Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
//                    //允许访问私有方法
//                    mthEndCall.setAccessible(true);
//                    final Object obj = mthEndCall.invoke(telMag, (Object[]) null);
//
//                    // 再通过ITelephony对象去反射里面的call方法，并传入包名和需要拨打的电话号码
//                    Method mt = obj.getClass().getMethod("call", new Class[]{String.class, String.class});
//                    //允许访问私有方法
//                    mt.setAccessible(true);
//                    mt.invoke(obj, new Object[]{getPackageName() + "", "10086"});
//
//                    Toast.makeText(MainActivity.this, "拨打电话！", Toast.LENGTH_SHORT).show();
//
//                    new Handler().postDelayed(new Runnable() {
//                        public void run() {
//                            try {
//                                // 延迟5秒后自动挂断电话
//                                // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
//                                Method mt = obj.getClass().getMethod("endCall");
//                                //允许访问私有方法
//                                mt.setAccessible(true);
//                                mt.invoke(obj);
//                                Toast.makeText(MainActivity.this, "挂断电话！", Toast.LENGTH_SHORT).show();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, 5 * 1000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

//
//    //挂断电话，需要调用系统底层的方法，要用哪个到反射拿底层方法
//    public  void endcall(){
//        //加载serviceManager的字节码
//        Class clazz=CallSmsSafeService.class.getClassLoader().loadClass("android.os.ServiceManager");
//        Method method=clazz.getDeclaredMethod("getservice",String.class);
//        IBinder ibind=method.invoke(null,TELEPHONY_SERVICE);
//        ITelephony.Stub.asInterface(ibind).endCall();
//
//    }
}
