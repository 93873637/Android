package com.liz.mydish.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.liz.mydish.R;
import com.liz.mydish.logic.Dish;

import java.util.ArrayList;

public class MenuListAdapter extends BaseAdapter {

    private ArrayList<Dish> mDishList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public MenuListAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);

        //load test data
        mDishList.add(new Dish("披萨", R.drawable.pizza));
        mDishList.add(new Dish("蛋糕", R.drawable.cake));
        mDishList.add(new Dish("牛排", R.drawable.beaf));
        mDishList.add(new Dish("咖喱饭", R.drawable.gali));
        mDishList.add(new Dish("火锅", R.drawable.firepot));
    }

    @Override
    public int getCount() {
        if (mDishList != null) {
            return mDishList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mDishList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.menu_list_item, parent, false);
            holder = new ViewHolder();
            holder.ivDishPic = convertView.findViewById(R.id.iv_dish);
            holder.tvDishName = convertView.findViewById(R.id.tv_dish_name);
            holder.btnDad = convertView.findViewById(R.id.btn_dad);
            holder.btnMum = convertView.findViewById(R.id.btn_mum);
            holder.btnGrandma = convertView.findViewById(R.id.btn_grandma);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();  //the Object stored in this view as a tag
        }

        holder.ivDishPic.setBackgroundResource(mDishList.get(position).resId);
        holder.tvDishName.setText(mDishList.get(position).name);

        holder.btnDad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDishList.get(position).dad_can = !mDishList.get(position).dad_can;
                holder.btnDad.setBackgroundResource(mDishList.get(position).dad_can?R.drawable.lightbulb_on1:R.drawable.lightbulb_off2);
            }
        });
        holder.btnMum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDishList.get(position).mum_can = !mDishList.get(position).mum_can;
                holder.btnMum.setBackgroundResource(mDishList.get(position).mum_can?R.drawable.lightbulb_on1:R.drawable.lightbulb_off2);
            }
        });
        holder.btnGrandma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDishList.get(position).grandma_can = !mDishList.get(position).grandma_can;
                holder.btnGrandma.setBackgroundResource(mDishList.get(position).grandma_can?R.drawable.lightbulb_on1:R.drawable.lightbulb_off2);
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        ImageView ivDishPic;
        TextView tvDishName;
        Button btnDad;
        Button btnMum;
        Button btnGrandma;
    }
}
