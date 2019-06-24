package com.cloudminds.feedback.bean;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cloudminds.feedback.R;


/**
 * Created by tomas on 15/07/15.
 */
public class CountHeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    public View containerView;

    public CountHeaderViewHolder(View itemView) {
        super(itemView);
        textView = (TextView)itemView.findViewById(R.id.head_title);
        containerView=itemView.findViewById(R.id.head_container);
        itemView.setTag(this);
    }

    public void render(String text){
        textView.setText(text);
    }
}
