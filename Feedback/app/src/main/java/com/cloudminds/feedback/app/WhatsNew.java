package com.cloudminds.feedback.app;

import android.app.AlertDialog;
import android.content.Context;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.activity.MainActivity;
import com.cloudminds.feedback.dialog.SelectUserTypeDialog;
import com.cloudminds.feedback.utils.ComUtils;

/**
 * WhatsNew.java: what's new for a release version
 * Created by liz on 2018/6/22.
 */

@SuppressWarnings("unused")
public class WhatsNew {
    //final static String RELEASE_VERSION_NUMBER = "1.0.56.180622.0239.853160b";
    //final static String MODIFICATIONS =
    private final static String WHAT_S_NEW = "" +
            "whatsnew in " + ThisApp.mAppVersion + ":\n" +
            "  1. add Task feature on main menu which save uncompleted upload tasks.\n" +
            "  2. only restart logd when log is running after submit message.\n" +
            "  3. improve network connection exception tips.\n" +
            "whatsnew in 1.0.59.180701.0226.76d00fb:\n" +
            "  1. refactor code for ftp manager when checking file exist.\n" +
            "  2. notify ftp to end when exception(421).\n" +
            "  3. ftp notify also disappear when failed as success.\n" +
            "  4. set ftp notify also disappear delay time to 6s.\n" +
            "  5. App Errors also upload offline logs as direct run.\n" +
            "  6. Anr/Exception(auto feedback messages) also have wifi limitations when uploading offline logs.\n" +
            "\n" +
            "whatsnew in 1.0.57.180625.1404.cab4eac: \n" +
            "  1. change Log Config to Log in main menu.\n" +
            "  2. show User Experience switch tab for turn on/off User Experience.\n" +
            "  3. fix the problem that offline log can't be removed after submit.\n" +
            "  4. add history version show on menu top.\n" +
            "\n" +
            "whatsnew in 1.0.56.180622.0239.853160b: \n" +
            "  1. remove userinfo from menu for the sake of security privacy.\n" +
            "  2. remove offline log files after zipped by click submit.\n" +
            "  3. remove log zip file after upload success.\n" +
            "  4. set save property for cmlogd to copy anr/tombstone file when submit.\n" +
            "  5. config update without click button, and take effect when activity exit.\n" +
            "  6. modify Log Config button as show logd state and click to switch on/off.\n" +
            "  7. replace log file size as logd status in main window.\n" +
            "  8. use offline log files for FEEDBACK_TYPE_DIRECT_RUN.\n" +
            "  9. exit main window after submit one feedback message.\n" +
            "  7. notify window auto disappear after upload complete in 2s.\n" +
            "  8. restart logd after submit one feedback to catch offline log to file again.\n" +
            "";

    private static int mCheckCount = 0;
    private static final int MAX_CHECK_COUNT = 6;

    public static void checkShow(Context context) {
        if (mCheckCount < MAX_CHECK_COUNT) {
            mCheckCount ++;
        }
        else {
            SelectUserTypeDialog.Builder selectUserTypeDialog =  new SelectUserTypeDialog.Builder(context,(MainActivity)context);
            selectUserTypeDialog.createDialog();
            /*new AlertDialog.Builder(context)
                    .setMessage(WHAT_S_NEW)
                    .setIcon(R.drawable.cloudminds)
                    .setTitle("History Versions")
                    .show();*/
            mCheckCount = 0;
        }
    }
}
