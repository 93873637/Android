package com.cloudminds.feedback.bean;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cloudminds.feedback.R;

/**
 * Created by tomas on 15/07/15.
 */
public class CountFooterViewHolder extends RecyclerView.ViewHolder {

     TextView textView;

    public CountFooterViewHolder(View itemView) {
        super(itemView);
         textView = (TextView)itemView.findViewById(R.id.title);
    }

    public void render(String text){
        textView.setText(text);
    }
}
