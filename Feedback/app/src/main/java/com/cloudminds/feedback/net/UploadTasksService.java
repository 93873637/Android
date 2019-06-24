package com.cloudminds.feedback.net;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cloudminds.feedback.adapter.TaskDatabaseHelper;
import com.cloudminds.feedback.bean.ResponseInfo;
import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.DataLogic;
import com.cloudminds.feedback.utils.LogUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * UploadTasksService.java: upload selected tasks
 * Created by cloud on 18-4-16.
 */

public class UploadTasksService extends Service {

    Boolean isUploadOK = false;
    private TaskDatabaseHelper mDatabaseHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<Integer>list_id=intent.getIntegerArrayListExtra("list_id");
        mDatabaseHelper = TaskDatabaseHelper.getInstance(this);

        if (!list_id.isEmpty()) {
            String idString = TextUtils.join(",", list_id);
            final List<IFUserFeedback.ImageBody> list = mDatabaseHelper.queryTaskData
                    (TaskDatabaseHelper.TaskColumns._ID + " IN (" + idString + ")");
            LogUtils.i(list.size() + "=====>size");
            new Thread() {
                public void run() {
                    onSubmitTasks(list);
                }
            }.start();
        }

        return Service.START_NOT_STICKY;
    }

    private void onSubmitTasks(List<IFUserFeedback.ImageBody> list) {
        for (IFUserFeedback.ImageBody imageBody : list) {
            uploadMessages(imageBody);
            uploadAttachments(ComDef.getOfflineLogZipPath() + imageBody.attachment_name);
        }
    }

    private void uploadAttachments(String attachFilePath) {
        LogUtils.d("uploadAttachments: attachFilePath=" + attachFilePath);
        if (!TextUtils.isEmpty(attachFilePath)) {
            Intent intent = new Intent(UploadTasksService.this, UploadService.class);
            intent.putExtra(ComDef.FILE_ABSOLUTE_PATH, attachFilePath);
            startService(intent);
        }
    }

    private void uploadMessages(final IFUserFeedback.ImageBody imageBody) {
        final int RESPONSE_CODE_SUCCESS = 200;

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
               LogUtils.i("httpLog: " + message);  //too many logs, open it for test only
            }
        }); //##@:
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        Retrofit sRetrofit = new Retrofit.Builder()
                .baseUrl(ComDef.WEB_SERVER_BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
        IFUserFeedback mIfUserFeedback = sRetrofit.create(IFUserFeedback.class);

        DataLogic.uploadImages(this, imageBody, mIfUserFeedback, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == RESPONSE_CODE_SUCCESS) {
                    try {
                        String result = response.body().string();
                        ResponseInfo info = new Gson().fromJson(result, ResponseInfo.class);
                        imageBody.report_id = info.getReport_id();
                        mDatabaseHelper.updateTaskData(imageBody._id, imageBody.report_id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isUploadOK = true;
                } else {
                    isUploadOK = false;
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isUploadOK = false;
            }
        });
    }
}
