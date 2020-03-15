package com.liz.whatsai.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;

import java.io.File;
import java.util.ArrayList;

public class AudioListAdapter extends BaseAdapter {

    private ArrayList<File> mFileList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private String mAudioDir;
    private int mSelected = ComDef.INVALID_LIST_POS;

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

    public void clearSelected() {
        mSelected = ComDef.INVALID_LIST_POS;
        notifyDataSetChanged();
    }

    public void updateSelected(int pos) {
        setSelected(pos);
        notifyDataSetChanged();
    }

    public boolean isSelected(int pos) {
        return pos == mSelected;
    }
    public String getAudioFilesInfo() {
        return this.getListInfo();
    }

    public String getAudioDir() {
        return mAudioDir;
    }

    public void removeItem(long id) {
        mFileList.remove((int)id);
        mSelected = ComDef.INVALID_LIST_POS;
        notifyDataSetChanged();
    }

    public void updateList() {
        loadListData();
        mSelected = ComDef.INVALID_LIST_POS;
        notifyDataSetChanged();
    }

    private void loadListData() {
        mFileList.clear();
        mFileList = FileUtils.getFileArrayList(mAudioDir, FileUtils.ORDER_BY_DATE_DESC);
        //###@: todo: append file duration here: + "(" + MediaUtils.getMediaDurationFormat(f) + ")";
    }

    private String getListInfo() {
        String info = "TOTAL <font color=\"#ff0000\">" + mFileList.size() + "</font> FILES";
        long sizeTotal = 0;
        for (File f: mFileList) {
            sizeTotal += FileUtils.getFileSize(f);
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
