package com.liz.noannoy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.noannoy.R;
import com.liz.noannoy.app.ThisApp;
import com.liz.noannoy.logic.ComDef;
import com.liz.noannoy.logic.DataAPI;
import com.liz.noannoy.logic.DataLogic;
import com.liz.noannoy.logic.Settings;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnNext).setOnClickListener(this);

        if (DataLogic.hasLogin()) {
            openWebNode(DataLogic.getNodeUrl());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNext) {
            onNextStep();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Toast.makeText(this, "###@: TODO: show settings...", 1).show();
                break;
            case R.id.menu_about:
                LogUtils.d("show about dlg...");
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getResources().getString(R.string.app_name) + "  " + ThisApp.mAppVersion)
                        .setIcon(R.drawable.ic_launcher_background)
                        .setTitle(ComDef.APP_NAME)
                        .show();
                break;
        }
        return true;
    }

    public void onNextStep() {
        EditText editTelNum = findViewById(R.id.editTelNum);
        final String telNum = editTelNum.getText().toString();

        if (TextUtils.isEmpty(telNum)) {
            Toast.makeText(this,"请输入手机号码", Toast.LENGTH_LONG).show();
            return;
        }

        DataLogic.fetchURL(telNum, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                LogUtils.d( "onFailure: e=" + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String respBody = response.body().string();
                LogUtils.d( "onResponse: " + respBody);

                final DataAPI.RespQueryMdn resp = DataAPI.parseResponseQueryMdn(respBody);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resp.errCode != DataAPI.RESP_ERR_SUCCESS) {
                            Toast.makeText(MainActivity.this, resp.errMsg, Toast.LENGTH_LONG).show();
                        }
                        else {
                            DataLogic.setTelNum(telNum);
                            DataLogic.setNodeUrl(resp.url);
                            Settings.setLocalMdn(MainActivity.this, telNum);
                            Settings.setNodeUrl(MainActivity.this, resp.url);
                            openWebNode(DataLogic.getNodeUrl());
                        }
                    }
                });
            }
        });
    }

    public void openWebNode(String nodeUrl) {
        final WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new NoAnnoyWebViewClient());
        setContentView(webView);
        webView.loadUrl(nodeUrl);

        final String jsInputTelNum = String.format("javascript:document.getElementById('input-mobile').value='%s';", DataLogic.getTelNum());
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("webView: exec js: " + jsInputTelNum);
                webView.evaluateJavascript(jsInputTelNum,null);
            }
        },2000);
    }
}
