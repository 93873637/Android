package com.liz.whatsai.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.app.NodeListAdapter;
import com.liz.whatsai.app.ThisApp;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.storage.WhatsaiStorage;

import java.util.Timer;
import java.util.TimerTask;

public class WhatsaiActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {

    final long DOUBLE_CLICK_TIME_SPAN = 200;  //unit by ms, best value?
    private long mWhatsaiLastClickTime;
    private long mListItemLastClickTime;
    private int mListItemLastClickPos;
    private Timer mListItemLastClickTimer;

    private final static int REQUEST_NODE_PROPERTIES = 2;
    private final static int REQUEST_NODE_TEXT_EDIT = 3;

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
                for (ComDef.ListMenu c : ComDef.ListMenu.values()) {
                    menu.add(0, c.id, 0, c.name);
                }
            }
        });

        LinearLayout llWhatsai = findViewById(R.id.ll_whatsai);
        llWhatsai.setOnClickListener(this);

        mWhatsaiLastClickTime = 0;
        mListItemLastClickTime = 0;
        mListItemLastClickPos = -1;
        mListItemLastClickTimer = null;
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
        int itemId = item.getItemId();
        if (itemId == ComDef.ListMenu.OPEN.id) {
            onOpenNode(info.id);
            return true;
        } else if (itemId == ComDef.ListMenu.ADD.id) {
            AddNodeDlg.onAddNode(this);
            return true;
        } else if (itemId == ComDef.ListMenu.MODIFY.id) {
            onModifyNode(info.id);
            return true;
        } else if (itemId == ComDef.ListMenu.DEL.id) {
            onDelNode(info.id);
            return true;
        } else if (itemId == ComDef.ListMenu.PROP.id) {
            openNodeProperties(info.id);
            return true;
        } else {
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
                .setTitle("Modify File/Path: ")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.equals(et.getText().toString(), node.getName())) {
                            node.setName(et.getText().toString());
                            NodeListAdapter.onDataChanged();
                            DataLogic.setDirty(true);
                        }
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    protected void onDelNode(long id) {
        final int pos = (int)id;
        final Node node = DataLogic.get(pos);
        final TextView tv = new TextView(this);
        tv.setText(node.getName());
        new AlertDialog
                .Builder(this)
                .setTitle("Delete File/Path: ")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(tv)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(getApplicationContext(), et.getText().toString(), Toast.LENGTH_LONG).show();
                        DataLogic.delTask(pos);
                        NodeListAdapter.onDataChanged();
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    protected void onOpenNode(final long id) {
        final Node node = DataLogic.get((int) id);
        if (node == null) {
            LogUtils.e("onOpenNode: invalid node id " + id);
        } else {
            if (!node.hasPassword()) {
                //direct open
                openNode(id);
            }
            else {
                //need password
                final EditText et = new EditText(this);
                et.setTransformationMethod(new PasswordTransformationMethod());
                String title = "Please Input Password of \"" + node.getName() + "\": ";
                new AlertDialog
                        .Builder(this)
                        .setTitle(title)
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String inputPassword = et.getText().toString();
                                if (node.samePassword(inputPassword)) {
                                    openNode(id);
                                }
                                else {
                                    Toast.makeText(WhatsaiActivity.this, "Password Incorrect", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNegativeButton("Cancel", null).show();

            }
        }
    }

    protected void openNode(long id) {
        final Node node = DataLogic.get((int) id);
        if (node == null) {
            LogUtils.e("openNode: invalid node id " + id);
        } else {
            if (node.getType() == ComDef.NODE_TYPE_TEXT) {
                openTextNode(id);
            } else {
                openNodeProperties(id);
            }
        }
    }

    protected void openNodeProperties(long id) {
        Intent intent = new Intent(WhatsaiActivity.this, NodeActivity.class);
        intent.putExtra("NodeId", id);
        startActivityForResult(intent, REQUEST_NODE_PROPERTIES);
    }

    protected void openTextNode(long id) {
        Intent intent = new Intent(WhatsaiActivity.this, TextActivity.class);
        intent.putExtra("NodeId", id);
        startActivityForResult(intent, REQUEST_NODE_TEXT_EDIT);
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
            mListItemLastClickTime = curTime;
            mListItemLastClickPos = position;
            mListItemLastClickTimer = new Timer();
            final AdapterView<?> arg0_ = arg0;
            final View arg1_ = arg1;
            final int position_ = position;
            final long arg3_ = arg3;
            mListItemLastClickTimer.schedule(new TimerTask() {
                public void run () {
                    WhatsaiActivity.this.runOnUiThread(new Runnable() {
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
            onOpenNode(position);
        }
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
                DataLogic.local_save();
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
            startActivity(new Intent(WhatsaiActivity.this, AudioActivity.class));
            return true;
        }
        else if (id == R.id.action_cloud_backup) {
            WhatsaiStorage.cloud_save(this);
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
        WhatsaiActivity.this.setTitle(DataLogic.getPath());
        NodeListAdapter.onUpdateList();
    }
}
