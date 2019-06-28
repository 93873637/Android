package com.liz.imagetools.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.liz.imagetools.app.ImageToolsReceiver;
import com.liz.imagetools.app.ThisApp;
import com.liz.imagetools.ui.MainActivity;
import com.liz.imagetools.utils.FileUtils;
import com.liz.imagetools.utils.ImageUtils;
import com.liz.imagetools.utils.LogUtils;
import com.liz.imagetools.utils.SysUtils;

import java.io.File;
import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.List;

public class DataLogic {

    public interface DataProcessListener {
        void onProgressMessage(String msg);
    }

    private static DataProcessListener mDataProcessListener;

    public static void setDataProcessListener(DataProcessListener listener) {
        mDataProcessListener = listener;
    }

    public static void showProgress(String msg) {
        if (mDataProcessListener != null) {
            mDataProcessListener.onProgressMessage(msg);
        }
    }
    
    public static void init(final Context context) {

        //mkdirs
        File nv21Path = new File(ComDef.NV21_FILES_PATH);
        if (!nv21Path.exists()) {
            LogUtils.i("create nv21 image path: " + ComDef.NV21_FILES_PATH);
            if (!nv21Path.mkdirs()) {
                LogUtils.e("ERROR: create image path \"" + ComDef.NV21_FILES_PATH + "\" failed.");
            }
        }

        File jpgPath = new File(ComDef.JPG_FILES_PATH);
        if (!nv21Path.exists()) {
            LogUtils.i("create jpg image path: " + ComDef.JPG_FILES_PATH);
            if (!nv21Path.mkdirs()) {
                LogUtils.e("ERROR: create image path \"" + ComDef.JPG_FILES_PATH + "\" failed.");
            }
        }

        ImageToolsReceiver.addAction(ComDef.ACTION_IMAGE_TOOLS_NV21_TO_JPG, new ImageToolsReceiver.ReceiverListener() {
            @Override
            public void onReceiveAction(String action) {
                //restartMainActivityForAction(context, action);
                convertNV21toJPG(ThisApp.getAppContext());
            }
        });
    }

    public static int getImageWidth(Context context) {
        return SysUtils.getSystemPropertyInt(context, ComDef.PROP_IMAGE_WIDTH, 0);
    }

    public static void setImageWidth(Context context, int width) {
        SysUtils.setSystemProperty(context, ComDef.PROP_IMAGE_WIDTH, "" + width);
    }

    public static int getImageHeight(Context context) {
        return SysUtils.getSystemPropertyInt(context, ComDef.PROP_IMAGE_HEIGHT, 0);
    }

    public static void setImageHeight(Context context, int height) {
        SysUtils.setSystemProperty(context, ComDef.PROP_IMAGE_HEIGHT, "" + height);
    }

    private static void restartMainActivityForAction(Context context, String action) {
        LogUtils.d("restartMainActivityForAction： action=" + action);
        Activity mainActivity = ThisApp.getMainActivity();
        if (mainActivity != null) {
            mainActivity.finish();
        }
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra(ComDef.IMAGE_TOOLS_MSG, ComDef.ACTION_IMAGE_TOOLS_NV21_TO_JPG);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }

    public static void convertNV21toJPG(Context context) {

        List<File> nv21Files = new ArrayList<>();
        if (!FileUtils.listFiles(ComDef.NV21_FILES_PATH, ComDef.NV21_FILE_EXTNAME, nv21Files)) {
            showProgress("ERROR: list file of path \"" + ComDef.NV21_FILES_PATH + "\" failed");
            return;
        }
        showProgress("ConvertNV21toJPG: file number = " + nv21Files.size());

        if (nv21Files.size() == 0) {
            showProgress("No nv21 files found in path \"" + ComDef.NV21_FILES_PATH + "\"");
            return;
        }

        int width = SysUtils.getSystemPropertyInt(context, ComDef.PROP_IMAGE_WIDTH, 0);
        if (width <= 0) {
            showProgress("ERROR: No with found by prop: " + ComDef.PROP_IMAGE_WIDTH);
            return;
        }

        int height = SysUtils.getSystemPropertyInt(context, ComDef.PROP_IMAGE_HEIGHT, 0);
        if (height <= 0) {
            showProgress("ERROR: No height found by prop: " + ComDef.PROP_IMAGE_HEIGHT);
            return;
        }

        int fileNumber = nv21Files.size();
        LogUtils.d("convertNV21toJPG: fileNumber=" + fileNumber + ", size=" + width + "x" + height);

        int count = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        try {
            showProgress("***Start Convert...");
            for (File file : nv21Files) {
                count ++;
                showProgress("#" + count + "/" + fileNumber + ": file=" + file.getName());
                String fileName = file.getName();
                LogUtils.d("convertNV21toJPG: fileName=" + fileName);

                showProgress("width=" + width + ", height=" + height);
                String res = ImageUtils.NV21toJPG(file, width, height, ComDef.JPG_FILES_PATH, ComDef.JPG_IMAGE_QUALITY);
                showProgress(res);

                if (res.equals(ImageUtils.SUCCESS)) {
                    totalSuccess++;
                } else {
                    totalFailed++;
                }
            }
        }
        catch (Exception e) {
            LogUtils.d("convertNV21toJPG Exception: " + e.toString());
            showProgress("Exception: " + e.toString());
        }

        LogUtils.d("convertNV21toJPG: Exit.");
        showProgress("***Convert Finished: total=" + count + ", Success=" + totalSuccess + ", Failed=" + totalFailed);
    }
}
