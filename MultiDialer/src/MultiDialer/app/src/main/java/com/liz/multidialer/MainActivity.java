package com.liz.multidialer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.TelUtils;
import com.liz.androidutils.FileUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TEL_LIST_FILE_NAME = "/sdcard/tellist.txt";

    private TextView mTextInfo;
    private TextView mTextTelNumber;

    private int mTelIndex = 0;
    private ArrayList<String> mTelList;

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        mTelList = FileUtils.readTxtFileLines(TEL_LIST_FILE_NAME);

        mTextInfo = findViewById(R.id.text_dial_info);
        mTextTelNumber = findViewById(R.id.text_tel_list_info);
        mTextTelNumber.setText("电话号码数量(" + TEL_LIST_FILE_NAME + "): " + mTelList.size());

        //##@: test
//        for (int i=0; i<100; i++) {
//            showMsg("#" + i + ": Start Call: 12345678901, OKssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
//        }

        findViewById(R.id.btn_start_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCallOnList();
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

    private void startCallOnList() {
        if (mTelList == null) {
            showMsg("startCallOnList: list null");
            return;
        }
        mTelIndex = 0;
        startCallOnNum(mTelIndex);
    }

    private void startCallOnNum(int idx) {
        if (idx >= mTelList.size()) {
            showMsg("***Call Finished.");
            return;
        }
        String strTel = mTelList.get(idx);
        try {
            String ret = TelUtils.startCall(MainActivity.this, strTel);
            showMsg("#" + (idx+1) + ": Start Call, Tel = " + strTel + ", " + ret);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    String retEndCall = TelUtils.endCall(MainActivity.this);
                    showMsg("End Call: " + retEndCall);
                    mTelIndex++;
                    startCallOnNum(mTelIndex);
                }
            }, 2 * 1000);  // 延迟n秒后自动挂断电话
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "OnClick Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            showMsg("startCallOnNum Exception: " + e.toString());
        }
    }

    private void showMsg(String msg) {
        mTextInfo.append(msg + "\n");
    }
}
