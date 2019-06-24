package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.cloudminds.feedback.net.IFUserFeedback.ImageBody;
import com.cloudminds.feedback.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cloud on 18-4-16.
 */

public class HistorysListActivity extends Activity implements View.OnClickListener {
    private ListView mlv;
    List<ImageBody> mHistorysList;
    TasksListAdapter adapter;
    private TaskDatabaseHelper mDatabaseHelper;

    private Toolbar mToolbarSet;
    private ImageButton mImgBtnReturn;

    private Button mBtnSubmit;
    private Button mBtnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historys_list_activity);

        mToolbarSet = findViewById(R.id.toolbar);
        mImgBtnReturn = mToolbarSet.findViewById(R.id.left_img_btn);
        mImgBtnReturn.setBackgroundResource(R.drawable.ic_return);
        mImgBtnReturn.setOnClickListener(this);
        TextView tvTitle = mToolbarSet.findViewById(R.id.tool_bar_title);
        tvTitle.setText(R.string.action_historys_list);

        mlv = (ListView) findViewById(R.id.lv_historys);
        mlv.addFooterView(new ViewStub(this));

        mDatabaseHelper = TaskDatabaseHelper.getInstance(this);
        mHistorysList = mDatabaseHelper.queryTaskData(TaskDatabaseHelper.TaskColumns.TASK_FLAG + "=1");

        adapter = new TasksListAdapter(this, mHistorysList);

        mlv.setAdapter(adapter);
        mlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTaskDetails(position);
            }
        });
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnDelete.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_img_btn:
                finish();
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
        Intent intent = new Intent(HistorysListActivity.this, TaskDetailsActivity.class);
        intent.putExtra("is_history", true);
        intent.putExtra("task_id", mHistorysList.get(position)._id);
        startActivity(intent);
    }

    private void deleteSelectItem() {

        List<ImageBody> listemp = new ArrayList<>();
        boolean[] checks = adapter.getChecks();
        for (int i = 0; i < checks.length; i++) {
            if (checks[i]) {
                LogUtils.i("===" + i + "===" + mHistorysList.get(i)._id);
                mDatabaseHelper.deleteTaskData(mHistorysList.get(i)._id);
                listemp.add(mHistorysList.get(i));
            }
        }
        mHistorysList.removeAll(listemp);
        checks = new boolean[mHistorysList.size()];
        adapter.setList(mHistorysList);
        adapter.setChecks(checks);
        adapter.notifyDataSetChanged();
    }
}
