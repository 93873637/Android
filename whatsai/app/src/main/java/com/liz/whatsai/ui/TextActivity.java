package com.liz.whatsai.ui;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.WhatsaiText;

import java.text.SimpleDateFormat;

public class TextActivity extends Activity implements View.OnClickListener {

    private WhatsaiText mTextNode = null;

    private EditText mEditContent;
    private ScrollView mEditScroll;

    private boolean mBtnSaveActive;
    private boolean mBtnCloudSaveActive;
    private boolean mContentDirty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        Node node = null;
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            long nodeId = bundle.getLong("NodeId");
            node = DataLogic.get((int) nodeId);
        }
        if (node == null) {
            Toast.makeText(this, "ERROR: NULL Node", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (node.getType() != ComDef.NODE_TYPE_TEXT) {
            Toast.makeText(this, "ERROR: Node type is NOT text", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mTextNode = (WhatsaiText) node;
        String titleName = DataLogic.getPath() + "/" + mTextNode.getName();
        ((TextView)findViewById(R.id.titlebar_title)).setText(titleName);

        findViewById(R.id.titlebar_menu).setOnClickListener(this);
        findViewById(R.id.titlebar_close).setOnClickListener(this);
        findViewById(R.id.toolbar_save).setOnClickListener(this);
        findViewById(R.id.toolbar_cloud_save).setOnClickListener(this);
        findViewById(R.id.toolbar_datetime).setOnClickListener(this);
        findViewById(R.id.toolbar_undo).setOnClickListener(this);
        findViewById(R.id.toolbar_redo).setOnClickListener(this);
        findViewById(R.id.toolbar_to_up).setOnClickListener(this);
        findViewById(R.id.toolbar_to_bottom).setOnClickListener(this);

        setToolbarSave(false);
        setToolbarCloudSave(false);
        initEditContent();
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
                }
                break;
            case R.id.toolbar_cloud_save:
                if (mBtnCloudSaveActive) {
                    cloudSave();
                }
                break;
            case R.id.toolbar_datetime:
                insertDateTime();
                break;
            case R.id.toolbar_undo:
                //#####@:
                break;
            case R.id.toolbar_redo:
                //#####@:
                break;
            case R.id.toolbar_to_up:
                toEditBegin();
                break;
            case R.id.toolbar_to_bottom:
                toEditEnd();
                break;
            default:
                break;
        }
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

    private void initEditContent() {
        mEditScroll = findViewById(R.id.edit_scroll);
        mEditContent = findViewById(R.id.edit_content);
        mEditContent.setText(mTextNode.getContent());
        mContentDirty = false;

        /*
        //##@: test data
        for(int i=0; i<500; i++) {
            mEditContent.append(i + ": aabbccddeeff\n");
        }
        //*/

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
                mContentDirty = true;
            }
        });

        toEditEnd();
    }

    private void toEditBegin() {
        mEditContent.setSelection(0);
        mEditScroll.scrollTo(0, 0);
    }

    private void toEditEnd() {
        mEditContent.setSelection(mEditContent.getText().length());
        mEditScroll.scrollTo(0, mEditContent.getHeight());
    }

    @Override
    public void onBackPressed() {
        localSave();
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void localSave() {
        LogUtils.d("TextActivity: localSave: mContentDirty = " + mContentDirty);
        if (mContentDirty) {
            mTextNode.setContent(mEditContent.getText().toString());
            DataLogic.local_save();
            setToolbarSave(false);
            mContentDirty = false;
        }
    }

    private void cloudSave() {
        LogUtils.d("TextActivity: cloudSave: mContentDirty = " + mContentDirty);
        if (mContentDirty) {
            mTextNode.setContent(mEditContent.getText().toString());
            DataLogic.local_save();
            DataLogic.cloud_save(this);
            setToolbarSave(false);
            setToolbarCloudSave(false);
            mContentDirty = false;
        }
    }

    private void insertDateTime() {
        int index = mEditContent.getSelectionStart();
        Editable editable = mEditContent.getText();
        String strDateTime = new SimpleDateFormat("[yy/MM/dd HH:mm]").format(new java.util.Date());
        editable.insert(index, strDateTime+"\n");
    }
}
