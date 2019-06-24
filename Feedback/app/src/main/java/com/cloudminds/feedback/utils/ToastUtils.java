package com.cloudminds.feedback.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private static Toast toast;
    public static void showToast(Context context, String content,int time) {
        if (toast == null) {
            toast = Toast.makeText(context, content, time);
        } else {
            toast.setDuration(time);
            toast.setText(content);
        }
        toast.show();
    }
}
