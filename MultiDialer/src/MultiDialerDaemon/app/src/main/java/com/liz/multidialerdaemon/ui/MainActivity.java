package com.liz.multidialerdaemon.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.liz.androidutils.LogUtils;
import com.liz.multidialerdaemon.R;
import com.liz.multidialerdaemon.app.ThisApp;
import com.liz.multidialerdaemon.logic.ComDef;
import com.liz.multidialerdaemon.logic.DataLogic;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer mUITimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataLogic.startMultiDialerApp(MainActivity.this);
            }
        });

        findViewById(R.id.btn_exit_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog
                        .Builder(MainActivity.this)
                        .setTitle("确定要退出 MultiDialerDaemon 吗？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ThisApp.exitApp();
                            }
                        }).setNegativeButton("Cancel", null)
                        .show();
            }
        });

        //startUITimer();
    }

    private void startUITimer() {
        //detect and update NV21 files of /sdcard/camera
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (DataLogic.mLifeCount == ComDef.MIN_LIFE_COUNT) {
                            DataLogic.startMultiDialerApp(MainActivity.this);
                        }
                    }
                });
            }
        }, 1000L, 2000L);
    }

    private void stopUITimer() {
        if (mUITimer != null) {
            mUITimer.cancel();
            mUITimer = null;
        }
    }

}
