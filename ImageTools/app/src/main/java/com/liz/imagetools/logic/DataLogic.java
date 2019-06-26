package com.liz.imagetools.logic;

import android.content.Context;

import com.liz.imagetools.utils.LogUtils;

import java.io.File;

public class DataLogic {
    public static void init(Context context) {

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
    }
}
