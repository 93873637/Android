package com.liz.whatsai.app;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WSListener;
import com.liz.whatsai.logic.WSPlayer;

import java.io.File;
import java.util.ArrayList;

public class AudioListAdapter extends BaseAdapter {

    private ArrayList<File> mFileList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private String mAudioDir;
    private int mSelected = ComDef.INVALID_LIST_POS;
    private WSPlayer mPlayer = null;

    public AudioListAdapter() {
        initAdapter(ComDef.WHATSAI_AUDIO_DIR);
    }

    public AudioListAdapter(String audioDir) {
        initAdapter(audioDir);
    }

    private void initAdapter(String audioDir) {
        mAudioDir = audioDir;
        mPlayer = new WSPlayer();
        loadListData();
        mLayoutInflater = LayoutInflater.from(MyApp.getAppContext());
    }

    private MediaPlayer.OnCompletionListener mPlayCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtils.td("onCompletion");
        }
    };

    public File getAudioFile(long pos) {
        return (File)this.getItem((int)pos);
    }

    public String getAudioFilePath(long pos) {
        return ((File)this.getItem((int)pos)).getAbsolutePath();
    }

    public void setSelected(int pos) {
        mSelected = pos;
    }

    public int getSelected() {
        return mSelected;
    }

    public void onItemClick(int pos) {
        if (isSelected(pos)) {
            mPlayer.stopPlay();
        } else {
            updateSelected(pos);
            mPlayer.startPlay(getAudioFilePath(pos), mPlayCompletionListener);
        }
    }

    public void clearSelected() {
        mSelected = ComDef.INVALID_LIST_POS;
        notifyDataSetChanged();
    }

    public void updateSelected(int pos) {
        setSelected(pos);
        notifyDataSetChanged();
    }

    public boolean isSelected(int pos) {
        return hasSelected() && pos == mSelected;
    }

    public boolean hasSelected() {
        return mSelected != ComDef.INVALID_LIST_POS;
    }

    public String getAudioFilesInfo() {
        return this.getListInfo();
    }

    public String getAudioDir() {
        return mAudioDir;
    }

    public void removeItem(int id) {
        if (isSelected(id)) {
            clearSelected();
            mPlayer.stopPlay();
        }
        mFileList.remove(id);
        mSelected = ComDef.INVALID_LIST_POS;
        notifyDataSetChanged();
    }

    public void playItem(final int pos) {
        if (!isSelected(pos)) {
            updateSelected(pos);
        }
        mPlayer.startPlay(this.getAudioFilePath(pos), mPlayCompletionListener);
    }

    public void stopPlay() {
        mPlayer.stopPlay();
    }

    public void updateList() {
        mPlayer.stopPlay();
        loadListData();
        mSelected = ComDef.INVALID_LIST_POS;
        notifyDataSetChanged();
    }

    private void loadListData() {
        mFileList.clear();
        mFileList = FileUtils.getFileArrayList(mAudioDir, FileUtils.ORDER_BY_DATE_DESC);
    }

    private String getListInfo() {
        String info = "TOTAL <font color=\"#ff0000\">" + mFileList.size() + "</font> FILES";
        long sizeTotal = 0;
        long size = 0;
        for (File f: mFileList) {
            size = FileUtils.getFileSize(f);
            if (size < 0) {
                // something wrong with the file list, maybe file deleted from sdcard manually
                // so need refresh list, but to avoid refresh frequently
                // we give a tip let user click to refresh manually
                return "<font color=\"#ff0000\">List Error, Try RELOAD</font>";
            }
            else {
                sizeTotal += size;
            }
        }
        info += ", <font color=\"#ff0000\">" + FileUtils.formatFileSize(sizeTotal) + "</font>";
        return info;
    }

    @Override
    public int getCount() {
        if (mFileList != null) {
            return mFileList.size();
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.audio_list_item, parent, false);
            holder = new ViewHolder();
            holder.ivType = convertView.findViewById(R.id.iv_image);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvSize = convertView.findViewById(R.id.tv_file_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();  //the Object stored in this view as a tag
        }

        File f = mFileList.get(position);
        if (f == null) {
            LogUtils.te2("file null on pos " + position);
        }
        else {
            String nameInfo = f.getName();
            if (position == mSelected) {
                nameInfo += "(" + TimeUtils.formatDuration(mPlayer.getCurrentPlayPosition()) + "/"
                        + TimeUtils.formatDuration(mPlayer.getCurrentPlayDuration()) + ")";
            }
            else {
                nameInfo += "(" + TimeUtils.formatDuration(WSListener.getWaveDuration(f)) + ")";
            }
            holder.tvName.setText(nameInfo);
            holder.tvSize.setText(FileUtils.getFileSizeFormat(f));
        }

        if (position == mSelected) {
            convertView.setBackgroundColor(Color.GREEN);
        }
        else {
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivType;
        TextView tvName;
        TextView tvSize;
    }
}
