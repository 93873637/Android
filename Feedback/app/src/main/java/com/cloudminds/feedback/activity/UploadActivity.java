package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.adapter.TaskDatabaseHelper;
import com.cloudminds.feedback.app.ThisApp;
import com.cloudminds.feedback.bean.ResponseInfo;
import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.DataLogic;
import com.cloudminds.feedback.logic.LogConfig;
import com.cloudminds.feedback.logic.Settings;
import com.cloudminds.feedback.net.IFUserFeedback;
import com.cloudminds.feedback.net.UploadService;
import com.cloudminds.feedback.utils.DeviceInfoUtils;
import com.cloudminds.feedback.utils.FileUtils;
import com.cloudminds.feedback.utils.LogUtils;
import com.cloudminds.feedback.utils.SysUtils;
import com.cloudminds.feedback.utils.ZipUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class UploadActivity extends Activity {

    private static final int RESPONSE_CODE_SUCCESS = 200;

    private IFUserFeedback mIfUserFeedback;
    private String mFeedbackType;
    private String mMsgCont;

    private String mErrorType;
    private String mPackageName;

    private String mLogFilePath;

    private String mAttachFilePath;  //attach file name with full path
    private String mAttachFileName;  //attach file name for web server downloading

    private IFUserFeedback.ImageBody imageBody = new IFUserFeedback.ImageBody();
    private DeviceInfoUtils mDeviceInfoUtils;
    private TaskDatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("UploadActivity.onCreate: " + ThisApp.mAppVersionShow);
        mDeviceInfoUtils = new DeviceInfoUtils(this);
        mDatabaseHelper = TaskDatabaseHelper.getInstance(this);

        Intent intent = getIntent();
        mFeedbackType = intent.getStringExtra(ComDef.FEEDBACK_MSG_TYPE);
        mMsgCont = intent.getStringExtra(ComDef.FEEDBACK_MSG_CONT);
        mErrorType = intent.getStringExtra(ComDef.FEEDBACK_ERROR_TYPE);
        mPackageName = intent.getStringExtra(ComDef.FEEDBACK_PACKAGE_NAME);

        String filePath = intent.getStringExtra(ComDef.FEEDBACK_FILE_PATH);
        String fileName = intent.getStringExtra(ComDef.FEEDBACK_FILE_NAME);

        //msg cont log not too long
        String msgContLog = "";
        int msgContLen = mMsgCont.length();
        final int MSG_CONT_LOG_MAX = 32;
        if (msgContLen > MSG_CONT_LOG_MAX) {
            msgContLog = mMsgCont.substring(0, MSG_CONT_LOG_MAX) + "...(" + msgContLen + ")";
        }
        else {
            msgContLog = mMsgCont;
        }
        LogUtils.d("UploadActivity.onCreate: mFeedbackType=" + mFeedbackType + ", mMsgCont=" + msgContLog);
        LogUtils.d("UploadActivity.onCreate: mErrorType=" + mErrorType + ", mPackageName=" + mPackageName);
        LogUtils.d("UploadActivity.onCreate: filePath=" + filePath + ", fileName=" + fileName);

        if (TextUtils.isEmpty(mFeedbackType)) {
            LogUtils.e("ERROR: UploadActivity.initData: no message type");
            finish();
        }

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                //##@: LogUtils.i("httpLog: " + message);  //too many logs, open it for test only
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        Retrofit sRetrofit = new Retrofit.Builder()
                .baseUrl(ComDef.WEB_SERVER_BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
        mIfUserFeedback = sRetrofit.create(IFUserFeedback.class);

        //directly submit the message and attach files if network available
        if (LogConfig.isLogdRunning()) {
            if (isNetworkAvailableForUpload()) {
                onSubmit();
            } else {
                LogUtils.w("UploadActivity: no suitable network for upload, save for network available.");
                onSave();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.tip_cannot_submit, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected boolean isNetworkAvailableForUpload() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            LogUtils.e("UploadActivity: get connectivity manager failed.");
            return false;
        }

        if (connMgr.getActiveNetworkInfo() == null) {
            LogUtils.e("UploadActivity: get active network info failed.");
            return false;
        }

        if (!connMgr.getActiveNetworkInfo().isAvailable()) {
            LogUtils.e("UploadActivity: network is not available.");
            return false;
        }

        int netType = connMgr.getActiveNetworkInfo().getType();
        LogUtils.d("UploadActivity: network type " + netType);

        if (Settings.getFeedbackOnWifi(this)) {
            LogUtils.e("UploadActivity: only upload feedback on wifi.");
            return (netType == ConnectivityManager.TYPE_WIFI);
        }
        else {
            LogUtils.d("UploadActivity: no wifi limit for upload.");
            return true;
        }
    }

    protected void onSubmit() {
        new UploadLogTask().execute();
    }

    private void onSave() {
        buildAttachments();
        saveUploadTask();
        finish();
    }

    private String[] buildAttachments() {
        LogUtils.d("UploadActivity.buildAttachments: using offline log files for app errors");
        mLogFilePath = SysUtils.getSystemProperty(this, ComDef.PROP_SYS_LOG_PATH, "");

        LogUtils.d("UploadActivity.buildAttachments: copy anr/tombstones for offline log files");
        LogConfig.syncSysFiles(true);
        LogUtils.d("UploadActivity.buildAttachments: mLogFilePath=" + mLogFilePath);

        List<String> fileAbsoluteList = new ArrayList<>();
        if (!TextUtils.isEmpty(mLogFilePath)) {
            fileAbsoluteList.add(mLogFilePath);
        }

        String zipFile = FileUtils.genLogZipName(UploadActivity.this);
        String zipPath = ComDef.getOfflineLogZipPath() + zipFile;

        if (ZipUtils.zipFiles(zipPath, fileAbsoluteList)) {
            mAttachFilePath = zipPath;
            mAttachFileName = zipFile;
            LogUtils.d("UploadActivity.buildAttachments: mAttachFilePath=" + mAttachFilePath);

            // restart logd to catch new log to file again(NOTE: current log will be clear)
            if (LogConfig.isLogdRunning()) {
                //delay a while for cmlogd process restart(in case failed to clear log)
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        LogConfig.restartLogd();
                    }
                }, ComDef.CMLOGD_RESTART_DELAY);
            }
            else {
                LogUtils.d("UploadActivity.buildAttachments: logd not running, no restart.");
            }
        }
        else {
            mAttachFilePath = "";
            mAttachFileName = "";
            LogUtils.e("UploadActivity.buildAttachments: ERROR: zip files for attachment failed.");
        }
        return new String[] { mAttachFileName, mAttachFilePath };
    }

    private void uploadAttachments(String attachFilePath) {
        LogUtils.d("uploadAttachments: attachFilePath=" + attachFilePath);
        if (!TextUtils.isEmpty(attachFilePath)) {
            Intent intent = new Intent(UploadActivity.this, UploadService.class);
            intent.putExtra(ComDef.FILE_ABSOLUTE_PATH, attachFilePath);
            startService(intent);
        }
    }

    private void saveUploadTask() {
        imageBody.task_flag = 0;
        imageBody.content = mMsgCont;
        imageBody.attachment_name = mAttachFileName;
        imageBody.attachment_size = FileUtils.getFormattedFileSize(mAttachFilePath);
        imageBody.error_type = mErrorType;
        imageBody.module = mPackageName;
        imageBody.app_version = mDeviceInfoUtils.getAppVersion(mPackageName);
        imageBody.battery_level = mDeviceInfoUtils.getBatteryLevel();
        imageBody.network_type = mDeviceInfoUtils.getNetworkType();
        imageBody.cpu_usage = mDeviceInfoUtils.getCpuUsage();
        imageBody.memory_usage = mDeviceInfoUtils.getMemoryUsage();
        imageBody.storage_usage = mDeviceInfoUtils.getStorageUsage();
        imageBody.is_monkey = mDeviceInfoUtils.isUserAMonkey();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        imageBody.error_time = format.format(new Date(System.currentTimeMillis()));
        imageBody.device_model = mDeviceInfoUtils.getDeviceModel();
        imageBody.imei1 = mDeviceInfoUtils.getIMEI1();
        imageBody.build_version = mDeviceInfoUtils.getBuildVersion();
        imageBody.hardware_version = mDeviceInfoUtils.getHardwareVersion();
        imageBody.android_version = mDeviceInfoUtils.getAndroidVersion();
        mDatabaseHelper.insertTaskData(imageBody);
    }

    private class UploadLogTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected void onPreExecute() {
            imageBody.task_flag = 1;
            imageBody.content = mMsgCont;
            imageBody.error_type = mErrorType;
            imageBody.module = mPackageName;
            imageBody.app_version = mDeviceInfoUtils.getAppVersion(mPackageName);
            imageBody.battery_level = mDeviceInfoUtils.getBatteryLevel();
            imageBody.network_type = mDeviceInfoUtils.getNetworkType();
            imageBody.cpu_usage = mDeviceInfoUtils.getCpuUsage();
            imageBody.memory_usage = mDeviceInfoUtils.getMemoryUsage();
            imageBody.storage_usage = mDeviceInfoUtils.getStorageUsage();
            imageBody.is_monkey = mDeviceInfoUtils.isUserAMonkey();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            imageBody.error_time = format.format(new Date(System.currentTimeMillis()));
            imageBody.device_model = mDeviceInfoUtils.getDeviceModel();
            imageBody.imei1 = mDeviceInfoUtils.getIMEI1();
            imageBody.build_version = mDeviceInfoUtils.getBuildVersion();
            imageBody.hardware_version = mDeviceInfoUtils.getHardwareVersion();
            imageBody.android_version = mDeviceInfoUtils.getAndroidVersion();
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            return buildAttachments();
        }

        @Override
        protected void onPostExecute(String[] attachFileInfo) {
            imageBody.attachment_name = attachFileInfo[0];
            imageBody.attachment_size = FileUtils.getFormattedFileSize(attachFileInfo[1]);

            DataLogic.uploadImages(UploadActivity.this, imageBody, mIfUserFeedback, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == RESPONSE_CODE_SUCCESS) {
                        try {
                            String result = response.body().string();
                            ResponseInfo info = new Gson().fromJson(result, ResponseInfo.class);
                            imageBody.report_id = info.getReport_id();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String okTip = getResources().getString(R.string.tip_submit_ok);
                        LogUtils.tipD(okTip);
                    } else {
                        String errTip = getResources().getString(R.string.tip_submit_failed) + "(" + response.code() + ")";
                        LogUtils.tipE(errTip);
                        LogUtils.e("uploadImages response error: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    String errTip = getResources().getString(R.string.tip_submit_failed) + "(" + DataLogic.getErrorMessage(t) + ")";
                    LogUtils.tipE(errTip);
                    //Toast.makeText(UploadActivity.this, errTip, Toast.LENGTH_LONG).show();
                    LogUtils.e("uploadImages failed: " + t.toString());
                }
            });

            uploadAttachments(attachFileInfo[1]);
            finish();
        }
    }
}
