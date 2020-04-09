package com.liz.whatsai.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.whatsai.app.AudioListAdapter;
import com.liz.whatsai.logic.ComDef;

import java.io.File;

public class AudioListView extends ListView {

    private AudioListAdapter mAdapter;
    private Activity mActivity;

    public AudioListView(Context context) {
        super(context);
    }

    public AudioListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //NOTE: you should call it on holder's onCreate
    public void onCreate(final Activity activity, String audioDir) {
        mAdapter = new AudioListAdapter(audioDir);
        mActivity = activity;
        this.setAdapter(mAdapter);
        this.addFooterView(new ViewStub(activity));
        this.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                for (ComDef.AudioListMenu c : ComDef.AudioListMenu.values()) {
                    menu.add(0, c.id, 0, c.name);
                }
            }
        });
        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                LogUtils.td("pos=" + pos + ", arg1=" + arg1 + ", arg3=" + arg3);
                mAdapter.onClick(pos);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    /**
     * NOTE: you should call it on holder's onContextItemSelected
     * @param item:
     * @return true if handled, or false
     */
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemId = item.getItemId();
        int pos = (int) info.id;
        LogUtils.td("menuItemId = " + menuItemId);
        if (menuItemId == ComDef.AudioListMenu.PLAY_MORE.id) {
            onPlayMore(pos);
            return true;
        } else if (menuItemId == ComDef.AudioListMenu.DELETE.id) {
            onDelete(pos);
            return true;
        } else if (menuItemId == ComDef.AudioListMenu.RELOAD.id) {
            onReload();
            return true;
        } else if (menuItemId == ComDef.AudioListMenu.DELETE_ALL.id) {
            onDeleteAll();
            return true;
        } else {
            return false;
        }
    }

    private void onPlayMore(final int pos) {
        AudioPlayDlg.openPlayDlg(mActivity, mAdapter.getAudioFile(pos));
    }

    private void onDelete(final int pos) {
        final File f = mAdapter.getAudioFile(pos);
        final TextView tv = new TextView(this.getContext());
        tv.setText(f.getAbsolutePath());
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);
        new AlertDialog
                .Builder(this.getContext())
                .setTitle("Confirm Delete?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(tv)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAdapter.removeItem(pos);
                        FileUtils.removeFile(f);
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private void onReload() {
        mAdapter.updateList();
    }

    private void onDeleteAll() {
        String text = "\nWARNING: \nALL AUDIO FILES WILL BE DELETED! \nARE YOU SURE?";

        final TextView tv = new TextView(this.getContext());
        tv.setText(text);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(50, 10, 10, 10);
        new AlertDialog
                .Builder(this.getContext())
                .setView(tv)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FileUtils.clearDir(mAdapter.getAudioDir());
                        mAdapter.updateList();
                    }
                })
                .setNegativeButton("NO", null)
                .show();
    }

    public String getAudioFilesInfo() {
        return mAdapter.getAudioFilesInfo();
    }

    public void updateList() {
        mAdapter.updateList();
    }
}
