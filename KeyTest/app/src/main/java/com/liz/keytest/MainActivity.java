package com.liz.keytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button mBtnWhite, mBtnStopWhite, mBtnExit;

    private final static String TAG = MainActivity.class.getSimpleName();
    private Intent whiteIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnWhite = findViewById(R.id.btn_white);
        mBtnStopWhite = findViewById(R.id.btn_stop_white);
        mBtnExit = findViewById(R.id.btn_exit);
        setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class ExitReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.finish();
        }
    }

    private void setListener(){
        OnClick onClick = new OnClick();
        mBtnWhite.setOnClickListener(onClick);
        mBtnStopWhite.setOnClickListener(onClick);
        mBtnExit.setOnClickListener(onClick);
    }

    private class  OnClick implements  View.OnClickListener{
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch(viewId){
                case R.id.btn_white:
                    Log.i(TAG, "MAIN: btn_white");
                    if(null == whiteIntent)
                        whiteIntent = new Intent(MainActivity.this, WhiteService.class);
                    startService(whiteIntent);
                    break;
                case R.id.btn_stop_white:
                    Log.i(TAG, "MAIN: btn_stop_white");
                    if(null != whiteIntent)
                        stopService(whiteIntent);
                    break;
                case R.id.btn_exit:
                    Log.i(TAG, "MAIN: btn_exit");
                    if(null != whiteIntent)
                        stopService(whiteIntent);
                    finish();
                    break;
            }
        }
    }

}
