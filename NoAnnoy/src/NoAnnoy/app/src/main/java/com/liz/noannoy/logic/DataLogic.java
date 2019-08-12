package com.liz.noannoy.logic;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/**
 * Created by liz on 2018/3/8.
 */
public class DataLogic {

    private static String mTelNum = "";
    public static String mNodeUrl = "";

    public static void init() {
    }

    public static void setTelNum(String telNum) {
        mTelNum = telNum;
    }

    public static String getTelNum() {
        return mTelNum;
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
