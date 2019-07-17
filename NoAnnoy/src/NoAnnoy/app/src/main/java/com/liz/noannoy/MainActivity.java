package com.liz.noannoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //实例化WebView对象
        WebView webview = new WebView(this);
        //设置WebView属性，能够执行Javascript脚本
        webview.getSettings().setJavaScriptEnabled(true);
        //加载需要显示的网页
        //webview.loadUrl("http://gzlt.dwsoft.com.cn:18080/ivr/");
        //webview.loadUrl("http://www.baidu.com");
        webview.loadUrl("https://blog.csdn.net/wl521124/article/details/81145970");
        webview.setWebViewClient(new WebViewClientDemo());
        //设置Web视图
        setContentView(webview);

        webview.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("####@: webview", "url=" + url);
                if (url != null && TextUtils.equals(url.toString(), "scheme://host/deduct")) {
                    // TODO: 2018/7/21
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        //not effect for click?
        webview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebView.HitTestResult result = ((WebView) view).getHitTestResult();
                Log.d("####@: webview", result.getType()+"");

            }
        });

        //ok for long click
        webview.setOnLongClickListener(new View.OnLongClickListener() {

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
        @Override
        // 在WebView中而不是默认浏览器中显示页面
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
