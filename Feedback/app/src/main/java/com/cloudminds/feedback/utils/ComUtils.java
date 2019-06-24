package com.cloudminds.feedback.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.cloudminds.feedback.dialog.DialogSaveListener;
import com.cloudminds.feedback.dialog.SelectUserTypeDialog;
import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.Settings;

/**
 * Created by liz on 2018/1/29.
 */

public class ComUtils {

    public static String getAppVersion(Context context) {
        String ver = "";
        try {
            PackageManager manager = context.getPackageManager();
            String packageName = context.getPackageName();
            LogUtils.d("packageName=" + packageName);
            PackageInfo info = manager.getPackageInfo(packageName,0);
            ver = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e("getPackageInfo exception: " + e.toString());
            e.printStackTrace();
        }
        return ver;
    }

    public static SelectUserTypeDialog.Builder showSetUserTypeDialog(Context context, DialogSaveListener DialogSaveListener){
        String user = Settings.getUserType(context);
        if(TextUtils.isEmpty(user)){
            SelectUserTypeDialog.Builder selectUserTypeDialog =  new SelectUserTypeDialog.Builder(context,DialogSaveListener);
            selectUserTypeDialog.createDialog();
            return selectUserTypeDialog;
        }
        return null;
    }

    public static void setUploadUrl(String userType){
        if(TextUtils.isEmpty(userType)){
            return;
        }
        if(userType.equals(Settings.NORMAL_USER)){
            ComDef.WEB_SERVER_BASE_URL = ComDef.FORMAL_WEB_SERVER_BASE_URL;
            ComDef.FTP_SERVER_URL = ComDef.FORMAL_FTP_SERVER_URL;
        }else{
            ComDef.WEB_SERVER_BASE_URL = ComDef.TEST_WEB_SERVER_BASE_URL;
            ComDef.FTP_SERVER_URL = ComDef.TEST_FTP_SERVER_URL;
        }
        ComDef.WEB_SERVER_POST_URL = ComDef.WEB_SERVER_BASE_URL + ComDef.WEB_SERVER_UPLOAD_PATH;
    }
}
