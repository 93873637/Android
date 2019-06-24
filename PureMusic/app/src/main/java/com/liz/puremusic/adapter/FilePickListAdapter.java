package com.liz.puremusic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.puremusic.R;
import com.liz.puremusic.ui.FilePickDialog;

import java.util.List;
import java.util.Map;

public class FilePickListAdapter extends BaseAdapter {
    private List<Map<String, Object>> list;
    private LayoutInflater inflater = null;

    public FilePickListAdapter(Context context, List<Map<String, Object>> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_pick_file, parent, false);
            holder = new ViewHolder();
            holder.ivFileType = convertView.findViewById(R.id.iv_file_type);
            holder.tvFileName = convertView.findViewById(R.id.tv_file_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();  //the Object stored in this view as a tag
        }

        Map<String, Object> map = list.get(position);
        holder.ivFileType.setImageResource(FilePickDialog.getIcon(map));
        holder.tvFileName.setText(FilePickDialog.getFileInfo(map));

        convertView.setBackgroundColor(FilePickDialog.isPicked(map) ? Color.GREEN : FilePickDialog.DEFAULT_LIST_BGCOLOR);
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivFileType;
        TextView tvFileName;
    }
}
