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
import android.widget.Toast;

import com.liz.whatsai.R;
import com.liz.whatsai.adapter.NodeListAdapter;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.utils.LogUtils;

/**
 * AddNodeDlg.java
 * Dialog for Add Task and Task Group
 * Created by admin on 2018/9/28.
 */

class AddNodeDlg extends Dialog {

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
        final CheckBox cbTaskgroup = addDlgWindow.findViewById(R.id.cbTaskgroup);

        Button btnOK = addDlgWindow.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                boolean taskgroupChecked = cbTaskgroup.isChecked();
                String nodeName = editName.getText().toString();
                if (TextUtils.isEmpty(nodeName)) {
                    Toast.makeText(getContext(), "please input name", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (taskgroupChecked) {
                        DataLogic.addTaskGroup(editName.getText().toString());
                    } else {
                        DataLogic.addTask(editName.getText().toString());
                    }
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
}
