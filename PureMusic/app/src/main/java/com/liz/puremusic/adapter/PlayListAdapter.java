package com.liz.puremusic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liz.puremusic.R;
import com.liz.puremusic.app.ThisApp;
import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.logic.PlayItem;

import java.util.List;

public class PlayListAdapter extends BaseAdapter {

    private static PlayListAdapter adapter;
    public static PlayListAdapter getAdapter() {
        if (adapter == null) {
            adapter = new PlayListAdapter(ThisApp.getAppContext(), DataLogic.getPlayList());
        }
        return adapter;
    }

    public static void setOnListOperListener(OnListOperListener operListener) {
        getAdapter().mOperListener = operListener;
    }

    public static void onDataChanged() {
        getAdapter().notifyDataSetChanged();
    }

    public static void onUpdateList() {
        getAdapter().updateList();
    }

    private List<PlayItem> list;
    private LayoutInflater inflater = null;

    private PlayListAdapter(Context context, List<PlayItem> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    private void updateList() {
        list = DataLogic.getPlayList();
        notifyDataSetChanged();
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
            convertView = inflater.inflate(R.layout.playlist_item, parent, false);
            holder = new ViewHolder();
            holder.item_index = convertView.findViewById(R.id.tv_item_index);
            holder.item_name = convertView.findViewById(R.id.tv_item_name);
            //holder.play_status = convertView.findViewById(R.id.iv_play_status);
            holder.operation_area = convertView.findViewById(R.id.ll_operations);
            holder.remove_item = convertView.findViewById(R.id.iv_remove);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();  //the Object stored in this view as a tag
        }

        PlayItem item = list.get(position);
        String strIndex = "" + (position + 1);
        holder.item_index.setText(strIndex);
        //holder.item_name.setText(item.getFileName());
        holder.operation_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //just reserve an area for operations(move up/down, remove), in case click to select list item
            }
        });
        holder.remove_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperListener != null) {
                    mOperListener.onItemRemove(position);
                }
            }
        });

        String nameInfo = item.getFileName();
        if (position == DataLogic.getCurrentListPos()) {
            //convertView.setBackgroundColor(Color.GREEN);
            nameInfo += "(" + DataLogic.getFormatCurrentPosition() + "/" + DataLogic.getFormatCurrentDuration() + ")";
            switch (DataLogic.getPlayStatus()) {
                case ComDef.PLAY_STATUS_STARTED:
                    convertView.setBackgroundColor(Color.RED);
                    break;
                case ComDef.PLAY_STATUS_PAUSED:
                    convertView.setBackgroundColor(ThisApp.getAppContext().getColor(R.color.light_blue));
                    break;
                default:
                    convertView.setBackgroundColor(Color.GREEN);
                    break;
            }
//            holder.play_status.setVisibility(View.VISIBLE);
//            if (DataLogic.isPlaying()) {
//                holder.play_status.setBackgroundResource(R.drawable.little_sound);
//            },
//            else {
//                holder.play_status.setBackgroundResource(R.drawable.little_pause);
//            }
        }
        else {
            nameInfo += "(" + DataLogic.getMusicFileDuration(position) + ")";
//            holder.play_status.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.WHITE);
        }

        holder.item_name.setText(nameInfo);
        return convertView;
    }

    public static class ViewHolder {
        TextView item_index;
        TextView item_name;
        //ImageButton play_status;
        LinearLayout operation_area;
        //ImageButton move_up;
        //ImageButton move_down;
        ImageButton remove_item;
    }

    ////////////////////////////////////////////////////////////////////////////////
    public OnListOperListener mOperListener;

    public interface OnListOperListener {
        void onItemRemove(int position);
    }
    ////////////////////////////////////////////////////////////////////////////////
}
