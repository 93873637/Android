package com.liz.whatsai.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.androidutils.FileUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WhatsaiAudio;

import java.io.File;

public class AudioListAdapter extends BaseAdapter {

    private File[] mFileList;
    private LayoutInflater mLayoutInflater;
    private String mAudioDir;

    public File getAudioFile(long pos) {
        return (File)this.getItem((int)pos);
    }

    public String getAudioFilePath(long pos) {
        return ((File)this.getItem((int)pos)).getAbsolutePath();
    }

    public String getAudioFilesInfo() {
        return this.getListInfo();
    }

    public void onDataChanged() {
        this.notifyDataSetChanged();
    }

    public AudioListAdapter() {
        mAudioDir = ComDef.WHATSAI_AUDIO_DIR;
        loadListData();
        mLayoutInflater = LayoutInflater.from(MyApp.getAppContext());
    }

    public AudioListAdapter(String filePath) {
        mAudioDir = filePath;
        loadListData();
        mLayoutInflater = LayoutInflater.from(MyApp.getAppContext());
    }

    public String getAudioDir() {
        return mAudioDir;
    }

    public void updateList() {
        loadListData();
        notifyDataSetChanged();
    }

    private void loadListData() {
        this.mFileList = FileUtils.getFileList(mAudioDir, FileUtils.ORDER_BY_DATE_DESC);
    }

    private String getListInfo() {
        String info = "Total <font color=\"#ff0000\">" + mFileList.length + "</font> Files";
        long sizeTotal = 0;
        for (File f: mFileList) {
            sizeTotal += FileUtils.getFileSize(f);
        }
        info += ", Size <font color=\"#ff0000\">" + FileUtils.formatFileSize(sizeTotal) + "</font>";
        return info;
    }

    @Override
    public int getCount() {
        if (mFileList != null) {
            return mFileList.length;
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mFileList[position];
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

        File f = mFileList[position];
        holder.tvName.setText(f.getName());
        holder.tvSize.setText(FileUtils.formatFileSize(FileUtils.getFileSize(f)));

        if (position == WhatsaiAudio.getPlayItemPos()) {
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
