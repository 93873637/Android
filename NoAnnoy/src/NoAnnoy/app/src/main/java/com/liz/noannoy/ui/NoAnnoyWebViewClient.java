package com.liz.noannoy.ui;

import android.graphics.Bitmap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.liz.androidutils.LogUtils;


public class NoAnnoyWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtils.d("NoAnnoyWebViewClient: shouldOverrideUrlLoading: url=" + url);
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        //LogUtils.d("NoAnnoyWebViewClient: onLoadResource: url=" + url);
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        LogUtils.d("NoAnnoyWebViewClient: onPageStarted: url=" + url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        LogUtils.d("NoAnnoyWebViewClient: onPageFinished: url=" + url);
        super.onPageFinished(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        //LogUtils.d("NoAnnoyWebViewClient: shouldInterceptRequest: url=" + url);
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
