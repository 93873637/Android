package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.adapter.TaskDatabaseHelper;
import com.cloudminds.feedback.net.IFUserFeedback;

import java.util.List;

public  class TaskDetailsActivity  extends Activity {

    private Toolbar mToolbarSet;
    private ImageButton mImgBtnReturn;

    private TextView mTvReportId;
    private TextView mTvSaveTime;
    private TextView mTvDescription;
    private TextView mTvModule;
    private TextView mTvLogFile;
    private TextView mTvLogFileSize;

    private TaskDatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details_activity);
        mDatabaseHelper = TaskDatabaseHelper.getInstance(this);

        mToolbarSet = findViewById(R.id.toolbar);
        mImgBtnReturn = mToolbarSet.findViewById(R.id.left_img_btn);
        mImgBtnReturn.setBackgroundResource(R.drawable.ic_return);
        mImgBtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = mToolbarSet.findViewById(R.id.tool_bar_title);
        Intent intent = getIntent();
        boolean is_history = intent.getBooleanExtra("is_history", false);
        tvTitle.setText(is_history?R.string.history_details:R.string.task_details);

        mTvReportId=findViewById(R.id.tvreportid);
        mTvSaveTime=findViewById(R.id.tvsavetime);
        mTvDescription=findViewById(R.id.tv_description);
        mTvModule=findViewById(R.id.tvmodule);
        mTvLogFileSize=findViewById(R.id.tvlogsize);
        mTvLogFile=findViewById(R.id.tvlogfile);
        showDetails();
    }

    private void showDetails(){
        Intent intent = getIntent();
        int taskId = intent.getIntExtra("task_id", 0);
        List<IFUserFeedback.ImageBody> taskList = mDatabaseHelper.queryTaskData(TaskDatabaseHelper.TaskColumns._ID + "=" + taskId);
        if (!taskList.isEmpty()) {
            IFUserFeedback.ImageBody imageBody = taskList.get(0);
            mTvReportId.setText(String.valueOf(imageBody.report_id));
            mTvSaveTime.setText(imageBody.error_time);
            mTvDescription.setText(imageBody.content );
            mTvModule.setText(imageBody.module);
            mTvLogFile.setText(imageBody.attachment_name);
            mTvLogFileSize.setText(imageBody.attachment_size);
        }
    }
}

