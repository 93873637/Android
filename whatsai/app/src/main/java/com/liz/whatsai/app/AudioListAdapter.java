package com.liz.whatsai.app;

import android.content.Context;
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

    private static AudioListAdapter adapter;
    public static AudioListAdapter getAdapter() {
        if (adapter == null) {
            adapter = new AudioListAdapter(ThisApp.getAppContext());
        }
        return adapter;
    }

    public static File getAudioFile(long pos) {
        return (File)adapter.getItem((int)pos);
    }

    public static String getAudioFilesInfo() {
        return adapter.getListInfo();
    }

    public static void onDataChanged() {
        getAdapter().notifyDataSetChanged();
    }

    public static void onUpdateList() {
        getAdapter().updateList();
    }

    private File[] list;
    private LayoutInflater inflater;

    private AudioListAdapter(Context context) {
        loadListData();
        inflater = LayoutInflater.from(context);
    }

    private void updateList() {
        loadListData();
        notifyDataSetChanged();
    }

    private void loadListData() {
        this.list = FileUtils.getFileList(ComDef.WHATSAI_AUDIO_DATA_PATH, FileUtils.ORDER_BY_DATE_DESC);
    }

    private String getListInfo() {
        String info = "Total " + list.length + " Files";
        long sizeTotal = 0;
        for (File f:list) {
            sizeTotal += FileUtils.getFileSize(f);
        }
        info += ", Size " + FileUtils.formatFileSize(sizeTotal);
        return info;
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.length;
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return list[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.audio_list_item, parent, false);
            holder = new ViewHolder();
            holder.ivType = convertView.findViewById(R.id.iv_image);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.ivPlay = convertView.findViewById(R.id.iv_play_audio);
            holder.tvSize = convertView.findViewById(R.id.tv_file_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();  //the Object stored in this view as a tag
        }

        File f = list[position];
        holder.ivType.setImageResource(R.drawable.ic_file_default);
        holder.tvName.setText(f.getName());
        holder.tvSize.setText(FileUtils.formatFileSize(FileUtils.getFileSize(f)));
        holder.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
        ImageView ivPlay;
        TextView tvSize;
    }
}
