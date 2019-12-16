package com.liz.multidialer.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.liz.androidutils.LogUtils;
import com.liz.multidialer.logic.ComDef;

/**
 * Created by liz on 17-12-27.
 */

public class UploadService extends Service {

    private String mFileAbsolutePath;  //file name with full path

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFileAbsolutePath = intent.getStringExtra(ComDef.UPLOAD_FILE_PATH);
        LogUtils.d("UploadService.onStartCommand: mFileAbsolutePath=" + mFileAbsolutePath);

        //create a new thread in case NetworkOnMainThreadException
        new Thread() {
            public void run() {
                uploadFile(getApplicationContext(), mFileAbsolutePath);
            }
        }.start();

        return Service.START_NOT_STICKY;
    }

    public void uploadFile(final Context context, String fileAbsolutePath) {
        LogUtils.d("UploadService.uploadFile: fileAbsolutePath=" + fileAbsolutePath);

        FTPManager ftpManager = null;
        try {
            ftpManager = new FTPManager();
            if (ftpManager.openFTP()) {
                LogUtils.d("open ftp successfully.");
                if (ftpManager.uploadFile(context, fileAbsolutePath)) {
                    LogUtils.d("upload file successfully.");
                } else {
                    LogUtils.e("upload file failed.");
                }
            }
        } catch (Exception e) {
            LogUtils.e("upload file exception: " + e.toString());
        }

        if (ftpManager != null) {
            ftpManager.closeFTP();
        }
    }
}
