package com.liz.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private LinearLayout mFullView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.btn); // 识别按钮
        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0); // 本地识别程序
        // new Intent(RecognizerIntent.ACTION_WEB_SEARCH), 0); // 网络识别程序

        mFullView = findViewById(R.id.full_view);

        /*
         * 此处没有使用捕捉异常，而是检测是否有语音识别程序。
         * 也可以在startRecognizerActivity()方法中捕捉ActivityNotFoundException异常
         */
        if (activities.size() != 0) {
            btn.setOnClickListener(this);
        } else {
            // 若检测不到语音识别程序在本机安装，测将扭铵置灰
            btn.setEnabled(false);
            btn.setText("未检测到语音识别设备");
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            startRecognizerActivity();
        }
    }

    // 开始识别
    private void startRecognizerActivity() {
        // 通过Intent传递语音识别的模式，开启语音
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // 语言模式和自由模式的语音识别
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // 提示语音开始
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");
        // 开始语音识别
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        // 调出识别界面
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("###@:", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        // 回调获取从谷歌得到的数据
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.e("###@:", "onActivityResult: VOICE_RECOGNITION_REQUEST_CODE");
            // 取得语音的字符
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.e("###@:", "results.size()=" + results.size());
            String resultString = "";
            for (int i = 0; i < results.size(); i++) {
                resultString += results.get(i);
                Log.e("###@:" + i, results.get(i));
            }
            //Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
            onRecognizedVoiceText(results.get(0));
        }
        // 语音识别后的回调，将识别的字串以Toast显示
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onRecognizedVoiceText(String voiceText) {
        Log.d("aaa", "onRecognizedVoiceText = " + voiceText);
        if (TextUtils.equals(voiceText, "red")) {
            mFullView.setBackgroundColor(Color.RED);
        }
        else if (TextUtils.equals(voiceText, "blue")) {
            mFullView.setBackgroundColor(Color.BLUE);
        }
        else if (TextUtils.equals(voiceText, "black")) {
            mFullView.setBackgroundColor(Color.BLACK);
        }
        else if (TextUtils.equals(voiceText, "green")) {
            mFullView.setBackgroundColor(Color.GREEN);
        }
        else if (TextUtils.equals(voiceText, "yellow")) {
            mFullView.setBackgroundColor(Color.YELLOW);
        }
        else if (TextUtils.equals(voiceText, "gray")) {
            mFullView.setBackgroundColor(Color.GRAY);
        }
        else {
            mFullView.setBackgroundColor(Color.WHITE);
        }
        startRecognizerActivity();
    }
}
