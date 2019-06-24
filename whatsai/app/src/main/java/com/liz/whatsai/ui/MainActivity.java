package com.liz.whatsai.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.liz.whatsai.R;
import com.liz.whatsai.adapter.NodeListAdapter;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.utils.LogUtils;
import com.liz.whatsai.utils.SysUtils;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {

    private TextView mTextToolbar;
    private ImageButton mImageToolbar;

    final long DOUBLE_CLICK_TIME_SPAN = 200;  //unit by ms, best value?
    private long mWhatsaiLastClickTime;
    private long mListItemLastClickTime;
    private int mListItemLastClickPos;
    private Timer mListItemLastClickTimer;

    // For Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final static int REQUEST_NODE_INFO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageToolbar = findViewById(R.id.left_img_btn);
        mImageToolbar.setOnClickListener(this);

        mTextToolbar = findViewById(R.id.tool_bar_title);
        mTextToolbar.setText(DataLogic.getPath());

        ListView listView = findViewById(R.id.lv_items);
        listView.addFooterView(new ViewStub(this));
        listView.setAdapter(NodeListAdapter.getAdapter());
        listView.setOnItemClickListener(this);
        listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, ComDef.LIST_MENU_ID_ADD, 0, ComDef.LIST_MENU_NAME_ADD);
                menu.add(0, ComDef.LIST_MENU_ID_UPDATE, 0, ComDef.LIST_MENU_NAME_UPDATE);
                menu.add(0, ComDef.LIST_MENU_ID_DEL, 0, ComDef.LIST_MENU_NAME_DEL);
                menu.add(0, ComDef.LIST_MENU_ID_INFO, 0, ComDef.LIST_MENU_NAME_INFO);
            }
        });

        LinearLayout llWhatsai = findViewById(R.id.ll_whatsai);
        llWhatsai.setOnClickListener(this);

        mWhatsaiLastClickTime = 0;
        mListItemLastClickTime = 0;
        mListItemLastClickPos = -1;
        mListItemLastClickTimer = null;

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.left_img_btn:
                if (DataLogic.isRootActive()) {
                    //###@: TODO show drawer menu
                }
                else {
                    //not exit, go to upper node
                    DataLogic.goUpperNode();
                    updateView();
                }
                break;
            case R.id.ll_whatsai:
                onClickWhatsaiLayout();
                break;
        }

        //anyhow, stop ring if have
        SysUtils.stopRingTone();
    }

    protected void onClickWhatsaiLayout() {
        LogUtils.d("onClickWhatsaiLayout: mWhatsaiLastClickTime=" + mWhatsaiLastClickTime);

        long curTime = System.currentTimeMillis();
        long diffTime = curTime - mWhatsaiLastClickTime;
        LogUtils.d("onClickWhatsaiLayout: diffTime=" + diffTime);

        if (diffTime >= 0 && diffTime <= DOUBLE_CLICK_TIME_SPAN) {
            //trigger double click action
            AddNodeDlg.onAddNode(this);
            mWhatsaiLastClickTime = 0;
        } else {
            //take as first click
            mWhatsaiLastClickTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case ComDef.LIST_MENU_ID_ADD:
                AddNodeDlg.onAddNode(this);
                return true;
            case ComDef.LIST_MENU_ID_UPDATE:
                onModifyNode(info.id);
                return true;
            case ComDef.LIST_MENU_ID_DEL:
                onDelNode(info.id);
                return true;
            case ComDef.LIST_MENU_ID_INFO:
                onNodeInfo(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected void onModifyNode(long id) {
        final int pos = (int)id;
        final Node node = DataLogic.get(pos);
        final EditText et = new EditText(this);
        et.setText(node.getName());
        new AlertDialog
                .Builder(this)
                .setTitle("修改任务：")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.equals(et.getText().toString(), node.getName())) {
                            node.setName(et.getText().toString());
                            NodeListAdapter.onDataChanged();
                            DataLogic.setDirty(true);
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }

    protected void onDelNode(long id) {
        final int pos = (int)id;
        final Node node = DataLogic.get(pos);
        final TextView tv = new TextView(this);
        tv.setText(node.getName());
        new AlertDialog
                .Builder(this)
                .setTitle("删除任务：")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(tv)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(getApplicationContext(), et.getText().toString(), Toast.LENGTH_LONG).show();
                        DataLogic.delTask(pos);
                        NodeListAdapter.onDataChanged();
                    }
                }).setNegativeButton("取消", null).show();
    }

    protected void onNodeInfo(long id) {
        Intent intent = new Intent(MainActivity.this, NodeActivity.class);
        intent.putExtra("NodeId", id);
        startActivityForResult(intent, REQUEST_NODE_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_NODE_INFO) {
                updateView();
                DataLogic.setDirty(true);  //TODO: check if really dirty?
            }
        }
    }

    protected void updateView() {
        mTextToolbar.setText(DataLogic.getPath());
        if (DataLogic.isRootActive()) {
            mImageToolbar.setBackgroundResource(R.drawable.ic_menu);
        }
        else {
            mImageToolbar.setBackgroundResource(R.drawable.ic_return);
        }
        NodeListAdapter.onUpdateList();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        LogUtils.d("onItemClick: position=" + position + ", mListItemLastClickTime=" + mListItemLastClickTime);

        long curTime = System.currentTimeMillis();
        long diffTime = curTime - mListItemLastClickTime;
        LogUtils.d("onItemClick: diffTime=" + diffTime);

        if ((mListItemLastClickTime != 0) && (position == mListItemLastClickPos)
            && (diffTime >= 0 && diffTime <= DOUBLE_CLICK_TIME_SPAN)) {
            mListItemLastClickTime = 0;
            mListItemLastClickPos = -1;
            if (mListItemLastClickTimer != null) {
                mListItemLastClickTimer.cancel();
                mListItemLastClickTimer = null;
            }
            onItemDoubleClick(arg0, arg1, position, arg3);
        }
        else {
            //alter item bg color to show click effect
            //arg1.setBackgroundResource(R.color.colorLightBlue);

            mListItemLastClickTime = curTime;
            mListItemLastClickPos = position;
            mListItemLastClickTimer = new Timer();
            final AdapterView<?> arg0_ = arg0;
            final View arg1_ = arg1;
            final int position_ = position;
            final long arg3_ = arg3;
            mListItemLastClickTimer.schedule(new TimerTask() {
                public void run () {
                    MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                mListItemLastClickTime = 0;
                                mListItemLastClickPos = -1;
                                mListItemLastClickTimer.cancel();
                                mListItemLastClickTimer = null;
                                onItemSingleClick(arg0_, arg1_, position_, arg3_);
                            }
                        });
                }
            }, DOUBLE_CLICK_TIME_SPAN);
        }
    }

    @SuppressWarnings("unused")
    protected void onItemSingleClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        LogUtils.d("onItemSingleClick: position=" + position);
        Node node = DataLogic.get(position);
        if (node.isDir()) {
            DataLogic.setActiveNode(node);
            updateView();
        }
        else {
            node.reverseDone();
            if (node.isDone()) {
                arg1.setBackgroundColor(Color.GREEN);
            }
            else {
                arg1.setBackgroundColor(Color.WHITE);
            }
            DataLogic.setDirty(true);
        }
    }

    @SuppressWarnings("unused")
    protected void onItemDoubleClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        LogUtils.d("onItemDoubleClick: position=" + position);
        Node node = DataLogic.get(position);
        if (node.isDir()) {
            DataLogic.setActiveNode(node);
            updateView();
        }
        else {
            onNodeInfo(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (DataLogic.isRootActive()) {
            super.onBackPressed();//exit activity
            DataLogic.save();
        }
        else {
            //not exit, go to upper node
            DataLogic.goUpperNode();
            updateView();
        }
    }
}
