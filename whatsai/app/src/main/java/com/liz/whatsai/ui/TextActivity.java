package com.liz.whatsai.ui;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;

import java.text.SimpleDateFormat;

public class TextActivity extends Activity implements View.OnClickListener {

    private Node mNode = null;
    private EditText mEditContent;
    private boolean mBtnSaveActive;
    private boolean mBtnCloudSaveActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

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

        ((TextView)findViewById(R.id.titlebar_title)).setText(DataLogic.getPath() + "/" + mNode.getName());
        mEditContent = findViewById(R.id.editContent);
        mEditContent.setText(mNode.getContent());

        findViewById(R.id.titlebar_menu).setOnClickListener(this);
        findViewById(R.id.titlebar_close).setOnClickListener(this);
        findViewById(R.id.toolbar_save).setOnClickListener(this);
        findViewById(R.id.toolbar_cloud_save).setOnClickListener(this);
        findViewById(R.id.toolbar_datetime).setOnClickListener(this);

        setToolbarSave(false);
        setToolbarCloudSave(false);
        setContentEdit();
    }

    private void setToolbarSave(boolean active) {
        mBtnSaveActive = active;
        ImageButton toolbarSave = findViewById(R.id.toolbar_save);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(active ? 1 : 0); //0; gray, 1: color
        ColorMatrixColorFilter grayColorFilter = new ColorMatrixColorFilter(cm);
        toolbarSave.getBackground().setColorFilter(grayColorFilter); //set null to restore
    }

    private void setToolbarCloudSave(boolean active) {
        mBtnCloudSaveActive = active;
        ImageButton toolbarCloudSave = findViewById(R.id.toolbar_cloud_save);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(active ? 1 : 0); //0; gray, 1: color
        ColorMatrixColorFilter grayColorFilter = new ColorMatrixColorFilter(cm);
        toolbarCloudSave.getBackground().setColorFilter(grayColorFilter);
    }

    private void setContentEdit() {
        mEditContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                LogUtils.d("beforeTextChanged: s = " + s + ", start = " + start +
                        ", count = " + count + ", after = " + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogUtils.d("onTextChanged: s = " + s + ", start = " + start +
                        ", before = " + before + ", count = " + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                LogUtils.d("afterTextChanged: " + s);
                setToolbarSave(true);
                setToolbarCloudSave(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.titlebar_menu:
            case R.id.titlebar_close:
                onBackPressed();
                break;
            case R.id.toolbar_save:
                if (mBtnSaveActive) {
                    localSave();
                    setToolbarSave(false);
                }
                break;
            case R.id.toolbar_cloud_save:
                if (mBtnCloudSaveActive) {
                    cloudSave();
                    setToolbarCloudSave(false);
                }
                break;
            case R.id.toolbar_datetime:
                insertDateTime();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        localSave();
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void localSave() {
        mNode.setContent(mEditContent.getText().toString());
        DataLogic.local_save();
    }

    private void cloudSave() {
        localSave();
        DataLogic.cloud_save(this);
    }

    private void insertDateTime() {
        int index = mEditContent.getSelectionStart();
        Editable editable = mEditContent.getText();
        String strDateTime = new SimpleDateFormat("[yy/MM/dd HH:mm]").format(new java.util.Date());
        editable.insert(index, strDateTime+"\n");
    }
}
