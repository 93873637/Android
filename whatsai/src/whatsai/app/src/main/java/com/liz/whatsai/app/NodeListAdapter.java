package com.liz.whatsai.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.whatsai.R;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;

import java.util.List;

public class NodeListAdapter extends BaseAdapter {

    private static NodeListAdapter adapter;
    public static NodeListAdapter getAdapter() {
        if (adapter == null) {
            adapter = new NodeListAdapter(ThisApp.getAppContext(), DataLogic.getDataList());
        }
        return adapter;
    }

    public static void onDataChanged() {
        getAdapter().notifyDataSetChanged();
    }

    public static void onUpdateList() {
        getAdapter().updateList();
    }

    private List<Node> list;
    private LayoutInflater inflater = null;

    private NodeListAdapter(Context context, List<Node> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    private void updateList() {
        list = DataLogic.getDataList();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        else {
            return 0;
        }
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
            convertView = inflater.inflate(R.layout.node_list_item, parent, false);
            holder = new ViewHolder();
            holder.ivType = convertView.findViewById(R.id.iv_image);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();  //the Object stored in this view as a tag
        }

        Node node = list.get(position);
        if (node.isDir()) {
            holder.ivType.setImageResource(R.drawable.ic_folder);
        }
        else {
            if (node.isRemindValid()) {
                holder.ivType.setImageResource(R.drawable.ic_file_alarm);
            }
            else {
                holder.ivType.setImageResource(R.drawable.ic_file_default);
            }
        }
        holder.tvName.setText(node.getNameEx());
        if (node.isDone()) {
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
    }
}
