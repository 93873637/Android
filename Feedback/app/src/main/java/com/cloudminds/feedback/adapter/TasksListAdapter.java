package com.cloudminds.feedback.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.net.IFUserFeedback.ImageBody;

import java.util.List;

public class TasksListAdapter extends BaseAdapter {

    private Context context;

    //从sqlite中获取list
    private List<ImageBody> list;
    //用于保存checkBox的选择状态
    private boolean[] checks;

    private LayoutInflater inflater = null;

    public TasksListAdapter(Context context, List<ImageBody> list) {
        this.context = context;
        this.list = list;
        checks = new boolean[list.size()];
        inflater = LayoutInflater.from(context);
    }

    public void setList(List<ImageBody> list) {
        this.list = list;
    }

    public boolean[] getChecks() {
        return checks;
    }

    public void setChecks(boolean[] checks) {
        this.checks = checks;
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
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tasks_list_item, null);
            holder = new ViewHolder();
            holder.tv_number = convertView.findViewById(R.id.tv_number);
            holder.tv_content = convertView.findViewById(R.id.tv_content);
            holder.tv_attachment = convertView.findViewById(R.id.tv_attachment);
            holder.cb = convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();//the Object stored in this view as a tag
        }
        holder.tv_number.setText(position + 1 + "");

        String attNamestr = list.get(position).attachment_name;
        String attSizestr = list.get(position).attachment_size;
        String attach = TextUtils.isEmpty(attNamestr) ? context.getResources().getString(R.string.no_offline_log_file) : attSizestr;
        holder.tv_content.setText(handleContent(list.get(position).content));
        holder.tv_attachment.setText(attach);
        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checks[position] = isChecked;
            }
        });
        holder.cb.setChecked(checks[position]);
        return convertView;
    }

    public static class ViewHolder {
        TextView tv_number;
        TextView tv_content;
        TextView tv_attachment;
        CheckBox cb;
    }

    //字符数过长时显示为省略号
    private String handleContent(String content) {
        if (TextUtils.isEmpty(content)) {

        } else {
            content = content.replace('\n', ' ');
            if (content.length() > 28) {
                content = content.substring(0, 28);
                content = content + "...";
            }
        }
        return content;
    }

    public int getCheckNum() {
        int checkNum = 0;
        for (Boolean b : checks) {
            if (b) checkNum++;
        }
        return checkNum;
    }

}