package com.liz.noannoy.logic;

import android.text.TextUtils;

import com.liz.androidutils.LogUtils;
import com.liz.noannoy.app.ThisApp;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/**
 * Created by liz on 2018/3/8.
 */
public class DataLogic {

    private static String mTelNum = "";
    private static String mNodeUrl = "";

    public static void init() {
        mTelNum = Settings.getLocalMdn(ThisApp.getAppContext());
        LogUtils.d("DataLogic: init: mTelNum = " + mTelNum);

        mNodeUrl = Settings.getNodeUrl(ThisApp.getAppContext());
        LogUtils.d("DataLogic: init: mNodeUrl = " + mNodeUrl);
    }

    public static void setTelNum(String telNum) {
        mTelNum = telNum;
    }

    public static String getTelNum() {
        return mTelNum;
    }

    public static void setNodeUrl(String nodeUrl) {
        mNodeUrl = nodeUrl;
    }

    public static String getNodeUrl() {
        return mNodeUrl;
    }

    public static boolean hasLogin() {
        return (!TextUtils.isEmpty(mTelNum)) && (!TextUtils.isEmpty(mNodeUrl));
    }

    public static void fetchURL(String telNum, Callback callback) {
        String url = DataAPI.getUrlQueryMdn(telNum);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }
}
