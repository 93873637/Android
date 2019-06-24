package com.cloudminds.feedback.logic;

import android.content.Context;

import com.cloudminds.feedback.net.IFUserFeedback;
import com.cloudminds.feedback.utils.LogUtils;

import retrofit2.Callback;

/**
 * Created by liz on 2018/3/8.
 */

public class DataLogic {
    public static void uploadImages(Context context, IFUserFeedback.ImageBody imageBody, IFUserFeedback service, Callback call) {
        //upload images to web server
        LogUtils.d("uploadImages: imagebody=" + imageBody.toString());
        service.uploadImages(ComDef.WEB_SERVER_POST_URL, imageBody).enqueue(call);
    }

    public static String getErrorMessage(Throwable t) {
        String ret = t.toString();
        if (ret.contains("SocketTimeoutException: failed to connect")) {
            ret = "CONN TIMEOUT";
        }
        return ret;
    }
}
