package com.liz.whatsai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.Reminder;

public class NodeActivity extends Activity implements View.OnClickListener {

    private Node mNode = null;

    private TextView mtvToolbar;
    private EditText mEditName;
    private EditText mEditSummary;
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

        mtvToolbar = findViewById(R.id.titlebar_title);
        mtvToolbar.setText(DataLogic.getPath() + "/" + mNode.getName());

        mEditName = findViewById(R.id.editNodeName);
        mCheckDone = findViewById(R.id.cbDone);
        mEditName.setText(mNode.getName());
        mCheckDone.setChecked(mNode.isDone());

        setCheckedType();

        mEditRemind = findViewById(R.id.editRemind);
        mEditRemind.setText(mNode.getRemindString());

        mEditSummary = findViewById(R.id.editSummary);
        mEditSummary.setText(mNode.getSummary());

        findViewById(R.id.titlebar_menu).setOnClickListener(this);
    }

    public void setCheckedType() {
        switch (mNode.getType()) {
            case ComDef.NODE_TYPE_DIR:
                ((RadioButton)findViewById(R.id.rb_directory)).setChecked(true);
                break;
            case ComDef.NODE_TYPE_TASKGROUP:
                ((RadioButton)findViewById(R.id.rb_taskgroup)).setChecked(true);
                break;
            case ComDef.NODE_TYPE_TASK:
                ((RadioButton)findViewById(R.id.rb_task)).setChecked(true);
                break;
            case ComDef.NODE_TYPE_TEXT:
                ((RadioButton)findViewById(R.id.rb_text)).setChecked(true);
                break;
            default:
                ((RadioButton)findViewById(R.id.rb_file)).setChecked(true);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.titlebar_menu:
                this.onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNode != null) {
            mNode.setName(mEditName.getText().toString());
            mNode.setSummary(mEditSummary.getText().toString());
            mNode.setDone(mCheckDone.isChecked());
            mNode.setRemindString(mEditRemind.getText().toString());
            Reminder.checkRemind(mNode);
        }
        setResult(RESULT_OK);
        super.onBackPressed();//exit activity
    }
}
