package com.liz.whatsai.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.liz.whatsai.logic.WSAudio;

import java.io.File;

public class AudioListView extends ListView {

    private AudioListAdapter mAdapter;

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
    public void onCreate(Context context, String audioDir) {
        mAdapter = new AudioListAdapter(audioDir);
        this.setAdapter(mAdapter);
        this.addFooterView(new ViewStub(context));
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
                if (mAdapter.isSelected(pos)) {
                    WSAudio.stopPlay();
                    mAdapter.clearSelected();
                }
                else {
                    mAdapter.updateSelected(pos);
                    WSAudio.startPlay(mAdapter.getAudioFilePath(pos));
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    //NOTE: you should call it on holder's onContextItemSelected
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemId = item.getItemId();
        int pos = (int)info.id;
        LogUtils.td("menuItemId = " + menuItemId);
        if (menuItemId == ComDef.AudioListMenu.PLAY.id) {
            if (!mAdapter.isSelected(pos)) {
                mAdapter.updateSelected(pos);
            }
            WSAudio.startPlay(this.getAudioFilePath(pos));
            return true;
        } else if (menuItemId == ComDef.AudioListMenu.STOP.id) {
            if (mAdapter.isSelected(pos)) {
                mAdapter.clearSelected();
                WSAudio.stopPlay();
            }
            return true;
        } else if (menuItemId == ComDef.AudioListMenu.DELETE.id) {
            onDeleteAudioFile((int) info.id);
            return true;
        } else {
            return false;
        }
    }

    private void onDeleteAudioFile(final int id) {
        final File f = mAdapter.getAudioFile(id);
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
                        FileUtils.removeFile(f);
                        mAdapter.removeItem(id);
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    public String getAudioFilePath(int id) {
        return mAdapter.getAudioFilePath(id);
    }

    public void updateList() {
        mAdapter.updateList();
    }

    public String getAudioFilesInfo() {
        return mAdapter.getAudioFilesInfo();
    }
}
