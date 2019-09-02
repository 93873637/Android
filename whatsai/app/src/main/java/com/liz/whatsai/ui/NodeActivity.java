package com.liz.whatsai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.whatsai.R;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.Reminder;

public class NodeActivity extends Activity implements View.OnClickListener {

    private Node mNode = null;

    private TextView mtvToolbar;
    private EditText mEditName;
    private EditText mEditDetail;
    private EditText mEditRemind;
    private CheckBox mCheckDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            long nodeId = bundle.getLong("NodeId");
            mNode = DataLogic.get((int) nodeId);
        }

        //can't show null node
        if (mNode == null) {
            Toast.makeText(this, "ERROR: NULL Node", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mtvToolbar = findViewById(R.id.toolbar_title);
        mtvToolbar.setText(DataLogic.getPath() + "/" + mNode.getName());

        mEditName = findViewById(R.id.editNodeName);
        mCheckDone = findViewById(R.id.cbDone);
        mEditName.setText(mNode.getName());
        mCheckDone.setChecked(mNode.isDone());

        mEditRemind = findViewById(R.id.editRemind);
        mEditRemind.setText(mNode.getRemindString());

        mEditDetail = findViewById(R.id.editDetail);
        mEditDetail.setText(mNode.detail);

        findViewById(R.id.toolbar_menu).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.toolbar_menu:
                this.onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNode != null) {
            mNode.setName(mEditName.getText().toString());
            mNode.detail = mEditDetail.getText().toString();
            mNode.setDone(mCheckDone.isChecked());
            mNode.setRemindString(mEditRemind.getText().toString());
            Reminder.checkRemind(mNode);
        }
        setResult(RESULT_OK);
        super.onBackPressed();//exit activity
    }
}
