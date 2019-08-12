package com.liz.noannoy.logic;

import android.text.TextUtils;

import com.liz.androidutils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
public class DataAPI {
    public static String getUrlQueryMdn(String telNum) {
        return "http://" + ComDef.CENTER_SERVER_IP + ":" + ComDef.CENTER_SERVER_PORT_HTTP
                + "/fsrserver/data/query?mdn=" + telNum;
    }

    //String respBody = "{\"resCode\":\"1000\", \"resMsg\":\"请求成功,查得\", \"data\":{\"mdn\":\"123123\",\"url\":\"http\",\"ctime\":\"2019-07-30 16:38:36\",\"utime\":\"2019-07-30 16:52:42\",\"mark\":\"\"}}";
    //String respBody = "{\"resCode\":\"1000\", \"resMsg\":\"请求成功,查得\", \"data\":{\"mdn\":\"123123\",\"url\":\"http://gzlt.dwsoft.com.cn:18080/ivr/\",\"ctime\":\"2019-07-30 16:38:36\",\"utime\":\"2019-07-30 16:52:42\",\"mark\":\"\"}}";

    public static String parseResponseQueryMdn(String resStr) {
        String url = "";
        try {
            JSONObject obj = new JSONObject(resStr);
            String resCode = obj.optString("resCode");
            String resMsg = obj.optString("resMsg");
            String dataStr = obj.optString("data");
            //JSONObject data = new JSONObject()
            LogUtils.d("resCode: " + resCode);
            LogUtils.d("resMsg: " + resMsg);
            LogUtils.d("data: " + dataStr);
            if (!TextUtils.isEmpty(dataStr)) {
                JSONObject dataObj = new JSONObject(dataStr);
                url = dataObj.optString("url");
                LogUtils.d("url: " + url);
            }
        } catch (Exception e) {
            LogUtils.e("parseResponseQueryMdn Exception: " + e.toString());
            e.printStackTrace();
        }
        return url;
    }
}
