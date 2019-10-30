package com.liz.multidialerdaemon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
    };

    TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //mTelephonyManager.listen(new PhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);

        loop();
    }

    private void loop() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                mTelephonyManager.listen(new PhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);
                Toast.makeText(MainActivity.this, "MultiDialerDaemon", Toast.LENGTH_SHORT).show();
                loop();
            }
        }, 3000L);
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

    public class PhoneCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("###aaa", "onCallStateChanged-state: " + state);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.v("###aaa", "onCallStateChanged-state: " + state);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.v("###aaa", "onCallStateChanged-incomingNumber: " + incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
}
