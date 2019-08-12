package com.liz.noannoy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.noannoy.R;
import com.liz.noannoy.logic.DataAPI;
import com.liz.noannoy.logic.DataLogic;

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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNext) {
            onNextStep();
        }
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

                //###@:
                //String respBody = "{\"resCode\":\"1000\", \"resMsg\":\"请求成功,查得\", \"data\":{\"mdn\":\"123123\",\"url\":\"http\",\"ctime\":\"2019-07-30 16:38:36\",\"utime\":\"2019-07-30 16:52:42\",\"mark\":\"\"}}";
                //String respBody = "{\"resCode\":\"1000\", \"resMsg\":\"请求成功,查得\", \"data\":{\"mdn\":\"123123\",\"url\":\"http://gzlt.dwsoft.com.cn:18080/ivr/\",\"ctime\":\"2019-07-30 16:38:36\",\"utime\":\"2019-07-30 16:52:42\",\"mark\":\"\"}}";
                LogUtils.d( "onResponse: " + respBody);

                final String nodeUrl = DataAPI.parseResponseQueryMdn(respBody);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(nodeUrl)) {
                            Toast.makeText(MainActivity.this, "对不起，该手机号码尚未开通该业务", Toast.LENGTH_LONG).show();
                        }
                        else {
                            DataLogic.setTelNum(telNum);
                            DataLogic.mNodeUrl = nodeUrl;
                            openWebNode(nodeUrl);
                        }
                    }
                });
            }
        });
    }

    public void openWebNode(String nodeUrl) {
        final WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        final String jsInputTelNum = String.format("javascript:document.getElementById('input-mobile').value='%s';", DataLogic.getTelNum());
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("webView: exec js: " + jsInputTelNum);
                webView.evaluateJavascript(jsInputTelNum,null);
            }
        },1000);

        //webView.loadUrl("http://gzlt.dwsoft.com.cn:18080/ivr/");
        //webView.loadUrl("http://www.baidu.com");  //can't access?
        //webView.loadUrl("https://blog.csdn.net/wl521124/article/details/81145970");
        webView.loadUrl(nodeUrl);

        webView.setWebViewClient(new WebViewClientDemo());
        setContentView(webView);

        webView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("####@: webView", "url=" + url);
                if (url != null && TextUtils.equals(url.toString(), "scheme://host/deduct")) {
                    // TODO: 2018/7/21
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        //not effect for click?
        webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebView.HitTestResult result = ((WebView) view).getHitTestResult();
                Log.d("####@: webView", result.getType() + "");

            }
        });

        //ok for long click
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                if (null == result)
                    return false;
                int type = result.getType();
                switch (type) {
                    case WebView.HitTestResult.EDIT_TEXT_TYPE: // 选中的文字类型
                        break;
                    case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                        break;
                    case WebView.HitTestResult.GEO_TYPE: // 　地图类型
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 带有链接的图片类型
                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项 }
                        return true;
                    case WebView.HitTestResult.UNKNOWN_TYPE: //未知
                        break;
                }
                return false;
            }
        });
    }

    public class WebViewClientDemo extends WebViewClient {
        // 在WebView中而不是默认浏览器中显示页面
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            if (url.contains("plus_logo.png")) {
//                try {
//                    return new WebResourceResponse("image/png", "utf-8", mContext.getAssets().open("logo.png"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            return super.shouldInterceptRequest(view, url);
        }
    }
}
