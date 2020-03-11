package com.liz.whatsai.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import com.liz.whatsai.app.AudioListAdapter;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WhatsaiAudio;

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
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int itemId = item.getItemId();
        if (itemId == ComDef.AudioListMenu.PLAY.id) {
            //####@: mListener.playAudio(mAudioListAdapter.getAudioFilePath((int)info.id));
            return true;
        }
        else if (itemId == ComDef.AudioListMenu.STOP.id) {
            WhatsaiAudio.stopPlay();
            return true;
        }

        return false;
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
