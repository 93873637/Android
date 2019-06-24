package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.adapter.TaskDatabaseHelper;
import com.cloudminds.feedback.adapter.TasksListAdapter;
import com.cloudminds.feedback.logic.Settings;
import com.cloudminds.feedback.net.IFUserFeedback.ImageBody;
import com.cloudminds.feedback.net.UploadTasksService;
import com.cloudminds.feedback.utils.ComUtils;
import com.cloudminds.feedback.utils.LogUtils;
import com.cloudminds.feedback.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cloud on 18-4-16.
 */

public class TasksListActivity extends Activity implements View.OnClickListener {
    private ListView mlv;
    List<ImageBody> mTasksList;
    TasksListAdapter adapter;
    private TaskDatabaseHelper mDatabaseHelper;

    private Toolbar mToolbarSet;
    private ImageButton mImgBtnReturn;

    private Button mBtnSubmit;
    private Button mBtnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_list_activity);

        mToolbarSet = findViewById(R.id.toolbar);
        mImgBtnReturn = mToolbarSet.findViewById(R.id.left_img_btn);
        mImgBtnReturn.setBackgroundResource(R.drawable.ic_return);
        mImgBtnReturn.setOnClickListener(this);
        TextView tvTitle = mToolbarSet.findViewById(R.id.tool_bar_title);
        tvTitle.setText(R.string.action_taskslist);

        mlv = (ListView) findViewById(R.id.lv_task);
        mlv.addFooterView(new ViewStub(this));

        mDatabaseHelper = TaskDatabaseHelper.getInstance(this);
        mTasksList = mDatabaseHelper.queryTaskData(TaskDatabaseHelper.TaskColumns.TASK_FLAG + "=0");

        adapter = new TasksListAdapter(this, mTasksList);

        mlv.setAdapter(adapter);
        mlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTaskDetails(position);
            }
        });

        mBtnSubmit = findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_img_btn:
                finish();
                break;
            case R.id.btn_submit:
                String user = Settings.getUserType(this);
                if(TextUtils.isEmpty(user)) {
                    ComUtils.showSetUserTypeDialog(this,null);
                }else {
                    if (adapter.getCheckNum() > 0) {
                        if (NetUtils.isConnectedOnWifi(this) || (NetUtils.isConnectedOnMobile(this) && !Settings.getFeedbackOnWifi(this))) {
                            submitSelectItem();
                        } else if (NetUtils.isConnectedOnMobile(this) && Settings.getFeedbackOnWifi(this)) {
                            Toast.makeText(this, R.string.using_mobiledata, Toast.LENGTH_SHORT).show();
                        } else if (!NetUtils.isConnected(this)) {
                            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_delete:
                if(adapter.getCheckNum()>0){
                    deleteSelectItem();
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(this, R.string.no_task_selected, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showTaskDetails(int position) {
        Intent intent = new Intent(TasksListActivity.this, TaskDetailsActivity.class);
        intent.putExtra("task_id", mTasksList.get(position)._id);
        startActivity(intent);
    }

    private void submitSelectItem() {
        List<ImageBody> listemp = new ArrayList<>();
        ArrayList<Integer> list_id = new ArrayList<>();
        boolean[] checks = adapter.getChecks();

        for (int i = 0; i < checks.length; i++) {
            if (checks[i]) {
                listemp.add(mTasksList.get(i));
                list_id.add(mTasksList.get(i)._id);
            }
        }

        if (!list_id.isEmpty()) {
            mTasksList.removeAll(listemp);
            checks = new boolean[mTasksList.size()];
            adapter.setList(mTasksList);
            adapter.setChecks(checks);
            adapter.notifyDataSetChanged();
            //上传选中的任务列表
            Intent intentUoload = new Intent(TasksListActivity.this, UploadTasksService.class);
            intentUoload.putIntegerArrayListExtra("list_id", list_id);
            startService(intentUoload);
        }

    }

    private void deleteSelectItem() {

        List<ImageBody> listemp = new ArrayList<>();
        boolean[] checks = adapter.getChecks();
        for (int i = 0; i < checks.length; i++) {
            if (checks[i]) {
                LogUtils.i("===" + i + "===" + mTasksList.get(i)._id);
                mDatabaseHelper.deleteTaskData(mTasksList.get(i)._id);
                listemp.add(mTasksList.get(i));
            }
        }
        mTasksList.removeAll(listemp);
        checks = new boolean[mTasksList.size()];
        adapter.setList(mTasksList);
        adapter.setChecks(checks);
        adapter.notifyDataSetChanged();
    }
}
