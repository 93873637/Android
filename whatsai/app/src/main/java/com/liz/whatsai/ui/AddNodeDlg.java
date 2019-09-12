package com.liz.whatsai.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.app.NodeListAdapter;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;

/**
 * AddNodeDlg.java
 * Dialog for Add Task and Task Group
 * Created by admin on 2018/9/28.
 */

class AddNodeDlg extends Dialog {

    RadioGroup mRgType;

    static void onAddNode(Context context) {
        new AddNodeDlg(context).openDlg();
    }

    private AddNodeDlg(Context context) {
        super(context);
        LogUtils.d("AddNodeDlg:AddNodeDlg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("AddNodeDlg:onCreate");
        View layout = View.inflate(getContext(), R.layout.add_node_dlg, null);
        this.setContentView(layout);
    }

    private void openDlg() {
        this.show();
        Window addDlgWindow = this.getWindow();
        if (addDlgWindow == null) {
            LogUtils.e("AddNodeDlg: addDlgWindow null");
            return;
        }

        final EditText editName = addDlgWindow.findViewById(R.id.editNodeName);
        mRgType = findViewById(R.id.rg_type);
        ((RadioButton) mRgType.getChildAt(0)).setChecked(true);

        Button btnOK = addDlgWindow.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String nodeName = editName.getText().toString();
                int nodeType = getNodeType();
                if (TextUtils.isEmpty(nodeName)) {
                    Toast.makeText(getContext(), "please input name", Toast.LENGTH_SHORT).show();
                }
                else {
                    DataLogic.createNode(nodeName, nodeType);
                    NodeListAdapter.onDataChanged();
                    AddNodeDlg.this.dismiss();
                }
            }
        });

        Button btnCancel = addDlgWindow.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AddNodeDlg.this.dismiss();
            }
        });
    }

    private int getNodeType() {
        switch (mRgType.getCheckedRadioButtonId()) {
            case R.id.rb_directory:
                return ComDef.NODE_TYPE_DIR;
            case R.id.rb_task:
                return ComDef.NODE_TYPE_TASK;
            case R.id.rb_text:
                return ComDef.NODE_TYPE_TEXT;
            case R.id.rb_taskgroup:
                return ComDef.NODE_TYPE_TASKGROUP;
            default:
                return ComDef.NODE_TYPE_FILE;
        }
    }
}
