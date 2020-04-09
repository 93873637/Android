package com.liz.whatsai.app;

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

import java.io.File;
import java.util.ArrayList;

public class AudioListAdapter extends BaseAdapter {

    private ArrayList<File> mFileList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private String mAudioDir;

    public AudioListAdapter() {
        initAdapter(ComDef.WHATSAI_AUDIO_DIR);
    }

    public AudioListAdapter(String audioDir) {
        initAdapter(audioDir);
    }

    private void initAdapter(String audioDir) {
        mAudioDir = audioDir;
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

    public String getAudioFilesInfo() {
        return this.getListInfo();
    }

    public String getAudioDir() {
        return mAudioDir;
    }

    public void removeItem(int id) {
        mFileList.remove(id);
        notifyDataSetChanged();
    }

    public void updateList() {
        loadListData();
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
            nameInfo += "(" + TimeUtils.formatDuration(WSListener.getWaveDuration(f)) + ")";
            holder.tvName.setText(nameInfo);
            holder.tvSize.setText(FileUtils.getFileSizeFormat(f));
        }
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivType;
        TextView tvName;
        TextView tvSize;
    }
}
