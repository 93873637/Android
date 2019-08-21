package com.liz.whatsai.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.SysUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.app.NodeListAdapter;
import com.liz.whatsai.app.ThisApp;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {

    // For Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    final long DOUBLE_CLICK_TIME_SPAN = 200;  //unit by ms, best value?
    private long mWhatsaiLastClickTime;
    private long mListItemLastClickTime;
    private int mListItemLastClickPos;
    private Timer mListItemLastClickTimer;

    private final static int REQUEST_NODE_INFO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        TextView tvAppInfo = navigationView.getHeaderView(0).findViewById(R.id.textAppInfo);
        tvAppInfo.setText(ThisApp.mAppVersion);

        ListView listView = findViewById(R.id.lv_items);
        listView.addFooterView(new ViewStub(this));
        listView.setAdapter(NodeListAdapter.getAdapter());
        listView.setOnItemClickListener(this);
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
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
    public void onClick(View v){
        switch(v.getId()){
            case R.id.ll_whatsai:
            onClickWhatsaiLayout();
            break;
        }

        //anyhow, stop ring if have
        //####@: SysUtils.stopRingTone();
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

    protected void onNodeInfo(long id) {
        Intent intent = new Intent(MainActivity.this, NodeActivity.class);
        intent.putExtra("NodeId", id);
        startActivityForResult(intent, REQUEST_NODE_INFO);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            if (DataLogic.isRootActive()) {
                super.onBackPressed(); //exit activity
                DataLogic.save();
            }
            else {
                //not exit, go to upper node
                DataLogic.goUpperNode();
                updateView();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    protected void updateView() {
        //####@: mToolbarTitle.setText(DataLogic.getPath());
        NodeListAdapter.onUpdateList();
    }

}
